package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosCrawlerConfig;
import com.virjar.thanos.mapper.ThanosCrawlerConfigMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 爬虫配置 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosCrawlerConfigService extends ServiceImpl<ThanosCrawlerConfigMapper, ThanosCrawlerConfig> implements IService<ThanosCrawlerConfig> {

}
