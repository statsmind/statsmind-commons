package com.statsmind.commons.web;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RestException extends RuntimeException {

    private Integer msgCode;

    private String msgInfo;

    private Throwable throwable;

    private Object msgData;


    public RestException(Integer msgCode, String msgInfo, Throwable throwable) {
        super(msgInfo);

        this.msgCode = msgCode;
        this.msgInfo = msgInfo;
        this.throwable = throwable;
    }

    public RestException(String msgInfo, Throwable throwable) {
        this(RestResponseCode.UNKNOWN_ERROR.getCode(), msgInfo, throwable);
    }

    public RestException(String msgInfo) {
        this(msgInfo, (Throwable) null);
    }


    public RestException(Throwable throwable) {
        this("系统错误，请联系技术支持或稍后重试", throwable);
    }

    public RestException(RestException e) {
        BeanUtils.copyProperties(e, this);
    }

    public RestException(RestResponseCode restResponseCode, String msgInfo, Throwable throwable) {
        this(restResponseCode.getCode(), StringUtils.isEmpty(msgInfo) ? restResponseCode.getDescription() : msgInfo, throwable);
    }

    public RestException(Integer msgCode, String msgInfo) {
        this(msgCode, msgInfo, null);
    }

    public RestException(RestResponseCode restResponseCode, String msgInfo) {
        this(restResponseCode, msgInfo, null);
    }

    public RestException(RestResponseCode restResponseCode) {
        this(restResponseCode, restResponseCode.getDescription(), null);
    }

    public Integer getMsgCode() {
        return msgCode;
    }

    public String getMsgInfo() {
        return msgInfo;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Object getMsgData() {
        return msgData;
    }

    public void setMsgData(Object msgData) {
        this.msgData = msgData;
    }

    public static class ValidationErrorBuilder {
        private List<RestResponse.ValidationError> validationErrors = new ArrayList<>();

        public ValidationErrorBuilder append(String field, String message) {
            this.validationErrors.add(new RestResponse.ValidationError()
                    .setField(field).setMessage(message));
            return this;
        }

        public RestException build() {
            RestException exception = new RestException(RestResponseCode.VALIDATION_ERROR);
            exception.setMsgData(this.validationErrors);
            return exception;
        }
    }
}
