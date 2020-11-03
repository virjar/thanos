package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virjar.thanos.entity.ThanosKvCache;
import com.virjar.thanos.mapper.ThanosKvCacheMapper;
import com.virjar.thanos.service.oss.OssManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * <p>
 * 爬虫系统提供的一个简单的kv缓存 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
@Slf4j
public class ThanosKvCacheService extends ServiceImpl<ThanosKvCacheMapper, ThanosKvCache> implements IService<ThanosKvCache> {

    @Resource
    private ThanosKvCacheMapper thanosKvCacheMapper;

    public void add(String group, String key, String value, long expire) {
        ThanosKvCache one = getOne(new QueryWrapper<ThanosKvCache>()
                .eq(ThanosKvCache.CACHE_GROUP, group)
                .eq(ThanosKvCache.CACHE_KEY, key));

        if (one == null) {
            one = new ThanosKvCache();
        }


        one.setCacheValue(OssManager.transformToOssIfNeed("db_kv_cache_" + key, value));
        one.setExpire(LocalDateTime.ofInstant(new Date(expire).toInstant(), ZoneId.systemDefault()));

        if (one.getId() != null) {
            updateById(one);
        } else {
            save(one);
        }

    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void clearExpireKey() {
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(1);
        while (true) {
            log.info("begin of clearExpireKVData... ");
            int i = thanosKvCacheMapper.clearExpireKVData(128, localDateTime);
            log.info("the expired data num: {}", i);
            if (i <= 0) {
                return;
            }
        }
    }

    public void remove(String group, String key) {
        ThanosKvCache one = getOne(new QueryWrapper<ThanosKvCache>()
                .eq(ThanosKvCache.CACHE_GROUP, group)
                .eq(ThanosKvCache.CACHE_KEY, key));
        if (one == null) {
            return;
        }
        removeById(one.getId());
    }

    public String get(String group, String key) {
        ThanosKvCache one = getOne(new QueryWrapper<ThanosKvCache>()
                .eq(ThanosKvCache.CACHE_GROUP, group)
                .eq(ThanosKvCache.CACHE_KEY, key));
        if (one == null) {
            return null;
        }
        if (one.getExpire().isBefore(LocalDateTime.now())) {
            remove(group, key);
            return null;
        }
        return OssManager.restoreOssContentIfHas(one.getCacheValue());
    }
}
