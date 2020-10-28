package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosResource;
import com.virjar.thanos.mapper.ThanosResourceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 爬虫资源管理 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosResourceService extends ServiceImpl<ThanosResourceMapper, ThanosResource> implements IService<ThanosResource> {

}
