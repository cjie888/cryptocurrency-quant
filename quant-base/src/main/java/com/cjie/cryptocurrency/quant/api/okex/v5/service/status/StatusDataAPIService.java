package com.cjie.cryptocurrency.quant.api.okex.v5.service.status;

import com.alibaba.fastjson.JSONObject;

public interface StatusDataAPIService {

    //Status
    JSONObject getStatus(String state);
}
