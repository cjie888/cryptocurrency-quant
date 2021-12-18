package com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param;

public class SetTheDisplayTypeOfGreeks {
    private String greeksType;

    public String getGreeksType() {
        return greeksType;
    }

    public void setGreeksType(String greeksType) {
        this.greeksType = greeksType;
    }

    @Override
    public String toString() {
        return "SetTheDisplayTypeOfGreeks{" +
                "greeksType='" + greeksType + '\'' +
                '}';
    }
}
