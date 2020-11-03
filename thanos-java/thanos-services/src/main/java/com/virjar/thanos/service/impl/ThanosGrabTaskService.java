package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosGrabTask;
import com.virjar.thanos.mapper.ThanosGrabTaskMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 爬虫任务，和seed相同概念 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
public class ThanosGrabTaskService extends ServiceImpl<ThanosGrabTaskMapper, ThanosGrabTask> implements IService<ThanosGrabTask> {

}
