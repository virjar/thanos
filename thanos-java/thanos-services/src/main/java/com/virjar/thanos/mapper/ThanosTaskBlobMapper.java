package com.virjar.thanos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.virjar.thanos.entity.ThanosTaskBlob;

/**
 * <p>
 * 爬虫任务的数据区域，由于数据内容会比较大。所以单独抽离出来，避免任务调度的时候影响性能 Mapper 接口
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
public interface ThanosTaskBlobMapper extends BaseMapper<ThanosTaskBlob> {

}
