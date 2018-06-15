package com.cjie.cryptocurrency.quant.api.huobi.domain.resp;


import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiAccount;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HuobiAccountResp extends HuobiResp {

    @SerializedName("data")
    private List<HuobiAccount> accounts = new ArrayList<>();
}
