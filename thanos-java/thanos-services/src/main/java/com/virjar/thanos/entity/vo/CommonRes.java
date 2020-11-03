package com.virjar.thanos.entity.vo;

import com.virjar.thanos.api.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by virjar on 2018/8/4.<br>
 * 前端返回统一数据结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonRes<T> {
    private int status = statusOK;
    private String message;
    private T data;

    public static final int statusOK = 0;
    public static final int statusError = -1;
    public static final int statusNeedLogin = -2;
    public static final int statusLoginExpire = -3;

    public static <T> CommonRes<T> success(T t) {
        CommonRes<T> ret = new CommonRes<>();
        ret.status = statusOK;
        ret.message = null;
        ret.data = t;
        return ret;

    }

    public static <T> CommonRes<T> of(T t) {
        if (t == null) {
            return failed("not exist");
        }
        return success(t);
    }

    @SuppressWarnings("unchecked")
    public <NT> CommonRes<NT> errorTransfer() {
        data = null;
        return (CommonRes<NT>) this;
    }

    public static <T> CommonRes<T> failed(String message) {
        return failed(statusError, message);
    }

    public static <T> CommonRes<T> failed(int status, String message) {
        CommonRes<T> ret = new CommonRes<>();
        ret.status = status;
        ret.message = message;
        return ret;
    }

    public static <T> CommonRes<T> failed(Exception exception) {
        return failed(statusError, CommonUtil.translateSimpleExceptionMessage(exception));
    }

    public boolean isOk() {
        return status == statusOK;
    }
}
