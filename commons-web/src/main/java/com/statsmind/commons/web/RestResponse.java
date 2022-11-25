package com.statsmind.commons.web;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * public RestResponse<Integer> actionAddStudyForm() {}
 *
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class RestResponse<T> implements Serializable {
    /**
     * 错误码，1000-9999 四位数字。1000 为正确应答，1001-1099 保留为其他正确的应答, 1100-1999都是错误
     */
    private Integer msgCode = RestResponseCode.SUCCESS.getCode();

    /**
     * 消息的描述，也可以是正确的消息，不能拿这个判断返回是否错误，要看 success,
     */
    private String msgInfo = "";

    private Object msgData = null;

    /**
     * msgInfo 是给用户看的，这个错误信息是给研发人员看的
     */
    private String msgDetail = null;

    private boolean success = true;

    private T data;

    public static <T> RestResponse<T> ok(T data) {
        RestResponse<T> response = new RestResponse<>();
        response.setData(data);
        response.setMsgInfo(RestResponseCode.SUCCESS.getDescription());

        return response;
    }

    public static <S, T> RestResponse<List<T>> ok(List<S> data, Function<S, T> mapper) {
        return ok(data.stream().map(mapper).collect(Collectors.toList()));
    }

    public static <T> RestResponse<T> fail(RestResponseCode responseCode) {
        return fail(responseCode, null, "");
    }

    public static <T> RestResponse<T> fail(RestResponseCode responseCode, T msgData, String msgDetail) {
        return fail(responseCode.getCode(), responseCode.getDescription(), msgData, "");
    }

    public static <T> RestResponse<T> fail(RestResponseCode responseCode, String msgDetail) {
        return fail(responseCode.getCode(), responseCode.getDescription(), null, "");
    }

    public static <T> RestResponse<T> fail(Integer msgCode, String msgInfo, T msgData, String msgDetail) {
        RestResponse<T> response = new RestResponse<>();
        response.setMsgCode(msgCode);
        response.setMsgInfo(msgInfo);
        response.setMsgData(msgData);
        response.setMsgDetail(msgDetail);
        response.setSuccess(false);

        return response;
    }

    public static <T> RestResponse<T> fail(String msgInfo) {
        return fail(RestResponseCode.UNKNOWN_ERROR.getCode(), msgInfo, null, "");
    }

    public static RestResponse<?> fail(Throwable ex, String msgInfo) {
        RestResponse<?> response = fail(ex);
        response.setMsgInfo(msgInfo);

        return response;
    }

    public static RestResponse<?> fail(Throwable ex) {
        RestResponse response;

        if (ex instanceof RestException) {
            response = handleRestException((RestException) ex);
        } else if (ex instanceof ValidationException) {
            response = handleValidationException((ValidationException) ex);
        } else if (ex instanceof ConstraintViolationException) {
            response = handleConstraintViolationException((ConstraintViolationException)ex);
        } else if (ex instanceof IllegalStateException) {
            response = handleNullPointerException((NullPointerException) ex);
        } else if (ex instanceof MethodArgumentNotValidException) {
            response = handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex);
        } else if (ex instanceof MissingServletRequestParameterException) {
            response = handleMissingServletRequestParameterException((MissingServletRequestParameterException) ex);
        } else if (ex instanceof TransactionSystemException) {
            response = handleTransactionSystemException((TransactionSystemException) ex);
        } else {
            response = handleThrowable(ex);
        }

        String message = getRootCause(ex).getMessage();
        if (!StringUtils.equalsAnyIgnoreCase(response.getMsgInfo(), message)) {
            response.setMsgDetail(message);
        }

        return response;
    }

    private static Throwable getRootCause(Throwable throwable) {
        List<Throwable> chain = new ArrayList<Throwable>(10);
        chain.add(throwable);

        Throwable cause;
        for (int i = 0; i < 10; ++i) {
            cause = throwable.getCause();
            if (cause == null || chain.contains(cause)) {
                return throwable;
            }

            chain.add(cause);
            throwable = cause;
        }

        return throwable;
    }

    protected static RestResponse<?> handleRestException(RestException ex) {
        return RestResponse.fail(ex.getMsgCode(), ex.getMsgInfo(), ex.getMsgData(), "");
    }

    protected static RestResponse<?> handleValidationException(ValidationException ex) {
        return RestResponse.fail(RestResponseCode.VALIDATION_ERROR, ex.getMessage());
    }

    protected static RestResponse<?> handleConstraintViolationException(ConstraintViolationException ex) {
        return RestResponse.fail(
            RestResponseCode.VALIDATION_ERROR,
            buildValidationErrors(ex.getConstraintViolations()),
            ex.getMessage()
        );
    }

    protected static RestResponse<?> handleIllegalStateException(IllegalStateException ex) {
        return RestResponse.fail(RestResponseCode.ILLEGAL_STATE, ex.getMessage());
    }

    protected static RestResponse<?> handleNullPointerException(NullPointerException ex) {
        /**
         * 此异常会上报至日志告警系统
         *
         * 通常业务系统应该能处理空指针数据，并且以 RestException 抛出
         */
        return RestResponse.fail(RestResponseCode.NULL_POINTER, ex.getMessage());
    }

    protected static RestResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getAllErrors();
        if (errors == null || errors.size() == 0) {
            return RestResponse.fail(RestResponseCode.VALIDATION_ERROR, ex.getMessage());
        }

        List<ValidationError> validationErrors = new ArrayList<>();
        String firstErrorMessage = null;

        for (ObjectError error : errors) {
            BeanWrapper beanWrapper = new BeanWrapperImpl(error);
            try {
                String field = (String) beanWrapper.getPropertyValue("field");
                String message = (String) beanWrapper.getPropertyValue("defaultMessage");
                firstErrorMessage = message;

                validationErrors.add(new ValidationError().setField(field).setMessage(message));
            } catch (Exception exx) {}
        }

        if (StringUtils.isBlank(firstErrorMessage)) {
            firstErrorMessage = ex.getMessage();
        }

        return RestResponse.fail(
            RestResponseCode.VALIDATION_ERROR,
            validationErrors,
            firstErrorMessage
        );
    }

    protected static RestResponse<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        /**
         * 无需上报
         */
        return RestResponse.fail(RestResponseCode.MISSING_SERVLET_REQUEST_PARAMETER, ex.getMessage());
    }

    protected static RestResponse<?> handleTransactionSystemException(TransactionSystemException ex) {
        /**
         * 此异常会上报至日志告警系统
         */
        return RestResponse.fail(RestResponseCode.TRANSACTION_SYSTEM, ex.getMessage());
    }

    protected static RestResponse<?> handleThrowable(Throwable ex) {
        /**
         * 此异常会上报至日志告警系统
         */
        return RestResponse.fail(RestResponseCode.UNKNOWN_ERROR, ex.getMessage());
    }

    private static List<ValidationError> buildValidationErrors(
        Set<ConstraintViolation<?>> violations) {
        return violations.
            stream().
            map(violation ->
                new ValidationError().
                    setField(
                        StreamSupport.stream(
                                violation.getPropertyPath().spliterator(), false).
                            reduce((first, second) -> second).
                            orElse(null).
                            toString()
                    ).
                    setMessage(violation.getMessage())).
            collect(toList());
    }

    @Override
    public String toString() {
        return msgCode + ":" + msgInfo;
    }

    @Data
    @Accessors(chain = true)
    public static class ValidationError {
        private String field;
        private String message;
    }
}
