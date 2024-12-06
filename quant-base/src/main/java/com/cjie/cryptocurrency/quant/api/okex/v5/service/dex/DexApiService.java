package com.cjie.cryptocurrency.quant.api.okex.v5.service.dex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DexApiService {
    public BigDecimal getPrice(String chainId, String tokenAddress) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Referer", "https://www.okx.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");

        JSONObject params = new JSONObject();
        params.put("chainIndex", chainId);
        params.put("tokenAddress", tokenAddress);

        String requestBody = JSON.toJSONString(params);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody , headers);

        String urlString = "http://localhost:3000/prices/getPrice";

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String body = client.postForObject(urlString, requestEntity, String.class);
//        String body = response.getBody();
        log.info(body);
        JSONObject result = JSON.parseObject(body);
        if ("0".equals(result.getString("code"))) {
            try {
                JSONObject priceData = result.getJSONArray("data").getJSONObject(0);
                return priceData.getBigDecimal("price");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public JSONObject getTokenDetail(String chainId, String tokenAddress) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Referer", "https://www.okx.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");

        JSONObject params = new JSONObject();
        params.put("chainIndex", chainId);
        params.put("tokenAddress", tokenAddress);

        String requestBody = JSON.toJSONString(params);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody , headers);

        String urlString = "http://localhost:3000/prices/getTokenDetail";

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String body = client.postForObject(urlString, requestEntity, String.class);
//        String body = response.getBody();
        log.info(body);
        JSONObject result = JSON.parseObject(body);
        if ("0".equals(result.getString("code"))) {
            try {
                JSONObject tokenData = result.getJSONArray("data").getJSONObject(0);
                return tokenData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public void sell(String chainId, String tokenAddress, String amount) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Referer", "https://www.okx.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");

        JSONObject params = new JSONObject();
        params.put("chainIndex", chainId);
        params.put("tokenAddress", tokenAddress);
        params.put("amount", amount);

        String requestBody = JSON.toJSONString(params);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody , headers);

        String urlString = "http://localhost:3000/trade/sell";

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String body = client.postForObject(urlString, requestEntity, String.class);
//        String body = response.getBody();
        log.info(body);
    }

    public void buy(String chainId, String tokenAddress, String amount) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Referer", "https://www.okx.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");

        JSONObject params = new JSONObject();
        params.put("chainIndex", chainId);
        params.put("tokenAddress", tokenAddress);
        params.put("amount", amount);

        String requestBody = JSON.toJSONString(params);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody , headers);

        String urlString = "http://localhost:3000/trade/buy";

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String body = client.postForObject(urlString, requestEntity, String.class);
//        String body = response.getBody();
        log.info(body);
    }

}
