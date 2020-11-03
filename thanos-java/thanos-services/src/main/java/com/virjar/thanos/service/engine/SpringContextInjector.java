package com.virjar.thanos.service.engine;

import com.google.common.collect.Sets;
import com.virjar.thanos.api.util.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * 实现对wrapper的对象的springContext单向依赖注入
 */
@Service
@Slf4j
public class SpringContextInjector implements ApplicationListener<ContextRefreshedEvent> {

    @Getter
    private static SpringContextInjector instance;

    public SpringContextInjector() {
        instance = this;
    }

    @Getter
    private ApplicationContext applicationContext;

    private void injectField(Object bean, ApplicationContext applicationContext, Field field, Set<Object> extensionBean) {
        // 确认是否有值,没有则尝试注入
        Object fieldValue = ReflectUtil.getFieldValue(bean, field.getName());
        if (fieldValue != null) {
            injectDependency(fieldValue, extensionBean);
            return;
        }

        // 匹配对象
        Class<?> declaringClass = field.getType();
        //test find bean in  spring context
        try {
            fieldValue = applicationContext.getBean(declaringClass);
        } catch (Exception e) {
            //ignore
        }
        if (fieldValue == null) {
            //test find bean in extension
            for (Object candidateBean : extensionBean) {
                if (declaringClass.isAssignableFrom(candidateBean.getClass())) {
                    fieldValue = candidateBean;
                    break;
                }
            }
        }
        if (fieldValue != null) {
            ReflectUtil.setFieldValue(bean, field.getName(), fieldValue);
        }
        fieldValue = ReflectUtil.getFieldValue(bean, field.getName());
        if (fieldValue == null) {// 如果还是为空,那么创建一个,然后注入
            try {
                fieldValue = declaringClass.newInstance();
                injectDependency(fieldValue, extensionBean);// 循环注入
                extensionBean.add(fieldValue);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("bean:{} 注入依赖:{} 失败", bean, field.getName(), e);
                }
            }
        }
        if (fieldValue != null) {
            ReflectUtil.setFieldValue(bean, field.getName(), fieldValue);
        }
    }

    /**
     * 对某个对象实现自动注入
     *
     * @param bean 本处理对象
     */
    public void injectDependency(Object bean) {
        Set<Object> extensionBean = Sets.newHashSet();
        injectDependency(bean, extensionBean);
    }


    private void injectDependency(Object bean, Set<Object> extensionBean) {
        extensionBean.add(bean);
        Class<?> clazz = bean.getClass();
        while (clazz != null) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.getAnnotation(Resource.class) != null || field.getAnnotation(Autowired.class) != null) {
                    injectField(bean, applicationContext, field, extensionBean);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        applicationContext = contextRefreshedEvent.getApplicationContext();
    }
}
