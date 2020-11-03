package com.virjar.thanos.service.engine;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.virjar.thanos.api.AutoParam;
import com.virjar.thanos.api.AutoResource;
import com.virjar.thanos.api.GrabProcessor;
import com.virjar.thanos.api.bean.*;
import com.virjar.thanos.api.interfaces.GrabResultParser;
import com.virjar.thanos.api.interfaces.SeedKeyResolver;
import com.virjar.thanos.api.interfaces.SpringContextAware;
import com.virjar.thanos.api.util.Md5Utils;
import com.virjar.thanos.api.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class GrabProcessorHandler {

    private final GrabProcessor rootGrabProcessor;
    private final Class<? extends GrabProcessor> processorClass;
    private final List<ParamAnnotationHolder> paramAnnotations = Lists.newArrayList();
    private final List<GrabResourceAnnotationHolder> grabResourceAnnotations = Lists.newArrayList();

    private static class GrabResourceAnnotationHolder {
        private final Field field;
        private final String resourceKey;

        public GrabResourceAnnotationHolder(AutoResource grabResource, Field field) {
            this.field = field;
            if (StringUtils.isNotBlank(grabResource.value())) {
                resourceKey = grabResource.value().trim();
            } else {
                resourceKey = field.getName();
            }
        }

        void bindResource(GrabParam grabParam, GrabProcessor grabProcessor) {
            ResourceMeta resourceMeta = grabParam.getResource(resourceKey);
            if (resourceMeta == null) {
                throw new IllegalStateException("can not inject grab resource: " + resourceKey);
            }

            JSONObject jsonObject = JSONObject.parseObject(resourceMeta.getField());
            Object castValue = TypeUtils.cast(jsonObject, field.getType(), ParserConfig.getGlobalInstance());
            ReflectUtil.setFieldValue(grabProcessor, field.getName(), castValue);
        }

    }

    private static class ParamAnnotationHolder {
        private final AutoParam param;
        private final Field field;
        private final Object val;
        private final Class<?> fieldType;
        private final Pattern pattern;


        public ParamAnnotationHolder(AutoParam param, Field field, Object val) {
            this.param = param;
            this.field = field;
            this.val = val;
            this.fieldType = field.getType();
            if (StringUtils.isNoneBlank(param.pattern())) {
                pattern = Pattern.compile(param.pattern());
            } else {
                pattern = null;
            }
        }

        String paramKey() {
            String value = param.value();
            if (StringUtils.isBlank(value)) {
                value = field.getName();
            }
            return value;
        }

        String defaultValue() {
            String defaultValue = param.defaultValue();
            if (StringUtils.isBlank(defaultValue) && val != null) {
                defaultValue = val.toString();
            }
            return defaultValue;
        }

        void bindParam(GrabParam grabParam, GrabProcessor grabProcessor) {
            if (fieldType.equals(GrabParam.class)) {
                ReflectUtil.setFieldValue(grabProcessor, field.getName(), grabParam);
                return;
            }
            Object value = grabParam.getParam().get(paramKey());
            if (value == null) {
                if (!param.nullable() && StringUtils.isBlank(param.defaultValue())) {
                    throw new IllegalArgumentException("the param: {" + paramKey() + "} not presented!!");
                }
                value = param.defaultValue();
                if (StringUtils.isBlank(value.toString())) {
                    //允许为空，切默认值为空
                    return;
                }
            }
            if (value instanceof String && pattern != null && !pattern.matcher(((String) value).toLowerCase()).matches()) {
                throw new IllegalArgumentException("the param: {" + paramKey() + "}  pattern match failed! value:{" + value + "}");
            }


            Object castValue = TypeUtils.cast(value, fieldType, ParserConfig.getGlobalInstance());
            ReflectUtil.setFieldValue(grabProcessor, field.getName(), castValue);
        }
    }

    GrabProcessorHandler(GrabProcessor rootGrabProcessor, Class<? extends GrabProcessor> processorClass) {
        callProcessorInit(rootGrabProcessor);
        this.rootGrabProcessor = rootGrabProcessor;
        this.processorClass = processorClass;
        parseProcessorMeta();
    }


    private void parseProcessorMeta() {
        //scan all annotations
        Field[] declaredFields = processorClass.getDeclaredFields();
        for (Field field : declaredFields) {
            AutoParam fieldAnnotation = field.getAnnotation(AutoParam.class);
            if (fieldAnnotation != null) {
                paramAnnotations.add(new ParamAnnotationHolder(fieldAnnotation, field, ReflectUtil.getFieldValue(rootGrabProcessor, field.getName())));
                continue;
            }

            AutoResource grabResourceAnnotation = field.getAnnotation(AutoResource.class);
            if (grabResourceAnnotation == null) {
                continue;
            }
            grabResourceAnnotations.add(new GrabResourceAnnotationHolder(grabResourceAnnotation, field));
        }
    }

    private void callProcessorInit(GrabProcessor grabProcessor) {
        SpringContextInjector.getInstance().injectDependency(grabProcessor);
        init4SpringContext(SpringContextInjector.getInstance().getApplicationContext());
    }

    private GrabProcessor createProcessor(GrabParam grabParam, boolean bindResource) {
        GrabProcessor grabProcessor;
        try {
            grabProcessor = processorClass.newInstance();
        } catch (Throwable e) {
            log.error("create processor failed", e);
            throw new IllegalStateException(e);
        }
        // inject spring beans
        callProcessorInit(grabProcessor);

        // auto inject grab parameter
        for (ParamAnnotationHolder param : paramAnnotations) {
            param.bindParam(grabParam, grabProcessor);
        }
        if (bindResource) {
            // auto inject grab resource
            for (GrabResourceAnnotationHolder resource : grabResourceAnnotations) {
                resource.bindResource(grabParam, grabProcessor);
            }
        }
        return grabProcessor;
    }


    public GrabResult process(GrabParam grabParam) {
        return createProcessor(grabParam, true).process(grabParam);
    }

    public CrawlerMeta config() {
        return rootGrabProcessor.config();
    }

    public Seed resolveSeedKey(JSONObject jsonObject) {
        if (rootGrabProcessor instanceof SeedKeyResolver) {
            SeedKeyResolver seedKeyResolver = (SeedKeyResolver) rootGrabProcessor;
            return seedKeyResolver.resolveSeedKey(jsonObject);
        }

        Object id = jsonObject.get("id");
        if (id == null) {
            id = jsonObject.get("seedId");
        }
        if (id == null) {
            id = jsonObject.get("key");
        }
        if (id != null) {
            return new Seed(String.valueOf(id), jsonObject);
        }
        return new Seed(Md5Utils.md5(jsonObject.toJSONString()), jsonObject);
    }


    public void init4SpringContext(ApplicationContext webApplicationContext) {
        if (rootGrabProcessor instanceof SpringContextAware) {
            ((SpringContextAware) rootGrabProcessor).init4SpringContext(webApplicationContext);
        }
    }

    public List<JSONObject> parse(GrabParam grabParam, String content) {
        if (content == null) {
            return Collections.emptyList();
        }
        if (rootGrabProcessor instanceof GrabResultParser) {
            //解析的时候也可能依赖参数确定解析方向，所以需要创建 processor
            GrabResultParser processor = (GrabResultParser) createProcessor(grabParam, false);
            return processor.parse(grabParam, content);
        }
        return Collections.emptyList();
    }

    /**
     * 资源声明可以在配置中声明，也可以在注解中声明。本方法合并两种声明内容
     *
     * @return 重构后的资源字符串，多个资源之间使用逗号分割
     */
    public String rebuildResourceDeclare() {
        Set<String> ret = Sets.newHashSet();
        String resourceDeclare = config().getResourceDeclare();
        if (StringUtils.isNotBlank(resourceDeclare)) {
            ret.addAll(Lists.newLinkedList(Splitter.on(",").omitEmptyStrings()
                    .trimResults().split(resourceDeclare)));
        }

        for (Field field : rootGrabProcessor.getClass().getDeclaredFields()) {
            AutoResource grabResource = field.getAnnotation(AutoResource.class);
            if (grabResource == null) {
                continue;
            }
            String resourceKey = grabResource.value();
            if (StringUtils.isNotBlank(resourceKey)) {
                resourceKey = resourceKey.trim();
            } else {
                resourceKey = field.getName();
            }
            ret.add(resourceKey + ":" + grabResource.expire());
        }
        return StringUtils.join(ret, ",");
    }
}

