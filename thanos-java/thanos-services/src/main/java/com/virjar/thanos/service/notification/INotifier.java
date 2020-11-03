package com.virjar.thanos.service.notification;

import java.util.Map;

/**
 * 抓取结果通知抽象，在抓取业务完成后。通过某个实现完成对上游系统数据推送工作。<br>
 * 一般来说，实现需要被 {@link ConfigKeys}修饰
 */
public interface INotifier {


    /**
     * 该实现需要的动态参数。比如mq类型，将会传递mq连接参数；URL post类型，将会传递对应url连接<br>
     * 请注意，具体那些参数。需要实现类 通过{@link ConfigKeys}描述。否则只会传递空Map
     *
     * @param config 配置内容。kv都是字符串。其他类型实现自行编解码处理
     * @see ConfigKeys
     */
    void doConfig(Map<String, String> config);

    /**
     * 通知实现
     *
     * @param grabFinishMessage 消息bean
     */
    void doNotify(GrabFinishMessage grabFinishMessage);

    /**
     * 系统关闭的时候，回收资源
     */
    void destroy();
}
