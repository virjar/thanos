package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosNotifierConfig;
import com.virjar.thanos.mapper.ThanosNotifierConfigMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 抓取完通知配置 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosNotifierConfigService extends ServiceImpl<ThanosNotifierConfigMapper, ThanosNotifierConfig> implements IService<ThanosNotifierConfig> {

}
