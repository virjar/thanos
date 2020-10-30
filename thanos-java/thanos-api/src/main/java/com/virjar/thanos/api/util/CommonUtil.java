package com.virjar.thanos.api.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.virjar.thanos.api.AutoParam;
import com.virjar.thanos.api.AutoResource;
import com.virjar.thanos.api.GrabProcessor;
import com.virjar.thanos.api.bean.GrabParam;
import com.virjar.thanos.api.bean.GrabResult;
import com.virjar.thanos.api.bean.ResourceMeta;
import com.virjar.thanos.api.interfaces.GrabResultParser;
import com.virjar.thanos.api.services.ResourceServices;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonUtil {
    private static String paramKey(AutoParam param, Field field) {
        String value = param.value();
        if (StringUtils.isBlank(value)) {
            value = field.getName();
        }
        return value;
    }

    private static Set<String> parseResourceDeclare(GrabProcessor grabProcessor) {
        Set<String> ret = Sets.newHashSet();
        String resourceDeclare = grabProcessor.config().getResourceDeclare();
        if (StringUtils.isNotBlank(resourceDeclare)) {
            ret.addAll(Lists.newLinkedList(Splitter.on(",").omitEmptyStrings()
                    .trimResults().split(resourceDeclare)));
        }

        for (Field field : grabProcessor.getClass().getDeclaredFields()) {
            AutoResource grabResource = field.getAnnotation(AutoResource.class);
            if (grabResource == null) {
                continue;
            }
            String resourceKey = grabResource.value();
            if (StringUtils.isNotBlank(resourceKey)) {
                ret.add(resourceKey.trim());
            } else {
                ret.add(field.getName());
            }
        }
        return ret;
    }

    private static void injectResource(AutoResource grabResource, GrabProcessor grabProcessor, Field field, GrabParam grabParam) {
        if (grabResource == null) {
            return;
        }
        String resourceKey = grabResource.value();
        if (StringUtils.isBlank(resourceKey)) {
            resourceKey = field.getName();
        }
        resourceKey = resourceKey.trim();

        ResourceMeta resourceMeta = grabParam.getResource(resourceKey);
        if (resourceMeta == null) {
            throw new IllegalStateException("can not find resource : " + resourceKey);
        }

        String jsonContent = resourceMeta.getField();
        JSONObject jsonObject = JSONObject.parseObject(jsonContent);
        Object castValue = TypeUtils.cast(jsonObject, field.getType(), ParserConfig.getGlobalInstance());
        ReflectUtil.setFieldValue(grabProcessor, field.getName(), castValue);
    }


    private static void injectParam(AutoParam param, GrabProcessor grabProcessor, Field field, GrabParam grabParam) {
        if (param == null) {
            return;
        }
        if (field.getType().equals(GrabParam.class)) {
            ReflectUtil.setFieldValue(grabProcessor, field.getName(), grabParam);
            return;
        }
        Object value = grabParam.getParam().get(paramKey(param, field));
        if (value == null) {
            if (!param.nullable() && StringUtils.isBlank(param.defaultValue())) {
                throw new IllegalArgumentException("the param: {" + paramKey(param, field) + "} not presented!!");
            }
            value = param.defaultValue();
            if (StringUtils.isBlank(value.toString())) {
                //允许为空，且默认值为空
                return;
            }
        }
        if (value instanceof String && StringUtils.isNotBlank(param.pattern()) && !value.toString().matches(param.pattern())) {
            throw new IllegalArgumentException("the param: {" + paramKey(param, field) + "}  pattern match failed! value:{" + value + "}");
        }

        Object castValue = TypeUtils.cast(value, field.getType(), ParserConfig.getGlobalInstance());
        ReflectUtil.setFieldValue(grabProcessor, field.getName(), castValue);
    }

    public static GrabResult callProcess(GrabProcessor grabProcessor, GrabParam grabParam, ResourceServices resourceServices) {
        Map<String, ResourceMeta> bindResource = grabParam.getBindResource();
        if (bindResource == null) {
            bindResource = new HashMap<>();
            grabParam.setBindResource(bindResource);
        }

        for (String resource : parseResourceDeclare(grabProcessor)) {
            if (resource.contains(":")) {
                resource = resource.substring(0, resource.indexOf(":"));
            }
            ResourceMeta existedResourceMeta = grabParam.getBindResource().get(resource);
            if (existedResourceMeta != null) {
                //如果已经分配了资源，那么不需要再次分配。可能在测试的时候为了测试资源是否被封。
                //所以使用的是特定资源，而非系统自动分配
                continue;
            }
            ResourceMeta resourceMeta = resourceServices.allocate(resource, -1);
            if (resourceMeta == null) {
                return GrabResult.failed("resource allocate failed: " + resource);
            }
            grabParam.getBindResource().put(resource, resourceMeta);
        }


        for (Field field : grabProcessor.getClass().getDeclaredFields()) {
            injectParam(field.getAnnotation(AutoParam.class), grabProcessor, field, grabParam);
            injectResource(field.getAnnotation(AutoResource.class), grabProcessor, field, grabParam);
        }

        GrabResult result = grabProcessor.process(grabParam);
        if (grabProcessor instanceof GrabResultParser && result.getErrorCode() == 0) {
            List<JSONObject> objectList = ((GrabResultParser) grabProcessor).parse(grabParam, CommonUtil.transformToString(result.getContent()));
            result.setParsedContent(objectList);
        }
        return result;
    }

    public static String transformToString(Object obj) {
        if (obj == null) {
            return "success with empty result";
        }

        if (obj instanceof InputStream) {
            try {
                return IOUtils.toString((InputStream) obj, StandardCharsets.UTF_8);
            } catch (IOException e) {
                //ignore
            }
        }
        if (obj instanceof CharSequence) {
            return obj.toString();
        }
        return JSONObject.toJSONString(obj);
    }

    public static int trimToInt(Integer value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static int trimToInt(Integer value) {
        return trimToInt(value, 0);
    }
}
