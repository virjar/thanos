package com.virjar.thanos.service;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.segments.NormalSegmentList;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.extension.parsers.DynamicTableNameParser;
import com.baomidou.mybatisplus.extension.parsers.ITableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.virjar.thanos.entity.ThanosGrabTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

@Configuration
@Slf4j
@Component
public class DynamicTableService {


    @Value("${grab.config.task_tables}")
    private String taskTableList;

    private static final int MIN_V_NODE_SIZE = 512;

    private TreeMap<Long, String> dynamicTableMappings;

    private static final ThreadLocal<String> forceUseTable = new ThreadLocal<>();

    private int init(String tableConfig) {
        String[] taskTables = tableConfig.split(",");
        dynamicTableMappings = Maps.newTreeMap();
        int vNodeSize = 1;
        if (taskTables.length < MIN_V_NODE_SIZE) {
            vNodeSize = MIN_V_NODE_SIZE / taskTables.length;
        }
        for (String table : taskTables) {
            table = table.trim();
            //如果分表数量太少，会引起哈希环不平均，所以增加虚拟节点映射
            for (int vNode = 0; vNode < vNodeSize; vNode++) {
                dynamicTableMappings.put(murHash(table + "_vNode_" + vNode), table);
            }
        }
        return taskTables.length;
    }

    /**
     * 强制使用某个分表，忽略一致性哈希动态规则。主要在分表数据迁移、mybatisPlus封装外部两个场景下使用
     *
     * @param table 具体表名
     */
    public void useTable(String table) {
        if (table == null) {
            forceUseTable.remove();
        } else {
            forceUseTable.set(table);
        }
    }

    public void useTableForCrawler(String crawler) {
        useTable(queryMapping(crawler));
    }

    /**
     * 查询分表规则，根据一致性hash规则，动态计算一个爬虫需要映射到那张表
     *
     * @param crawlerName 爬虫ID
     * @return 该爬虫对应的表
     */
    public String queryMapping(String crawlerName) {
        // 这里通过一致性哈希分表
        //mysql查询默认不区分大小写，所以分表的时候，也要加上这个
        crawlerName = crawlerName.toUpperCase();
        long hash = murHash(crawlerName);
        SortedMap<Long, String> subMap = dynamicTableMappings.tailMap(hash);
        if (subMap.isEmpty()) {
            return dynamicTableMappings.firstEntry().getValue();
        } else {
            return subMap.get(subMap.firstKey());
        }
    }

    /**
     * 系统配置的所有动态表
     *
     * @return 系统配置的所有动态表
     */
    public List<String> availableTaskTables() {
        //主要存在消重，有虚拟环之后，value会重复出现
        return Lists.newArrayList(Sets.newTreeSet(dynamicTableMappings.values()));
    }


    private String judgeCrawlerIdFromQueryWrapper(AbstractWrapper<?, ?, ?> queryWrapper) {
        NormalSegmentList sqlSegments = queryWrapper.getExpression().getNormal();
        for (int i = 0; i < sqlSegments.size() - 2; i++) {
            ISqlSegment iSqlSegment = sqlSegments.get(i);
            if (!ThanosGrabTask.CRAWLER_NAME.equals(iSqlSegment.getSqlSegment().trim())) {
                continue;
            }
            if (!SqlKeyword.EQ.equals(sqlSegments.get(i + 1))) {
                continue;
            }
            return sqlSegments.get(i + 2).getSqlSegment().trim();
        }
        return null;
    }

    private String judgeCrawlerIdFromSqlParam(Object parameterObject) {
        if (parameterObject instanceof Map) {
            for (Object obj : ((Map<?, ?>) parameterObject).values()) {
                if (obj instanceof AbstractWrapper) {
                    String crawlerIdView = judgeCrawlerIdFromQueryWrapper((AbstractWrapper) obj);
                    if (StringUtils.isBlank(crawlerIdView)) {
                        continue;
                    }

                    if (!crawlerIdView.startsWith("#{") || !crawlerIdView.endsWith("}")) {
                        return crawlerIdView;
                    }

                    //#{ew.paramNameValuePairs.MPGENVAL4}
                    crawlerIdView = crawlerIdView.substring(2, crawlerIdView.length() - 1);
                    String crawlerId = (String) SystemMetaObject.forObject(parameterObject).getValue(crawlerIdView);
                    if (StringUtils.isNotBlank(crawlerId)) {
                        return crawlerId;
                    } else {
                        return crawlerIdView;
                    }

                } else if (obj instanceof ThanosGrabTask) {
                    return ((ThanosGrabTask) obj).getCrawlerName();
                } else {
                    log.warn("unknown parameterObject type: {}", obj.getClass().getName());
                }
                //其他类型暂时不知道如何处理1
            }
        } else if (parameterObject instanceof ThanosGrabTask) {
            return ((ThanosGrabTask) parameterObject).getCrawlerName();
        } else {
            log.warn("unknown parameterObject type: {}", parameterObject.getClass().getName());
        }
        //其他类型暂时不知道如何处理2
        return null;
    }

    /**
     * https://gitee.com/baomidou/mybatis-plus-samples/blob/master/mybatis-plus-sample-dynamic-tablename/src/main/java/com/baomidou/mybatisplus/samples/dytablename/config/MybatisPlusConfig.java<br/>
     * https://mp.baomidou.com/guide/dynamic-table-name-parser.html
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        int tableSize = init(taskTableList);

        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        DynamicTableNameParser dynamicTableNameParser = new DynamicTableNameParser();
        dynamicTableNameParser.setTableNameHandlerMap(new HashMap<String, ITableNameHandler>(tableSize) {{
            put("grab_task", (metaObject, sql, tableName) -> {

                String forceTable = forceUseTable.get();
                if (forceTable != null) {
                    return forceTable;
                }
                String crawlerName = judgeCrawlerIdFromSqlParam(metaObject.getValue("delegate.boundSql.parameterObject"));
                if (crawlerName == null) {
                    log.warn("can not deal with {crawler_name} for sql: {} for stack:", sql, new Throwable());
                    return null;
                }
                return queryMapping(crawlerName);
            });
        }});
        paginationInterceptor.setSqlParserList(Collections.singletonList(dynamicTableNameParser));
        return paginationInterceptor;
    }

    private static final int seed = 0x1234ABCD;
    private static final long m = 0xc6a4a7935bd1e995L;
    private static final int r = 47;

    private static Long murHash(String key) {
//        while (key.length() < 64) {
//            key = StringUtils.join(key, key);
//        }

        //ByteBuffer buf = ByteBuffer.wrap(Md5Util.md5(key));
        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        long h = seed ^ (buf.remaining() * m);
        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

}
