package com.cjie.commons.okex.open.api.exception;

/**
 * API Exception
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/8 19:59
 */
public class APIException extends RuntimeException {

    public APIException(String message) {
        super(message);
    }

    public APIException(Throwable cause) {
        super(cause);
    }

    public APIException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
