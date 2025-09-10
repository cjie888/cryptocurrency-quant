package com.cjie.cryptocurrency.quant.strategy.binance;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BinanceFundingRateChecker {
    private static final String API_URL = "https://fapi.binance.com/fapi/v1/premiumIndex";
    private static final double HIGH_FUNDING_RATE_THRESHOLD = 1; // 资金费率阈值（百分比）


    @Autowired
    @Qualifier("telegramMessageServiceImpl")
    private MessageService messageService;

    public void monitorHighFundingRatePairs() {
        try {
            List<String> highFundingRatePairs = getHighFundingRatePairs();
            System.out.println("资金费率较高的交易对：");
            StringBuilder sb = new StringBuilder();
            for (String pair : highFundingRatePairs) {
                sb.append(pair).append("\n");
            }
            messageService.sendMonitorMessage("binance资金费率较高的交易对", sb.toString());
        } catch (IOException e) {
            System.err.println("请求失败: " + e.getMessage());
        }
    }

    private static List<String> getHighFundingRatePairs() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        List<String> highFundingRatePairs = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String jsonData = response.body().string();
            JSONArray jsonArray = JSON.parseArray(jsonData);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String symbol = jsonObject.getString("symbol");
                double fundingRate = jsonObject.getDouble("lastFundingRate") * 100; // 转为百分比
                double markPrice = jsonObject.getDouble("markPrice");

                if (Math.abs(fundingRate) >= HIGH_FUNDING_RATE_THRESHOLD) {
                    highFundingRatePairs.add(
                            String.format("交易对: %s, 资金费率: %.4f%%, 价格:%.8f%", symbol, fundingRate, markPrice)
                    );
                }
            }
        }

        return highFundingRatePairs;
    }
}
