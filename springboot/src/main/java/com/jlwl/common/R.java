package com.jlwl.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应封装。
 * code: 0=成功, 401=未登录, 403=无权限, 500=服务器错误
 */
@Data
public class R<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public static <T> R<T> ok() { return ok(null); }
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 0;
        r.msg = "success";
        r.data = data;
        return r;
    }
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        return r;
    }
    public static <T> R<T> unauthorized() { return fail(401, "未登录或登录已过期"); }
    public static <T> R<T> forbidden() { return fail(403, "无权限"); }
}
