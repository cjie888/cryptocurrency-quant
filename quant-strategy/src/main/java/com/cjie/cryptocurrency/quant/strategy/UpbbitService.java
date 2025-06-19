package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.mapper.UpbitNoticeMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.UpbitNotice;
import com.cjie.cryptocurrency.quant.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class UpbbitService {

    @Autowired
    private UpbitNoticeMapper upbitNoticeMapper;

    @Autowired
    @Qualifier("telegramMessageServiceImpl")
    private MessageService messageService;

    public void monitorNewCoin() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Referer", "https://www.upbit.com");
//        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url =  "https://api-manager.upbit.com/api/v1/announcements?os=moweb&page=1&per_page=20&category=trade";
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);
        log.info(url);
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);

        JSONObject upbitResult =  JSON.parseObject(body);
        if (upbitResult == null || !upbitResult.containsKey("success") || !upbitResult.getBoolean("success")) {
            return;
        }
        JSONArray noticeArray  = upbitResult.getJSONObject("data").getJSONArray("notices");
        for (Object object : noticeArray) {
            if (!(object instanceof  JSONObject)) {
                continue;
            }
            JSONObject noticeObject = (JSONObject) object;
            UpbitNotice upbitNotice = new UpbitNotice();
            upbitNotice.setUpbitId(noticeObject.getLong("id"));
            upbitNotice.setTitle(noticeObject.getString("title"));
            upbitNotice.setListedAt(noticeObject.getDate("listed_at"));
            upbitNotice.setFirstListedAt(noticeObject.getDate("first_listed_at"));
            upbitNotice.setNeedNewBadge(noticeObject.getBoolean("need_new_badge"));
            upbitNotice.setNeedUpdateBadge(noticeObject.getBoolean("need_update_badge"));

            upbitNotice.setCreateTime(new Date());
//            System.out.println(JSON.toJSONString(upbitNotice));

            UpbitNotice lastUpbitNotice = upbitNoticeMapper.selectByUpbitId(upbitNotice.getUpbitId());
            if (lastUpbitNotice == null) {
                upbitNoticeMapper.insert(upbitNotice);
                messageService.sendNoticeMessage("Upbit上新币", upbitNotice.getTitle());
            }
        }
    }
}
