package com.virjar.thanos.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoParam {

    /**
     * 绑定参数
     *
     * @return 默认为Field name，故该字段可以为空
     */
    String value() default "";


    /**
     * 该字段是否可以为空
     *
     * @return 是否可以为空
     */
    boolean nullable() default true;

    /**
     * 参数校验规则
     *
     * @return 一个正则表达式，如果为空则证明没有格式校验
     */
    String pattern() default "";

    /**
     * 默认值，框架会自动进行参数转换
     *
     * @return 默认值
     */
    String defaultValue() default "";

    /**
     * 演示value，在渲染wrapper文档的时候，使用这个值。
     * 文档渲染当且仅当nullable为false的时候生效
     *
     * @return 演示value
     */
    String demoValue() default "";

    /**
     * 该字段对应的参数解释
     *
     * @return 字段描述
     */
    String description() default "";
}
