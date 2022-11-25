package com.statsmind.commons.web;

public class RestResponseCode {

    /**
     * 1000 代表调用成功，1001-1999 都是平台保留的异常信息
     */
    public static final RestResponseCode SUCCESS = new RestResponseCode(1000, "调用成功", true);

    public static final RestResponseCode UNKNOWN_ERROR = new RestResponseCode(1100, "系统错误，请联系技术支持或稍后重试");

    /**
     * 权限，认证相关错误
     */
    public static final RestResponseCode PERMISSION_DENIED = new RestResponseCode(1200, "无权执行此操作");

    public static final RestResponseCode NO_AUTH = new RestResponseCode(1201, "用户没有认证");
    public static final RestResponseCode INVALID_AUTH = new RestResponseCode(1202, "用户认证信息错误");

    public static final RestResponseCode NO_LOGIN = new RestResponseCode(1203, "用户未登录验证");

    public static final RestResponseCode AUTH_EXPIRED = new RestResponseCode(1204, "授权信息已过期");

    /**
     * 数据验证错误
     */
    public static final RestResponseCode VALIDATION_ERROR = new RestResponseCode(1300, "数据验证错误，请联系技术支持或稍后重试");
    public static final RestResponseCode METHOD_ARGUMENT_NOT_VALID = new RestResponseCode(1301, "方法参数未通过验证异常，请联系技术支持或稍后重试");
    public static final RestResponseCode MISSING_SERVLET_REQUEST_PARAMETER = new RestResponseCode(1302, "缺失必要参数异常，请联系技术支持或稍后重试");
    public static final RestResponseCode DUPLICATED_RESOURCE = new RestResponseCode(1303, "资源重复");
    public static final RestResponseCode INVALID_RESOURCE = new RestResponseCode(1304, "资源不存在");


    /**
     * 数据库相关
     */
    public static final RestResponseCode CONSTRAINT_VIOLATION = new RestResponseCode(1400, "数据库对象更新错误，请联系技术支持或稍后重试");
    public static final RestResponseCode TRANSACTION_SYSTEM = new RestResponseCode(1401, "数据库事务错误，请联系技术支持或稍后重试");

    /**
     * 其他
     */
    public static final RestResponseCode ILLEGAL_STATE = new RestResponseCode(1500, "违法访问异常，请联系技术支持或稍后重试");
    public static final RestResponseCode NULL_POINTER = new RestResponseCode(1501, "空对象异常，请联系技术支持或稍后重试");


    private Integer code;

    private String description;

    private boolean success;

    public RestResponseCode(Integer code, String description) {
        this(code, description, false);
    }

    public RestResponseCode(Integer code, String description, boolean success) {
        this.code = code;
        this.description = description;
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccess() {
        return success;
    }
}
