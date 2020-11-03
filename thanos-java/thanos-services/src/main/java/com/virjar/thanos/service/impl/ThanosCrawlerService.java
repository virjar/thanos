package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosCrawler;
import com.virjar.thanos.entity.vo.GrabCrawlerModel;
import com.virjar.thanos.mapper.ThanosCrawlerMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 爬虫的基础信息 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
public class ThanosCrawlerService extends ServiceImpl<ThanosCrawlerMapper, ThanosCrawler> implements IService<ThanosCrawler> {

    public List<GrabCrawlerModel> allActiveCrawlers() {
        return null;
    }
}
