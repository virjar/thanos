package com.virjar.thanos.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 抓取资源自动注入
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoResource {
    /**
     * 资源名称，如果没有设置，那么以FiledName为准
     *
     * @return 资源名称
     */
    String value() default "";

    /**
     * 资源申请加锁，锁超时时间<br>
     * 单位：分钟
     *
     * @return 超时时间
     */
    int expire() default -1;
}
