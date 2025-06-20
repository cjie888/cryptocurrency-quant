package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.mapper.UpbitNoticeMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.UpbitNotice;
import com.cjie.cryptocurrency.quant.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
import xyz.hexile.cloudflarescraper.CloudflareScraper;
import xyz.hexile.cloudflarescraper.CloudflareScraperBuilder;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class UpbbitService {

    @Autowired
    private UpbitNoticeMapper upbitNoticeMapper;

    @Autowired
    @Qualifier("telegramMessageServiceImpl")
    private MessageService messageService;

    public void monitorNewCoin() {
        try {
            log.info("Start to monitor upbit coin");
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true) // 自动处理重定向
                    .build();

            String url = "https://api-manager.upbit.com/api/v1/announcements?os=moweb&page=1&per_page=20&category=trade";
            // Create CloudflareScraper object
            CloudflareScraper cloudflareScraper = new CloudflareScraperBuilder(new URI(url))
                    .setConnectionTimeout(5000)
                    .setReadTimeout(5000)
                    .setChallengeDelay(4000) // At least 4000 milliseconds, otherwise Cloudflare won't give you a clearance cookie
                    .build();
            // Pass this cookies in your request
            Request.Builder requestBuilder = new Request.Builder().url(url) // 替换为目标网站
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5");
            // Check if site is protected by Cloudflare
            if (cloudflareScraper.connect()) {
                List<HttpCookie> cookies = cloudflareScraper.getCookies();
                for (HttpCookie cookie : cookies) {
                    log.info("Cookie:{}", cookie.toString());
                    requestBuilder.addHeader("Cookie", cookie.toString());
                }
            }

            // 构造请求，添加伪装的 User-Agent 头
            Request request = requestBuilder.build();
            String body = "";
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    body = response.body().string();
                    log.info("Response received: " + body);
                } else {
                    log.info("Request failed: " + response.code());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            log.info(body);

            JSONObject upbitResult = JSON.parseObject(body);
            if (upbitResult == null || !upbitResult.containsKey("success") || !upbitResult.getBoolean("success")) {
                return;
            }
            JSONArray noticeArray = upbitResult.getJSONObject("data").getJSONArray("notices");
            for (Object object : noticeArray) {
                if (!(object instanceof JSONObject)) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
