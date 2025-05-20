package com.cjie.cryptocurrency.quant.service.impl;

import com.cjie.cryptocurrency.quant.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class TelegramMessageServiceImpl implements MessageService {

    @Value("${quant.message.telegram.token}")
    private String token;

    @Value("${quant.message.telegram.strategyToken}")
    private String strategyToken;


    @Value("${quant.message.telegram.chatId}")
    private String chatId;


    private void sendCommonMessage(String token, String title, String content) {
        MultiValueMap<String, String> headers = new HttpHeaders();

        headers.add("Referer", "https://www.okx.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");


        HttpEntity requestEntity = new HttpEntity<>(headers);



        String urlString = "https://api.telegram.org/bot" + token + "/sendMessage"+ "?chat_id=" + chatId + "&text=" + content;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(urlString, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
    }

    @Override
    public void sendMessage(String title, String content) {
        try {
            sendCommonMessage(token, title, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendStrategyMessage(String title, String content) {
        try {
            sendCommonMessage(strategyToken, title, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
