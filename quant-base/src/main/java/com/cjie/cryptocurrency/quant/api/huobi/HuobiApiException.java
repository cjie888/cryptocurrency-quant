package com.cjie.cryptocurrency.quant.api.huobi;


public class HuobiApiException extends Exception {

    public HuobiApiException() {

    }

    public HuobiApiException(HuobiApiError error) {
        super(error.toString());
    }

    public HuobiApiException(Throwable cause) {
        super(cause);
    }

    public HuobiApiException(String msg) {
        super(msg);
    }

    public HuobiApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
