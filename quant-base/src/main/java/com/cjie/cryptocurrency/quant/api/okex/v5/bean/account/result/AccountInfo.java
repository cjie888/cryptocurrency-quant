package com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result;

import java.util.List;

public class AccountInfo {

    private List<AccountDetail> details;

    public List<AccountDetail> getDetails() {
        return details;
    }

    public void setDetails(List<AccountDetail> details) {
        this.details = details;
    }
}
