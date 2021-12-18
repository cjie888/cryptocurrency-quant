package com.cjie.cryptocurrency.quant.api.okex.v5.service.status.impl;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.client.APIClient;
import com.cjie.cryptocurrency.quant.api.okex.v5.config.APIConfiguration;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.status.StatusDataAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



public class StatusDataAPIServiceImpl implements StatusDataAPIService {

    private final APIClient client;
    private final StatusDataAPI statusDataAPI;

    public StatusDataAPIServiceImpl(final APIConfiguration config) {
        this.client = new APIClient(config);
        this.statusDataAPI = this.client.createService(StatusDataAPI.class);
    }

    //status
    @Override
    public JSONObject getStatus(String state) {
        return this.client.executeSync(this.statusDataAPI.getStatus(state));
    }
}
