package com.virjar.thanos.api.test;

import com.virjar.thanos.api.GrabProcessor;
import com.virjar.thanos.api.bean.GrabParam;
import com.virjar.thanos.api.bean.GrabResult;
import com.virjar.thanos.api.test.defaultbean.TestResourceServices;
import com.virjar.thanos.api.util.CommonUtil;

public class GrabTestRunner {
    public static GrabResult run(Class<? extends GrabProcessor> grabProcessorClass, GrabParam grabParam) {
        try {
            return run(grabProcessorClass.newInstance(), grabParam);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static GrabResult run(GrabProcessor grabProcessor, GrabParam grabParam) {
        //fist step ensure all spring bean injected
        TestDependencyRegistry.injectDependency(grabProcessor);
        return CommonUtil.callProcess(grabProcessor, grabParam, new TestResourceServices());
    }


}
