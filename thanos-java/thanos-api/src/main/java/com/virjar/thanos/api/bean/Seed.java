package com.virjar.thanos.api.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 任务种子，其中seedId用于唯一标记这个种子任务。
 * param表示这个种子任务需要的附加数据，param和业务有关，但不代表唯一性
 */
@Data
@AllArgsConstructor
public class Seed {
    public String seedId;
    public JSONObject param;
}
