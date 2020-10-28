package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosCrawlerJar;
import com.virjar.thanos.mapper.ThanosCrawlerJarMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 爬虫实例jar包，可以被升级和降级 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosCrawlerJarService extends ServiceImpl<ThanosCrawlerJarMapper, ThanosCrawlerJar> implements IService<ThanosCrawlerJar> {

}
