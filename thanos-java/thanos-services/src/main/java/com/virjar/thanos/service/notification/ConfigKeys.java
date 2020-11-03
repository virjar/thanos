package com.virjar.thanos.service.notification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigKeys {
    /**
     * 作用在{@link INotifier}的实现class。描述INotifier需要提供的配置key，
     * 然后前端配置每个key的value。在创建INotifier的时候将会传递给对应实现组件
     *
     * @return 多个配置key
     */
    String[] value();

    /**
     * 通知器类型。是具体实现的字符串id标志
     *
     * @return 类型
     */
    String type();
}
