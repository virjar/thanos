package com.virjar.thanos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.virjar.thanos.entity.ThanosKvCache;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 爬虫系统提供的一个简单的kv缓存 Mapper 接口
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
public interface ThanosKvCacheMapper extends BaseMapper<ThanosKvCache> {
    @Delete("delete from grab_kv_cache where expire < #{time} limit #{batch}")
    int clearExpireKVData(@Param("batch") int batch, @Param("time") LocalDateTime time);
}
