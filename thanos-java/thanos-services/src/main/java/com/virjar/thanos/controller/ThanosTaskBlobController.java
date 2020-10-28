package com.virjar.thanos.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 爬虫任务的数据区域，由于数据内容会比较大。所以单独抽离出来，避免任务调度的时候影响性能 前端控制器
 * </p>
 *
 * @author virar
 * @since 2020-10-28
 */
@RestController
@RequestMapping("/api/thanos-task-blob")
public class ThanosTaskBlobController {

}
