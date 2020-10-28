package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosServer;
import com.virjar.thanos.mapper.ThanosServerMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 抓取服务器节点，主要用作分片 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosServerService extends ServiceImpl<ThanosServerMapper, ThanosServer> implements IService<ThanosServer> {

}
