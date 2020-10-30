package com.virjar.thanos.api.interfaces;

import org.springframework.context.ApplicationContext;

/**
 * Created by virjar on 2018/2/3.<br>
 */
public interface SpringContextAware {
    void init4SpringContext(ApplicationContext webApplicationContext);
}
