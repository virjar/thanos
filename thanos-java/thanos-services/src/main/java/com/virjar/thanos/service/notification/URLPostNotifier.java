package com.virjar.thanos.service.notification;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@ConfigKeys(value = {URLPostNotifier.paramURL}, type = URLPostNotifier.NOTIFIER_KEY)
public class URLPostNotifier implements INotifier {

    static final String paramURL = "URL";
    static final String NOTIFIER_KEY = "URL_POST";

    private String targetUrl = null;
    private CloseableHttpAsyncClient client;

    @Override
    public void doConfig(Map<String, String> config) {
        targetUrl = config.get(paramURL);
        client = HttpAsyncClients.createDefault();
        client.start();
    }

    @Override
    public void doNotify(GrabFinishMessage grabFinishMessage) {
        HttpPost post = new HttpPost(targetUrl);
        post.addHeader("Content-type", "application/json; charset=utf-8");
        post.setHeader("Accept", "application/json");
        post.setEntity(new StringEntity(JSONObject.toJSONString(grabFinishMessage), ContentType.APPLICATION_JSON));
        client.execute(post, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    String response = IOUtils.toString(result.getEntity().getContent(), StandardCharsets.UTF_8);
                    log.info("notify for url:{} response:{} ", targetUrl, response);
                } catch (IOException e) {
                    log.error("read notify response failed for url:{} crawler:{} task:{}"
                            , targetUrl, grabFinishMessage.getCrawlerId(), grabFinishMessage.getSeedId()
                            , e);
                }
            }

            @Override
            public void failed(Exception ex) {
                //todo monitor
                log.error("failed to notify grab message for url:{} crawler:{} task:{}"
                        , targetUrl, grabFinishMessage.getCrawlerId(), grabFinishMessage.getSeedId()
                        , ex);
            }

            @Override
            public void cancelled() {
                //not happen
            }
        });
    }

    @Override
    public void destroy() {
        try {
            client.close();
        } catch (IOException e) {
            log.error("failed to close http client");
        }
    }
}
