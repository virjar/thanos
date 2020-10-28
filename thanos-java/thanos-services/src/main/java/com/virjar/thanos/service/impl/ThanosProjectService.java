package com.virjar.thanos.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.virjar.thanos.entity.ThanosProject;
import com.virjar.thanos.mapper.ThanosProjectMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 项目，爬虫数量变多之后进行分类 服务实现类
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@Service
public class ThanosProjectService extends ServiceImpl<ThanosProjectMapper, ThanosProject> implements IService<ThanosProject> {

}
