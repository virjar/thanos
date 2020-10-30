package com.virjar.thanos.api.services;


import com.virjar.thanos.api.bean.ResourceMeta;

public interface ResourceServices {

    /**
     * 分配资源
     *
     * @param group  分组
     * @param expire 期待过期资源，不过期传递-1
     * @return 分配失败后返回null
     */
    ResourceMeta allocate(String group, int expire);

    /**
     * 对特定资源加锁锁定
     *
     * @param group        group
     * @param key          key
     * @param expireSecond 锁超时时间,为时间范围，单位秒 如锁定60s
     * @return 是否加锁成功
     */
    boolean lock(String group, String key, int expireSecond);

    /**
     * 获取最新的数据
     *
     * @param group 分组
     * @param key   key
     * @return 最新的内容
     */
    ResourceMeta query(String group, String key);

    /**
     * 释放锁
     *
     * @param group group
     * @param key   key
     */
    void unlock(String group, String key);

    void feedback(ResourceMeta resourceMeta, boolean good);

    void updateContent(ResourceMeta resourceMeta);

    void forbid(ResourceMeta resourceMeta);
}
