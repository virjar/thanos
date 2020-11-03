package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virjar.thanos.entity.ThanosOpLog;
import com.virjar.thanos.entity.ThanosUser;
import com.virjar.thanos.mapper.ThanosOpLogMapper;
import com.virjar.thanos.system.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志，方便用户操作溯源 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
@Slf4j
public class ThanosOpLogService extends ServiceImpl<ThanosOpLogMapper, ThanosOpLog> implements IService<ThanosOpLog> {

    public void log(String message) {
        if (StringUtils.isBlank(message)) {
            log.warn("save log with empty message", new Throwable());
            return;
        }
        ThanosUser sessionUser = LoginInterceptor.getSessionUser();
        String userName = "system";
        if (sessionUser != null) {
            userName = sessionUser.getAccount();
        }

        log.info("grab log system  user:{} message:{}", userName, message);

        ThanosOpLog grabOpLog = new ThanosOpLog();
        grabOpLog.setOpAccount(userName);
        grabOpLog.setCreateTime(LocalDateTime.now());
        grabOpLog.setMessage(message);
        try {
            save(grabOpLog);
        } catch (Exception e) {
            // 日志太长，进行截断
            grabOpLog.setMessage(StringUtils.left(grabOpLog.getMessage(), 512));
            save(grabOpLog);
        }
    }
}
