package com.virjar.thanos.api.test;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.virjar.thanos.api.GrabLogger;
import com.virjar.thanos.api.util.ClassScanner;
import com.virjar.thanos.api.util.ReflectUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by virjar on 17/5/28.
 */
public class TestDependencyRegistry {
    private static Set<Object> allBeans = Sets.newHashSet();

    static {
        ClassScanner.AnnotationClassVisitor visitor = new ClassScanner.AnnotationClassVisitor(TestBean.class);
        ClassScanner.scan(visitor, Lists.newArrayList(TestBean.class.getPackage().getName()));
        // 得到所有的默认bean
        Set<Class> classSet = visitor.getClassSet();

        for (Class clazz : classSet) {
            try {
                Field instance = clazz.getField("instance");

                // 规范,如果有一个叫做instance的静态字段,那么直接使用这个
                instance.setAccessible(true);
                Object o = instance.get(clazz);
                allBeans.add(o);
                continue;
            } catch (Throwable e) {
                // ignore
            }

            try {
                Object o = clazz.newInstance();
                allBeans.add(o);
                continue;
            } catch (Exception e) {
                // ignore
            }
            throw new IllegalStateException("class:" + clazz + " 不能注入到依赖环境,确认有空参构造或者提供了一个instance的静态字段");
        }
    }

    public static void registBean(Object o) {
        allBeans.add(o);
    }

    public static void removeBean(Class<?> type) {
        Set<Object> needRemove = new HashSet<>();
        for (Object o : allBeans) {
            if (type.isAssignableFrom(o.getClass())) {
                needRemove.add(o);
            }
        }
        allBeans.removeAll(needRemove);
    }

    /**
     * 对某个对象实现自动注入
     *
     * @param bean 本处理对象
     */
    public static <T> T injectDependency(T bean, boolean registerSelf) {
        if (registerSelf) {
            registBean(bean);
        }
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotation(Resource.class) != null || field.getAnnotation(Autowired.class) != null) {
                // 确认是否有值,没有则尝试注入
                Object fieldValue = ReflectUtil.getFieldValue(bean, field.getName());
                if (fieldValue != null) {
                    injectDependency(fieldValue, false);
                    continue;
                }

                // 匹配对象
                Class<?> declaringClass = field.getType();
                for (Object candidateBean : allBeans) {
                    if (declaringClass.isAssignableFrom(candidateBean.getClass())) {
                        GrabLogger.info("为:" + bean + "注入依赖:" + candidateBean + " 对应字段:" + field.getName());
                        ReflectUtil.setFieldValue(bean, field.getName(), candidateBean);
                        break;
                    }
                }

                fieldValue = ReflectUtil.getFieldValue(bean, field.getName());
                if (fieldValue == null && !declaringClass.isInterface() && !Modifier.isAbstract(declaringClass.getModifiers())) {// 如果还是为空,那么创建一个,然后注入
                    try {
                        Object o = declaringClass.newInstance();
                        GrabLogger.info("为:" + bean + "注入依赖:" + o + " 对应字段:" + field.getName());
                        ReflectUtil.setFieldValue(bean, field.getName(), o);
                        injectDependency(o);// 循环注入
                    } catch (Exception e) {
                        GrabLogger.error("bean:" + bean + " 注入依赖:" + field.getName() + " 失败", e);
                    }
                }
            }
        }
        return bean;
    }

    public static <T> T injectDependency(T bean) {
        return injectDependency(bean, true);
    }

}
