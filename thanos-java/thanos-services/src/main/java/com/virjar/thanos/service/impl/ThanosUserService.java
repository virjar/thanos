package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.virjar.thanos.entity.ThanosUser;
import com.virjar.thanos.mapper.ThanosUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 系统用户，需要有账号才能进入本系统 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-11-03
 */
@Service
public class ThanosUserService extends ServiceImpl<ThanosUserMapper, ThanosUser> implements IService<ThanosUser> {

    public ThanosUser checkLogin(String token) {
        ThanosUser loginToken = getOne(new QueryWrapper<ThanosUser>().eq(ThanosUser.LOGIN_TOKEN, token));
        if (loginToken == null) {
            return null;
        }
        if (loginToken.getLastActive().plusHours(4).isBefore(LocalDateTime.now())) {
            // 登陆过期
            return null;
        }

        loginToken.setLastActive(LocalDateTime.now());
        updateById(loginToken);
        return loginToken;
    }

}
