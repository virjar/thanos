package com.virjar.thanos.api.test.defaultbean;


import com.virjar.thanos.api.GrabLogger;
import com.virjar.thanos.api.bean.Seed;
import com.virjar.thanos.api.services.TaskPushService;
import com.virjar.thanos.api.test.TestBean;

@TestBean
public class TestTaskPushService implements TaskPushService {
    @Override
    public void pushNewSeed(String crawlerId, Seed seed) {
        //do nothing on test environment
        GrabLogger.info("ignore push task on test environment ");
    }
}
