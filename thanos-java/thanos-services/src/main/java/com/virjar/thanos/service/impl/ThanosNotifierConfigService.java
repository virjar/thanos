package com.virjar.thanos.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.virjar.thanos.entity.ThanosNotifierConfig;
import com.virjar.thanos.entity.vo.GrabCrawlerModel;
import com.virjar.thanos.mapper.ThanosNotifierConfigMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virjar.thanos.service.notification.GrabFinishMessage;
import com.virjar.thanos.service.notification.GrabNotificationManager;
import com.virjar.thanos.service.notification.INotifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>
 * 抓取完通知配置 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
@Slf4j
public class ThanosNotifierConfigService extends ServiceImpl<ThanosNotifierConfigMapper, ThanosNotifierConfig> implements IService<ThanosNotifierConfig> {
    @Resource
    private ThanosCrawlerService thanosCrawlerService;

    private class NotifierConfigHolder {
        private long createTimestamp;
        private long updateScopeTimestamp;
        private String name;
        private ThanosNotifierConfig grabNotifierConfig;
        private INotifier iNotifier;
        private Set<String> scope;

        boolean isSupport(String crawlerName) {
            return scope.isEmpty() || scope.contains(crawlerName);
        }

        void reloadScopeIfNeed() {
            if (System.currentTimeMillis() - updateScopeTimestamp > duration * 5) {
                updateScopeTimestamp = System.currentTimeMillis();
                scope = parseScope(grabNotifierConfig.getScope());
            }
        }

    }

    private static final long duration = 2 * 60 * 1000;
    private long lastUpdate = 0;

    private void refreshModeIfNeed() {
        if (lastUpdate > 10000 && System.currentTimeMillis() - lastUpdate < duration) {
            return;
        }
        lastUpdate = System.currentTimeMillis();
        List<ThanosNotifierConfig> list = list(new QueryWrapper<ThanosNotifierConfig>().eq(ThanosNotifierConfig.ENABLE, true));

        Map<String, NotifierConfigHolder> oldConfig = Maps.newHashMap(allNotifiers);
        Map<String, NotifierConfigHolder> newConfig = Maps.newHashMap();


        for (ThanosNotifierConfig grabNotifierConfig : list) {
            NotifierConfigHolder notifierConfigHolder = oldConfig.remove(grabNotifierConfig.getName());
            if (notifierConfigHolder == null) {
                notifierConfigHolder = create(grabNotifierConfig);
            } else if (notifierConfigHolder.createTimestamp < grabNotifierConfig.getUpdateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()) {
                notifierConfigHolder.iNotifier.destroy();
                notifierConfigHolder = create(grabNotifierConfig);
            }
            if (notifierConfigHolder == null) {
                log.error("can not crate Notifier:{} ", grabNotifierConfig.getName());
                grabNotifierConfig.setEnable(false);
                updateById(grabNotifierConfig);
                continue;
            }
            newConfig.put(grabNotifierConfig.getName(), notifierConfigHolder);
        }

        for (NotifierConfigHolder needRemove : oldConfig.values()) {
            needRemove.iNotifier.destroy();
        }

        allNotifiers = newConfig;
    }

    private NotifierConfigHolder create(ThanosNotifierConfig grabNotifierConfig) {
        JSONObject config = JSONObject.parseObject(grabNotifierConfig.getConfig());
        Map<String, String> createParam = Maps.newHashMap();
        for (String key : config.keySet()) {
            createParam.put(key, config.getString(key));
        }

        INotifier notifier = GrabNotificationManager.createNotifier(grabNotifierConfig.getType(), createParam);
        if (notifier == null) {
            return null;
        }
        NotifierConfigHolder notifierConfigHolder = new NotifierConfigHolder();
        notifierConfigHolder.iNotifier = notifier;
        notifierConfigHolder.createTimestamp = System.currentTimeMillis();
        notifierConfigHolder.grabNotifierConfig = grabNotifierConfig;
        notifierConfigHolder.name = grabNotifierConfig.getName();
        String scope = grabNotifierConfig.getScope();
        notifierConfigHolder.scope = parseScope(scope);
        notifierConfigHolder.updateScopeTimestamp = System.currentTimeMillis();
        return notifierConfigHolder;
    }

    private Set<String> parseScope(String dbScope) {
        if (StringUtils.isBlank(dbScope) || "*".equalsIgnoreCase(dbScope)) {
            return Collections.emptySet();
        }
        Set<String> parsedScope = Sets.newHashSet();
        if (dbScope.contains("|")) {
            parsedScope.addAll(Lists.newArrayList(Splitter.on("|").split(dbScope)));
        }
        try {
            Pattern pattern = Pattern.compile(dbScope);
            List<GrabCrawlerModel> grabCrawlerModels = thanosCrawlerService.allActiveCrawlers();
            for (GrabCrawlerModel grabCrawlerModel : grabCrawlerModels) {
                if (pattern.matcher(grabCrawlerModel.getCrawlerName()).matches()) {
                    parsedScope.add(grabCrawlerModel.getCrawlerName());
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return parsedScope;

    }


    private Map<String, NotifierConfigHolder> allNotifiers = Maps.newHashMap();

    public void doNotify(GrabFinishMessage grabFinishMessage) {
        refreshModeIfNeed();
        for (NotifierConfigHolder notifierConfigHolder : allNotifiers.values()) {
            notifierConfigHolder.reloadScopeIfNeed();
            if (!notifierConfigHolder.isSupport(grabFinishMessage.getCrawlerId())) {
                continue;
            }
            try {
                notifierConfigHolder.iNotifier.doNotify(grabFinishMessage);
            } catch (Exception e) {
                log.error("failed to notify grab result crawler:{} notifier:{}",
                        grabFinishMessage.getCrawlerId(), notifierConfigHolder.name, e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        for (NotifierConfigHolder notifierConfigHolder : allNotifiers.values()) {
            try {
                notifierConfigHolder.iNotifier.destroy();
            } catch (Exception e) {
                log.error("close notifier error", e);
            }
        }
    }
}
