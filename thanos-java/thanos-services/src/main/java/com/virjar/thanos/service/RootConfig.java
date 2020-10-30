package com.virjar.thanos.service;

import com.alibaba.fastjson.parser.ParserConfig;
import com.virjar.thanos.util.TokenQueue;
import com.virjar.thanos.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Service
@RootConfig.ConfigPath("thanos.system")
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RootConfig implements InitializingBean {

    /**
     * 阿里oss配置
     */
    @ConfigPath("oss.ali")
    public static class ALIOss {
        public static String endpoint;
        public static String accessKeyId;
        public static String accessKeySecret;
        public static String bucket;
    }

    /**
     * minio配置
     */
    @ConfigPath("oss.minio")
    public static class MINIOss {
        public static String endpoint;
        public static String accessKeyId;
        public static String accessKeySecret;
        public static String bucket;
    }

    @ThanosConfig(value = "working_root", defaultValue = "${user.home}")
    public static String workingRoot;

    @ThanosConfig(value = "jar_cache_files_size", defaultValue = "120")
    public static int jarCacheSizeStr;


    @Override
    public void afterPropertiesSet() {
        handleClassConfig(RootConfig.class, "");
        log.info("thanos config refresh end");
    }


    private void handleClassConfig(Class<?> clazz, String path) {
        String nowPath = clazz.getSimpleName();
        ConfigPath configPath = clazz.getAnnotation(ConfigPath.class);
        if (configPath != null) {
            nowPath = configPath.value();
        }
        path += nowPath;
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            String property = null;
            ThanosConfig config = field.getAnnotation(ThanosConfig.class);
            if (config != null) {
                property = config.value();
            } else if (TypeUtils.wrapperToPrimitive(field.getType()) != null
                    || CharSequence.class.isAssignableFrom(field.getType())
            ) {
                property = field.getName();
            }
            if (StringUtils.isBlank(property)) {
                continue;
            }
            property = property.trim();
            String key = path + "." + property;
            injectValue(field, key, config == null ? null : config.defaultValue());
        }

        Class<?>[] declaredClasses = clazz.getDeclaredClasses();
        for (Class<?> innerClazz : declaredClasses) {
            if (!Modifier.isPublic(innerClazz.getModifiers())) {
                continue;
            }
            if (Modifier.isInterface(innerClazz.getModifiers())) {
                continue;
            }
            handleClassConfig(innerClazz, path + ".");
        }
    }

    private Object findProperty(String key) {
        for (PropertySource<?> propertySource : propertySourcesPlaceholderConfigurer.getAppliedPropertySources()) {
            Object property = propertySource.getProperty(key);
            if (property != null) {
                return property;
            }
        }
        return null;
    }

    private Object invokeExpression(String exp, String defaultValue) {
        boolean useDefaultValue = false;
        TokenQueue tokenQueue = new TokenQueue(exp);
        while (true) {
            tokenQueue.consumeTo("$");
            if (tokenQueue.isEmpty()) {
                return exp;
            }
            tokenQueue.advance();
            if (!tokenQueue.matches("{")) {
                continue;
            }
            int start = tokenQueue.nowPosition();
            String subExp = tokenQueue.chompBalanced('{', '}');
            if (subExp == null) {
                return exp;
            }
            Object subProperty = findProperty(subExp.trim());
            if (subProperty == null
                    || StringUtils.isBlank(subProperty.toString())) {
                if (useDefaultValue) {
                    subProperty = "";
                } else {
                    subProperty = defaultValue;
                    useDefaultValue = true;
                }
            }
            exp = exp.substring(0, start - 1) + subProperty + exp.substring(tokenQueue.nowPosition());
            tokenQueue = new TokenQueue(exp);
        }
    }

    private void injectValue(Field field, String key, String defaultValue) {
        Object property = findProperty(key);
        if (property == null) {
            property = defaultValue;
        }
        if (property instanceof String) {
            property = invokeExpression((String) property, defaultValue);
        }
        Object value = com.alibaba.fastjson.util.TypeUtils.cast(property, field.getType(), ParserConfig.getGlobalInstance());
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(null, value);
        } catch (Throwable throwable) {
            throw new IllegalStateException("failed to set up config properties");
        }

    }

    @Resource
    private PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer;


    @Target({FIELD})
    @Retention(RUNTIME)
    private @interface ThanosConfig {
        String value();

        String defaultValue() default "";
    }

    @Target({TYPE})
    @Retention(RUNTIME)
    @interface ConfigPath {
        String value() default "";
    }
}
