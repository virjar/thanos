package com.virjar.thanos.mapper;

import com.virjar.thanos.entity.ThanosServer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 抓取服务器节点，主要用作分片 Mapper 接口
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
public interface ThanosServerMapper extends BaseMapper<ThanosServer> {

}
