package com.virjar.thanos.api.services;


/**
 * 爬虫中台缓存，基于DB而非redis，请不要把它当成redis使用。
 * 用来存储抓取中间可以复用的中间数据
 */
public interface KvCacheService {
    /**
     * 查询内容，如果不存在或者过期，那么返回null
     *
     * @param group 存在group
     * @param key   数据对应key
     * @return 数据内容
     */
    String get(String group, String key);

    /**
     * 查询内容，如果不存在或者过期，那么返回null。group在公共group，请注意个爬虫之后是否会冲突混用
     *
     * @param key 数据对应key
     * @return 数据内容
     */
    String get(String key);

    /**
     * 设置缓存内容
     *
     * @param group  存在group
     * @param key    数据对应key
     * @param value  数据内容
     * @param expire 过期时间
     */
    void set(String group, String key, String value, long expire);

    void set(String key, String value, long expire);
}
