package com.virjar.thanos.service.notification;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.virjar.thanos.api.util.Md5Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;

@Slf4j
@ConfigKeys(value = {
        AliyunRocketMqNotifier.KEY_ACCESS_KEY,
        AliyunRocketMqNotifier.KEY_SECRET_KEY,
        AliyunRocketMqNotifier.KEY_NAME_SRV_ADDR,
        AliyunRocketMqNotifier.KEY_TOPIC},
        type = AliyunRocketMqNotifier.NOTIFIER_KEY
)
public class AliyunRocketMqNotifier implements INotifier {

    static final String KEY_ACCESS_KEY = "accessKey";
    static final String KEY_SECRET_KEY = "secretKey";
    static final String KEY_NAME_SRV_ADDR = "namesrvAddr";
    static final String KEY_TOPIC = "topic";

    static final String NOTIFIER_KEY = "AliyunRocketMq";

    private ProducerBean producerBean;
    private String mqTopic;

    // 消息体, 消息体长度默认不超过4M, 具体请参阅集群部署文档描述.
    // 如果消息过长，那么我们抹除原始数据，只保留解析数据
    private static final int maxMessageLength = 1 << 22;


    @Override
    public void doConfig(Map<String, String> config) {
        Properties properties = new Properties();
        // 鉴权用 AccessKey，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.AccessKey, config.get(KEY_ACCESS_KEY));
        // 鉴权用 SecretKey，在阿里云服务器管理控制台创建
        properties.put(PropertyKeyConst.SecretKey, config.get(KEY_SECRET_KEY));
        // 设置 TCP 接入域名
        properties.put(PropertyKeyConst.NAMESRV_ADDR, config.get(KEY_NAME_SRV_ADDR));
        producerBean = new ProducerBean();
        producerBean.setProperties(properties);
        producerBean.start();

        mqTopic = config.get(KEY_TOPIC);
    }

    @Override
    public void doNotify(GrabFinishMessage grabFinishMessage) {
        byte[] bytes = JSONObject.toJSONBytes(grabFinishMessage);
        if (bytes.length > maxMessageLength) {
            grabFinishMessage.setRawResponse(null);
            log.warn("4M limited for rocked mq, discard raw response data");
            bytes = JSONObject.toJSONBytes(grabFinishMessage);
        }


        Message message = new Message(mqTopic,
                grabFinishMessage.getCrawlerId(),
                grabFinishMessage.getCrawlerId() + "_" + Md5Utils.md5(
                        grabFinishMessage.getSeedId() + grabFinishMessage.getExecuteEndTime()
                ),
                bytes
        );
        producerBean.send(message);
    }

    @Override
    public void destroy() {
        producerBean.shutdown();
    }
}
