package com.cjie.cryptocurrency.quant.api.okex.bean.swap.result;

import java.util.List;

public class ApiOrderResultVO {

    private List<PerOrderResult> order_info;
    private String result;

    public ApiOrderResultVO() {
    }

    public ApiOrderResultVO(List<PerOrderResult> order_info, String result) {
        this.order_info = order_info;
        this.result = result;
    }

    public List<PerOrderResult> getOrder_info() {
        return order_info;
    }

    public void setOrder_info(List<PerOrderResult> order_info) {
        this.order_info = order_info;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    public static class PerOrderResult {

        String order_id;
        String state;

        String type;
        String price;
        String client_oid;
        String error_code;
        String error_message;

        String timestamp;

        public PerOrderResult() {
        }

        public PerOrderResult(String order_id, String client_oid, String error_code, String error_message) {
            this.order_id = order_id;
            this.client_oid = client_oid;
            this.error_code = error_code;
            this.error_message = error_message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getOrder_id() {
            return order_id;
        }

        public void setOrder_id(String order_id) {
            this.order_id = order_id;
        }

        public String getClient_oid() {
            return client_oid;
        }

        public void setClient_oid(String client_oid) {
            this.client_oid = client_oid;
        }

        public String getError_code() {
            return error_code;
        }

        public void setError_code(String error_code) {
            this.error_code = error_code;
        }

        public String getError_message() {
            return error_message;
        }

        public void setError_message(String error_message) {
            this.error_message = error_message;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
