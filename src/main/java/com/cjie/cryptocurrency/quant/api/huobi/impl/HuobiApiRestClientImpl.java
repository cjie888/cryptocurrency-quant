package com.cjie.cryptocurrency.quant.api.huobi.impl;

import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiError;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiException;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.constant.HuobiConst;
import com.cjie.cryptocurrency.quant.api.huobi.domain.*;
import com.cjie.cryptocurrency.quant.api.huobi.domain.resp.*;
import com.cjie.cryptocurrency.quant.api.huobi.util.HuobiUtil;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HuobiApiRestClientImpl implements HuobiApiRestClient {

    private static final String METHOD_GET = "GET";

    private static final String METHOD_POST = "POST";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient.Builder builder = new OkHttpClient.Builder();

    static {
        builder.sslSocketFactory(getSSLSocketFactory());
        builder.hostnameVerifier(new HostnameVerifier() {

            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
    }

    private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    private OkHttpClient httpClient;

    private String apiKey = null;

    private String secret = null;

    private List<HuobiAccount> accountsCache = null;

    private final Object accountsCacheLock = new Object();

    private static SSLSocketFactory getSSLSocketFactory() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return sslSocketFactory;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    public HuobiApiRestClientImpl(String apiKey, String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
        this.httpClient = builder.build();
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfTime.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private HuobiApiError parseError(HuobiResp resp) {
        if (resp.getStatus().equals("ok")) {
            return null;
        }
        HuobiApiError err = new HuobiApiError();
        err.setErrCode(resp.getErrCode());
        err.setErrMsg(resp.getErrMsg());
        return err;
    }

    private <T extends HuobiResp> T executeReq(Request request, Class<T> clazz) throws HuobiApiException {
        Call call = httpClient.newCall(request);
        try {
            Response response = call.execute();
            ResponseBody body;
            if (response.isSuccessful()) {
                body = response.body();
                if (body != null) {
                    String json = body.string();
                    T t = HuobiUtil.fromJson(json, clazz);
                    HuobiApiError err = parseError(t);
                    if (err != null) {
                        throw new HuobiApiException(err);
                    } else {
                        return t;
                    }
                } else {
                    throw new HuobiApiException("body error");
                }

            } else {
                body = response.body();
                if (body != null) {
                    HuobiResp resp = HuobiUtil.fromJson(body.string(), HuobiResp.class);
                    HuobiApiError err = parseError(resp);
                    throw new HuobiApiException(err);
                }
                throw new HuobiApiException(String.format("execute error with url %s", request.url().url().toString()));
            }
        } catch (IOException e) {
            throw new HuobiApiException(e);
        }

    }


    private Request buildRequest(String url, Object post) {

        Request.Builder builder = new Request.Builder().url(url);
        Map<String, String> map = new HashMap<>();
        map.put("Content-type", "application/x-www-form-urlencoded");
        map.put("Accept", "application/json");
        map.put("Accept-Language", "zh-CN");
        map.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");

        for (Map.Entry<String, String> e : map.entrySet()) {
            builder.addHeader(e.getKey(), e.getValue());
        }
        if (post != null) {
            String json = HuobiUtil.toJson(post);
            RequestBody body = RequestBody.create(JSON, json);
            return builder.post(body).build();
        }

        return builder.build();
    }

    private <T extends HuobiResp> T httpGetPost(String host, String path, Map<String, String> queryMap, Object post, Class<T> clazz) throws HuobiApiException {
        String query = null;
        try {
            query = HuobiUtil.buildQuery(queryMap);
        } catch (UnsupportedEncodingException e) {
            throw new HuobiApiException(e);
        }
        String url = String.format("%s%s?%s", host, path, query);
        // System.out.println(url);
        return executeReq(buildRequest(url, post), clazz);
    }

    private <T extends HuobiResp> T httpGet(String host, String path, Map<String, String> queryMap, Class<T> clazz) throws HuobiApiException {
        return httpGetPost(host, path, queryMap, null, clazz);
    }

    private <T extends HuobiResp> T httpPost(String host, String path, Map<String, String> queryMap, Object post, Class<T> clazz) throws HuobiApiException {
        return httpGetPost(host, path, queryMap, post, clazz);
    }

    @Override
    public List<HuobiKLineData> kline(String symbol, String period, int size) throws HuobiApiException {
        if (StringUtils.isEmpty(symbol)) {
            throw new IllegalArgumentException("symbol");
        }
        String ps = "1min,5min,15min,30min,60min,1day,1mon,1week,1year";
        final String[] periods = ps.split(",");
        boolean find = false;
        for (String s : periods) {
            if (s.equals(period)) {
                find = true;
                break;
            }
        }
        if (!find) {
            throw new IllegalArgumentException("period");
        }

        if (size < 1) {
            size = 1;
        }
        if (size > 2000) {
            size = 2000;
        }

        final String path = "/market/history/kline";
        Map<String, String> map = new HashMap<>();
        map.put("symbol", symbol.toLowerCase());
        map.put("period", period);
        map.put("size", String.valueOf(size));
        HuobiKLineResp resp = httpGet(HuobiConst.MARKET_URL, path, map, HuobiKLineResp.class);
        return resp.getData();
    }

    @Override
    public HuobiTick tick(String symbol) throws HuobiApiException {
        if (StringUtils.isEmpty(symbol)) {
            throw new IllegalArgumentException("symbol");
        }
        final String path = "/market/detail/merged";
        Map<String, String> map = new HashMap<>();
        map.put("symbol", symbol.toLowerCase());
        HuobiTickResp resp = httpGet(HuobiConst.MARKET_URL, path, map, HuobiTickResp.class);
        return resp.getTick();
    }

    @Override
    public List<String> currencys() throws HuobiApiException {
        String path = "/v1/common/currencys";
        HuobiCurrencysResp resp = httpGet(HuobiConst.MARKET_URL, path, null, HuobiCurrencysResp.class);
        return resp.getSymbols();
    }

    @Override
    public List<HuobiSymbol> symbols() throws HuobiApiException {
        String path = "/v1/common/symbols";
        HuobiSymbolResp resp = httpGet(HuobiConst.MARKET_URL, path, null, HuobiSymbolResp.class);
        return resp.getSymbols();
    }

    @Override
    public HuobiOrderBook depth(String symbol, String type) throws HuobiApiException {
        String path = "/market/depth";
        Map<String, String> query = new HashMap<>();
        query.put("symbol", symbol.trim().toLowerCase());
        query.put("type", type);
        HuobiOrderBookResp resp = httpGet(HuobiConst.MARKET_URL, path, query, HuobiOrderBookResp.class);

        HuobiOrderBook book = new HuobiOrderBook();
        book.setTs(resp.getTs());
        book.setAsks(HuobiOrderBookEntry.parseMany(resp.tick.asks));
        book.setBids(HuobiOrderBookEntry.parseMany(resp.tick.bids));
        return book;
    }

    private TreeMap<String, String> buildQueryMap(Map<String, String> map, String method, String path) throws HuobiApiException {
        TreeMap<String, String> query = new TreeMap<>();
        query.put("AccessKeyId", apiKey);
        query.put("SignatureMethod", "HmacSHA256");
        query.put("SignatureVersion", "2");
        Date now = new Date();
        String timestamp = String.format("%sT%s", sdfDate.format(now), sdfTime.format(now));
        // timestamp = "2018-01-08T14:30:20";

        query.put("Timestamp", timestamp);

        if (METHOD_GET.equals(method) && (map != null) && !map.isEmpty()) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                query.put(e.getKey(), e.getValue());
            }
        }
        try {
            String data = String.format("%s\napi.huobi.pro\n%s\n%s", method, path, HuobiUtil.buildQuery(query));
            String sign = HuobiUtil.hashMac256(data, secret);
            query.put("Signature", sign);
            return query;
        } catch (UnsupportedEncodingException e) {
            throw new HuobiApiException(e);
        }
    }

    @Override
    public List<HuobiAccount> accounts() throws HuobiApiException {
        synchronized (accountsCacheLock) {
            if (accountsCache != null) {
                return accountsCache;
            }
        }
        if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(secret)) {
            throw new HuobiApiException("key not exist");
        }
        String path = "/v1/account/accounts";
        TreeMap<String, String> query = buildQueryMap(null, METHOD_GET, path);
        HuobiAccountResp resp = httpGet(HuobiConst.TRADE_URL, path, query, HuobiAccountResp.class);
        if (resp != null && !resp.getAccounts().isEmpty()) {
            synchronized (accountsCacheLock) {
                this.accountsCache = resp.getAccounts();
            }
            return resp.getAccounts();
        }
        return new ArrayList<>();
    }

    private HuobiAccount spotAccount() throws HuobiApiException {
        List<HuobiAccount> accounts = null;
        synchronized (accountsCacheLock) {
            if (accountsCache != null) {
                accounts = accountsCache;
            } else {
                accounts = accounts();
            }
        }
        for (HuobiAccount acc : accounts) {
            if (HuobiAccount.ACCOUNT_TYPE_SPOT.equals(acc.getType())) {
                return acc;
            }
        }
        return null;
    }

    @Override
    public HuobiBalance balance(String type) throws HuobiApiException {
        if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(secret)) {
            throw new HuobiApiException("key not exist");
        }
        if (!HuobiAccount.ACCOUNT_TYPE_SPOT.equals(type) && !HuobiAccount.ACCOUNT_TYPE_OTC.equals(type)) {
            throw new HuobiApiException("invalid type");
        }
        List<HuobiAccount> accounts = null;
        synchronized (accountsCacheLock) {
            if (accountsCache != null) {
                accounts = accountsCache;
            } else {
                accounts = accounts();
            }
        }
        HuobiAccount spotAccount = null;
        for (HuobiAccount acc : accounts) {
            if (type.equals(acc.getType())) {
                spotAccount = acc;
            }
        }
        if (spotAccount == null) {
            throw new HuobiApiException("spot account not found");
        }
        return balance(spotAccount.getId());
    }

    @Override
    public HuobiBalance balance(long accountId) throws HuobiApiException {
        String path = String.format("/v1/account/accounts/%d/balance", accountId);
        HashMap<String, String> map = new HashMap<>();
        map.put("account-id", String.valueOf(accountId));
        TreeMap<String, String> query = buildQueryMap(map, METHOD_GET, path);
        HuobiBalanceResp resp = httpGet(HuobiConst.TRADE_URL, path, query, HuobiBalanceResp.class);
        return resp.data;
    }

    @Override
    public String sendOrder(String symbol, String price, String amount, HuobiOrderType type) throws HuobiApiException {
        if (StringUtils.isEmpty(symbol)) {
            throw new HuobiApiException("invalid symbol");
        }
        symbol = symbol.trim().toLowerCase();
        final String path = "/v1/order/orders/place";
        final String source = "api";
        HuobiAccount spotAcount = spotAccount();
        HashMap<String, String> map = new HashMap<>();
        map.put("account-id", String.valueOf(spotAcount.getId()));
        map.put("amount", amount);
        map.put("price", price);
        map.put("symbol", symbol);
        map.put("type", type.getName());
        map.put("source", source);
        TreeMap<String, String> query = buildQueryMap(map, METHOD_POST, path);
        HuobiOrderResp resp = httpPost(HuobiConst.TRADE_URL, path, query, map, HuobiOrderResp.class);
        return resp.getOrderId();
    }

    @Override
    public String cancelOrder(String orderId) throws HuobiApiException {
        if (StringUtils.isEmpty(orderId)) {
            throw new HuobiApiException("invalid orderId");
        }
        final String path = String.format("/v1/order/orders/%s/submitcancel", orderId);
        HashMap<String, String> map = new HashMap<>();
        TreeMap<String, String> query = buildQueryMap(map, METHOD_POST, path);
        HuobiCancelOrderResp resp = httpPost(HuobiConst.TRADE_URL, path, query, map, HuobiCancelOrderResp.class);
        return resp.getOrderId();
    }

    @Override
    public HuobiOrderInfo orderInfo(String orderId) throws HuobiApiException {
        if (StringUtils.isEmpty(orderId)) {
            throw new HuobiApiException("invalid orderId");
        }
        String path = String.format("/v1/order/orders/%s", orderId);
        HashMap<String, String> map = new HashMap<>();
        TreeMap<String, String> query = buildQueryMap(map, METHOD_GET, path);
        HuobiOrderInfolResp resp = httpGet(HuobiConst.TRADE_URL, path, query, HuobiOrderInfolResp.class);
        return resp.getOrderDetail();
    }

    @Override
    public HuobiOrderMatchResult matchResult(String orderId) throws HuobiApiException {
        if (StringUtils.isEmpty(orderId)) {
            throw new HuobiApiException("invalid orderId");
        }
        String path = String.format("/v1/order/orders/%s/matchresults", orderId);
        HashMap<String, String> map = new HashMap<>();
        TreeMap<String, String> query = buildQueryMap(map, METHOD_GET, path);
        HuobiOrderMatchResultResp resp = httpGet(HuobiConst.TRADE_URL, path, query, HuobiOrderMatchResultResp.class);
        return resp.getMatchResult();
    }

    private String joinTypes(List<HuobiOrderType> types) {
        if (types == null || types.isEmpty()) {
            return "";
        }
        return StringUtils.join(types, ",");
    }

    private String joinStates(List<HuobiOrderState> states) {
        if (states == null || states.isEmpty()) {
            return "";
        }
        return StringUtils.join(states, ",");
    }

    @Override
    public List<HuobiOrderInfo> orders(String symbol,
                                       List<HuobiOrderType> types,
                                       String startDate,
                                       String endDate,
                                       List<HuobiOrderState> states,
                                       String fromOrderId,
                                       int size) throws HuobiApiException {
        if (StringUtils.isEmpty(symbol)) {
            throw new HuobiApiException("symbol is valid");
        }
        if (states == null || states.isEmpty()) {
            throw new HuobiApiException("states is empty");
        }
        symbol = symbol.trim().toLowerCase();
        final String path = "/v1/order/orders";
        HashMap<String, String> map = new HashMap<>();
        map.put("symbol", symbol);
        String typesJoin = joinTypes(types);
        if (StringUtils.isEmpty(typesJoin)) {
            map.put("types", typesJoin);
        }
        if (!StringUtils.isEmpty(startDate)) {
            map.put("start-date", startDate);
        }
        if (!StringUtils.isEmpty(endDate)) {
            map.put("end-date", endDate);
        }
        map.put("states", joinStates(states));
        if (!StringUtils.isEmpty(fromOrderId)) {
            map.put("from", fromOrderId);
        }
        if (size > 0) {
            map.put("size", String.valueOf(size));
        }

        TreeMap<String, String> query = buildQueryMap(map, METHOD_GET, path);
        HuobiOrdersResp resp = httpGet(HuobiConst.TRADE_URL, path, query, HuobiOrdersResp.class);
        return resp.getOrderInfos();
    }

}
