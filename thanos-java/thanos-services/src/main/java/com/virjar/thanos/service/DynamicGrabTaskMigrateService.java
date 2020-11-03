package com.virjar.thanos.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.virjar.thanos.entity.ThanosGrabTask;
import com.virjar.thanos.mapper.ThanosGrabTaskMapper;
import com.virjar.thanos.service.impl.ThanosCrawlerService;
import com.virjar.thanos.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 动态表，动态迁移表内容
 */
@Service
@Slf4j
public class DynamicGrabTaskMigrateService {

    @Resource
    private DynamicTableService dynamicTableService;

    @Resource
    private ThanosCrawlerService thanosCrawlerService;

    @Resource
    private ThanosGrabTaskMapper thanosGrabTaskMapper;

    private final Executor grabTaskMigrateExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("grabTaskMigrate"));


    private volatile boolean running = false;

    private static final int batchSize = 128;

    /**
     * 请使用http接口触发<br>
     * 原因是我们需要等所有机器发布完成，代码（包括配置）达到一致状态，再进行表迁移<br>
     * 否则多台服务器可能再做相互搬移的动作
     */
    public void scheduleMigrate() {
        if (running) {
            //可能从后台强行触发
            log.info("the migrate task running already!!");
            return;
        }
        grabTaskMigrateExecutor.execute(this::doMigrate);
    }

    public void stopMigrate() {
        running = false;
    }

    //
    private void doMigrate() {
//        running = true;
//        try {
//            List<String> tables = dynamicTableService.availableTaskTables();
//            //没台服务器，只处理他自己的分片爬虫，分片外的自己不处理
//            List<GrabCrawlerModel> grabCrawlerModels = thanosCrawlerService.splitCrawlers();
//
//            // 10 * 25 * 128 = 32000 每次每个爬虫可以迁移3万条任务
//            for (int i = 0; i < 10; i++) {
//                long totalRecord = 0;
//                for (GrabCrawlerModel grabCrawlerModel : grabCrawlerModels) {
//                    if (!running) {
//                        return;
//                    }
//                    log.info("do migrate task for crawler :{}", grabCrawlerModel.getCrawlerName());
//                    totalRecord += migrateForCrawler(grabCrawlerModel, tables);
//
//                }
//                if (totalRecord == 0) {
//                    break;
//                }
//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e) {
//                    break;
//                }
//            }
//
//
//        } finally {
//            running = false;
//            dynamicTableService.useTable(null);
//        }
    }
//
//
//    private long migrateForCrawler(GrabCrawlerModel grabCrawlerModel, List<String> tables) {
//        String crawlerName = grabCrawlerModel.getCrawlerName();
//        long totalRecord = 0;
//        for (String fromTable : tables) {
//            String targetTable = dynamicTableService.queryMapping(crawlerName);
//            //从 fromTable 迁移到 targetTable
//            if (fromTable.equals(targetTable)) {
//                continue;
//            }
//
//            for (int i = 0; i < 25; i++) {
//                if (!running) {
//                    return 0;
//                }
//                int migrateRecordSize = migrateForTable(fromTable, targetTable, crawlerName);
//                if (migrateRecordSize == 0) {
//                    break;
//                }
//                totalRecord += migrateRecordSize;
//            }
//        }
//        log.info("migrate record size:{}", totalRecord);
//        return totalRecord;
//    }

    private int migrateForTable(String fromTable, String targetTable, String crawlerName) {
        log.info("migrate grab task from :{} to:{} for crawler:{}", fromTable, targetTable, crawlerName);
        //原表查询数据
        dynamicTableService.useTable(fromTable);
        List<ThanosGrabTask> grabTasks = thanosGrabTaskMapper.selectList(
                new QueryWrapper<ThanosGrabTask>()
                        .eq(ThanosGrabTask.CRAWLER_NAME, crawlerName)
                        .last("limit " + batchSize)
        );
        if (grabTasks.isEmpty()) {
            return 0;
        }
        //插入到目标表
        int size = 0;

        for (ThanosGrabTask thanosGrabTask : grabTasks) {
            Long originId = thanosGrabTask.getId();
            thanosGrabTask.setId(null);
            dynamicTableService.useTable(targetTable);
            boolean success = false;
            try {
                success = (thanosGrabTaskMapper.insert(thanosGrabTask) == 1);
            } catch (DuplicateKeyException ignore) {
                // duplicate key 直接忽略
                success = true;
            } catch (Exception e) {
                log.warn("insert failed", e);
                // remove even through duplicate record
                Integer count = thanosGrabTaskMapper.selectCount(new QueryWrapper<ThanosGrabTask>()
                        .eq(ThanosGrabTask.CRAWLER_NAME, crawlerName)
                        .eq(ThanosGrabTask.TASK_ID, thanosGrabTask.getTaskId())
                );
                if (count > 0) {
                    success = true;
                }
            }
            if (success) {
                dynamicTableService.useTable(fromTable);
                thanosGrabTaskMapper.deleteById(originId);
                size++;
            }
        }
        log.info("migrate grab task from :{} to:{} for crawler:{} recordSize:{}", fromTable, targetTable, crawlerName, size);
        return size;
    }
}
