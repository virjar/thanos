package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosUser;
import com.virjar.thanos.mapper.ThanosUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统用户，需要有账号才能进入本系统 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosUserService extends ServiceImpl<ThanosUserMapper, ThanosUser> implements IService<ThanosUser> {

}
