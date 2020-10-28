package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosTaskBlob;
import com.virjar.thanos.mapper.ThanosTaskBlobMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 爬虫任务的数据区域，由于数据内容会比较大。所以单独抽离出来，避免任务调度的时候影响性能 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosTaskBlobService extends ServiceImpl<ThanosTaskBlobMapper, ThanosTaskBlob> implements IService<ThanosTaskBlob> {

}
