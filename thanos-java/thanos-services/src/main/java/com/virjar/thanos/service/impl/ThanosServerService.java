package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.virjar.thanos.entity.ThanosServer;
import com.virjar.thanos.mapper.ThanosServerMapper;
import com.virjar.thanos.util.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 抓取服务器节点，主要用作分片 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
public class ThanosServerService extends ServiceImpl<ThanosServerMapper, ThanosServer> implements IService<ThanosServer> {
    @PostConstruct
    public void makeSureServerInfo() {
        String computerId = Environment.computerId();
        ThanosServer one = getOne(new QueryWrapper<ThanosServer>()
                .eq(ThanosServer.EXECUTOR, computerId));
        if (one != null) {
            reloadServerNode();
            return;
        }

        ThanosServer grabServer = new ThanosServer();
        grabServer.setEnable(true);
        grabServer.setExecutor(computerId);
        grabServer.setServerComment("auto find by thanos system");
        save(grabServer);

        reloadServerNode();
    }


    public List<String> getAllServerNodes() {
        if (allServerNodes == null || lastUpdate < 100000) {
            reloadServerNode();
            return allServerNodes;
        }
        if (System.currentTimeMillis() - lastUpdate > 30 * 1000) {
            // 10分钟同步一下db
            reloadServerNode();
        }
        return allServerNodes;
    }

    private long lastUpdate = 0L;

    private List<String> allServerNodes = null;

    private ThanosServer mNode = null;

    public ThanosServer getMNode() {
        if (mNode == null || System.currentTimeMillis() - lastUpdate > 30 * 1000) {
            reloadServerNode();
        }
        return mNode;
    }

    public void reloadServerNode() {
        String computerId = Environment.computerId();

        List<ThanosServer> list = list(new QueryWrapper<>());
        Set<String> nodeSet = new HashSet<>();

        for (ThanosServer grabServer : list) {
            if (grabServer.getEnable() && grabServer.getLastSurvivalTime() != null
                    && grabServer.isAlive()) {
                nodeSet.add(grabServer.getExecutor());
            }
            if (computerId.equals(grabServer.getExecutor())) {
                mNode = grabServer;
            }

        }
        List<String> ret = Lists.newArrayList(nodeSet);
        Collections.sort(ret);
        allServerNodes = ret;
        lastUpdate = System.currentTimeMillis();

    }

    @Scheduled(fixedRate = 100000)
    public void writeResearchServer() {
        String computerId = Environment.computerId();
        ThanosServer one = getOne(new QueryWrapper<ThanosServer>()
                .eq(ThanosServer.EXECUTOR, computerId));
        if (one == null) {
            return;
        }
        one.setLastSurvivalTime(LocalDateTime.now());
        updateById(one);
    }

    public boolean isMaster() {
        ThanosServer mNode = getMNode();
        return mNode.getMaster();
    }


    public void occupyMaster() {
        ThanosServer mastNode = getOne(new QueryWrapper<ThanosServer>()
                .eq(ThanosServer.MASTER, true)
                .last("limit 1")
        );

        if (mastNode != null && mastNode.isAlive()) {
            return;
        }

        if (mastNode != null) {
            update(new UpdateWrapper<ThanosServer>()
                    .eq(ThanosServer.ID, mastNode.getId()).eq(ThanosServer.MASTER, true)
                    .set(ThanosServer.MASTER, false)
            );
        }

        ThanosServer mNode = getMNode();
        mNode.setMaster(true);
        updateById(mNode);

        List<ThanosServer> list = list(new QueryWrapper<ThanosServer>()
                .eq(ThanosServer.MASTER, true)
                .orderByAsc(ThanosServer.ID)
        );

        if (list.size() <= 1) {
            return;
        }

        //抢到master执行权之后，并不会马上执行任务，所以理论上不会存在一致性问题
        //也即不会由两台服务器同时获得master权限
        for (int i = 1; i < list.size(); i++) {
            ThanosServer grabServer = list.get(i);
            grabServer.setMaster(false);
            updateById(grabServer);
        }
    }

}
