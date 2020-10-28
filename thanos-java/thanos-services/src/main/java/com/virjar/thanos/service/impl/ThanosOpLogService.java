package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosOpLog;
import com.virjar.thanos.mapper.ThanosOpLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志，方便用户操作溯源 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosOpLogService extends ServiceImpl<ThanosOpLogMapper, ThanosOpLog> implements IService<ThanosOpLog> {

}
