package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosKvCache;
import com.virjar.thanos.mapper.ThanosKvCacheMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 爬虫系统提供的一个简单的kv缓存 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosKvCacheService extends ServiceImpl<ThanosKvCacheMapper, ThanosKvCache> implements IService<ThanosKvCache> {

}
