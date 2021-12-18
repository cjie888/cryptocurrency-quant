package com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.param;

public class SetPositionMode {

    @Override
    public String toString() {
        return "SetPositionMode{" +
                "posMode='" + posMode + '\'' +
                '}';
    }

    public String getPosMode() {
        return posMode;
    }

    public void setPosMode(String posMode) {
        this.posMode = posMode;
    }

    private String posMode;


}
