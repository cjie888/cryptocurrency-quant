package com.cjie.cryptocurrency.quant.strategy.fcoin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.fcoin.Balance;
import com.cjie.cryptocurrency.quant.api.fcoin.FcoinRetry;
import com.cjie.cryptocurrency.quant.mapper.CurrencyBalanceLogMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyBalanceLog;
import com.cjie.cryptocurrency.quant.mapper.CurrencyOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.CurrencyPriceMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyOrder;
import com.cjie.cryptocurrency.quant.model.CurrencyPrice;
import com.cjie.cryptocurrency.quant.mapper.CurrencyBalanceLogMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyBalanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FcoinUtils {

    private static final RetryTemplate retryTemplate = FcoinRetry.getRetryTemplate();

    private static final RetryTemplate tradeRetryTemplate = FcoinRetry.getTradeRetryTemplate();

    private static final Logger logger = LoggerFactory.getLogger(FcoinUtils.class);

    private static final String app_key;
    private static final String app_secret;

    private static final double initMultiple;//初始化平衡的美金
    private static final double maxNum;//单笔最大数量
    private static final double minUsdt;//最小美金
    private static final int pricePrecision;
    private static final int numPrecision;
    private static final double minLimitPriceOrderNum;

    private static final int initInterval;//初始化间隔

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private CurrencyOrderMapper currencyOrderMapper;

    @Autowired
    private CurrencyBalanceLogMapper currencyBalanceLogMapper;

    static {
        Properties properties = null;
        try {
            properties = PropertiesLoaderUtils.loadProperties(
                    new ClassPathResource("app_ft.properties", FcoinUtils.class.getClassLoader()));
        } catch (IOException e) {
            logger.error("类初始化异常", e);
        }

        app_key = properties.getProperty("app_key");
        app_secret = properties.getProperty("app_secret");

        initMultiple = Double.valueOf(properties.getProperty("initMultiple", "3"));
        maxNum = Double.valueOf(properties.getProperty("maxNum", "1000"));
        minUsdt = Double.valueOf(properties.getProperty("minUsdt", "10"));

        initInterval = Integer.valueOf(properties.getProperty("initInterval", "10"));
        pricePrecision = Integer.valueOf(properties.getProperty("pricePrecision", "5"));
        numPrecision = Integer.valueOf(properties.getProperty("numPrecision", "2"));
        minLimitPriceOrderNum = Double.valueOf(properties.getProperty("minLimitPriceOrderNum", "3.01"));
    }

    public static BigDecimal getBigDecimal(double value, int scale) {
        return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal getNum(double b) {//为了尽量能够成交，数字向下精度
        return new BigDecimal(b).setScale(numPrecision, BigDecimal.ROUND_DOWN);
    }

    public static BigDecimal getMarketPrice(double marketPrice) {
        return getBigDecimal(marketPrice, pricePrecision);
    }

    public static String getSign(String data, String secret) throws Exception {

        String base64_1 = Base64.getEncoder().encodeToString(data.getBytes("utf-8"));
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes("utf-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(base64_1.getBytes("utf-8"));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    public static String getBalance() throws Exception {
        String url = "https://api.fcoin.com/v2/accounts/balance";
        Long timeStamp = System.currentTimeMillis();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("FC-ACCESS-KEY", app_key);
        headers.add("FC-ACCESS-SIGNATURE", getSign("GET" + url + timeStamp, app_secret));
        headers.add("FC-ACCESS-TIMESTAMP", timeStamp.toString());

        HttpEntity requestEntity = new HttpEntity<>(headers);
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        //logger.info(response.getBody());
        return response.getBody();
    }

    public void buy(String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        BigDecimal maxNumDeci = getNum(maxNum);
        while (amount.doubleValue() > 0) {
            if (amount.compareTo(maxNumDeci) > 0) {
                subBuy(maxNumDeci.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
            } else {
                subBuy(amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
                break;
            }
            amount = amount.subtract(maxNumDeci);

            Thread.sleep(5000);
        }

    }

    public void sell(String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        BigDecimal maxNumDeci = getNum(maxNum);
        while (amount.doubleValue() > 0) {
            if (amount.compareTo(maxNumDeci) > 0) {
                subSell(maxNumDeci.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
            } else {
                subSell(amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
                break;
            }
            amount = amount.subtract(maxNumDeci);

            Thread.sleep(5000);
        }
    }

    public void buyNotLimit(String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        subBuy(amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
    }

    public void sellNotLimit(String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        subSell(amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
    }

    private boolean createOrder(String amount, String price, String side, String symbol, String type, String marketPrice) throws Exception {
        String url = "https://api.fcoin.com/v2/orders";
        Long timeStamp = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.add("FC-ACCESS-KEY", app_key);
        headers.add("FC-ACCESS-TIMESTAMP", timeStamp.toString());

        MediaType t = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(t);
        headers.setAccept(Collections.singletonList(MediaType.ALL));

        //  封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        Map<String, String> params = new HashMap<>();
        //  也支持中文
        params.put("amount", amount);
        params.put("side", side);
        params.put("symbol", symbol);
        params.put("type", type);
        String urlSeri = "";
        if ("limit".equals(type)) {
            urlSeri = "amount=" + amount + "&price=" + price + "&side=" + side + "&symbol=" + symbol + "&type=" + type;
            params.put("price", price);
        } else if ("market".equals(type)) {
            urlSeri = "amount=" + amount + "&side=" + side + "&symbol=" + symbol + "&type=" + type;
        }
        headers.add("FC-ACCESS-SIGNATURE",
                getSign("POST" + url + timeStamp + urlSeri, app_secret));

        String param = JSON.toJSONString(params);
        logger.info(param);
        HttpEntity<String> requestEntity = new HttpEntity<String>(param, headers);
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response;
        try {
            response = client.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            logger.error("买卖有异常", e);
            throw new Exception(e);
        }
        logger.info(response.getBody());
        if (StringUtils.isEmpty(response.getBody())) {
            throw new Exception("订单创建失败：" + type);
        }
        if (!StringUtils.isEmpty(response.getBody())) {
            String data = JSON.parseObject(response.getBody()).getString("data");
            CurrencyOrder currencyOrder = CurrencyOrder.builder()
                    .orderId(data)
                    .amount(new BigDecimal(amount))
                    .orderPrice(new BigDecimal(price))
                    .createTime(new Date())
                    .markePrice(new BigDecimal(marketPrice))
                    .type("buy".equals(side) ? 1 : 2)
                    .site("fcoin")
                    .baseCurrency("ft")
                    .quotaCurrency("usdt").build();
            currencyOrderMapper.insert(currencyOrder);
            if (StringUtils.isEmpty(data)) {
                throw new Exception("订单创建失败：" + type);
            }
        }
        return true;
    }

    public void subSell(String amount, String price, String symbol, String type, String marketPrice) throws Exception {
        tradeRetryTemplate.execute(retryContext ->
                createOrder(amount, price, "sell", symbol, type, marketPrice)
        );
    }

    public void subBuy(String amount, String price, String symbol, String type, String marketPrice) throws Exception {
        tradeRetryTemplate.execute(retryContext ->
                createOrder(amount, price, "buy", symbol, type, marketPrice)
        );
    }

    public Map<String, Double> getPriceInfo(String symbol, String baseCurrency, String quotaCurrency) throws Exception {
        String url = "https://api.fcoin.com/v2/market/ticker/" + symbol;
        Long timeStamp = System.currentTimeMillis();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("FC-ACCESS-KEY", app_key);
        headers.add("FC-ACCESS-SIGNATURE", getSign("GET" + url + timeStamp, app_secret));
        headers.add("FC-ACCESS-TIMESTAMP", timeStamp.toString());

        HttpEntity requestEntity = new HttpEntity<>(headers);
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        logger.info("price:{}", JSON.toJSONString(jsonObject));
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("ticker");
        Map<String, Double> result = new HashMap<>();
        double marketPrice = Double.valueOf(jsonArray.get(0).toString());

        result.put("marketPrice", marketPrice);
        CurrencyPrice currencyPrice  = CurrencyPrice.builder()
                .tickTime(new Date())
                .price(new BigDecimal(marketPrice))
                .baseCurrency(baseCurrency)
                .quotaCurrency(quotaCurrency)
                .site("fcoin")
                .build();
        currencyPriceMapper.insert(currencyPrice);

        double hight_24H = Double.valueOf(jsonArray.get(7).toString());
        double low_24H = Double.valueOf(jsonArray.get(8).toString());

        result.put("24HPrice", (hight_24H + low_24H) / 2);

        return result;
    }

    public static String getSymbols() throws Exception {
        String url = "https://api.fcoin.com/v2/public/symbols";
        Long timeStamp = System.currentTimeMillis();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("FC-ACCESS-KEY", app_key);
        headers.add("FC-ACCESS-SIGNATURE", getSign("GET" + url + timeStamp, app_secret));
        headers.add("FC-ACCESS-TIMESTAMP", timeStamp.toString());

        HttpEntity requestEntity = new HttpEntity<>(headers);
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        logger.info(response.getBody());
        return response.getBody();
    }

    public List<String> getOrdes(String symbol, String states, String after, String limit, String side) throws Exception {
        String url = "https://api.fcoin.com/v2/orders?after=" + after + "&limit=" + limit + "&states=" + states + "&symbol=" + symbol;
        Long timeStamp = System.currentTimeMillis();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("FC-ACCESS-KEY", app_key);
        headers.add("FC-ACCESS-SIGNATURE", getSign("GET" + url + timeStamp, app_secret));
        headers.add("FC-ACCESS-TIMESTAMP", timeStamp.toString());

        HttpEntity requestEntity = new HttpEntity<>(headers);
        RestTemplate client = new RestTemplate();
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        logger.info(response.getBody());
        JSONArray jsonArray = JSON.parseObject(response.getBody()).getJSONArray("data");
        if (jsonArray == null || jsonArray.size() == 0) {
            return new ArrayList<>();
        }
        if (StringUtils.isEmpty(side)) {
            return jsonArray.stream().map(jsonObject -> ((JSONObject) jsonObject).getString("id")).collect(Collectors.toList());
        } else {
            return jsonArray.stream().filter(jsonObj -> side.equals(((JSONObject) jsonObj).getString("side"))).map(jsonObject -> ((JSONObject) jsonObject).getString("id")).collect(Collectors.toList());
        }
    }

    public List<String> getNotTradeOrders(String symbol, String after, String limit) throws Exception {
        List<String> list1 = getOrdes(symbol, "submitted", after, limit, null);
        List<String> list2 = getOrdes(symbol, "partial_filled", after, limit, null);
        list1.addAll(list2);
        return list1;
    }

    public List<String> getNotTradeSellOrders(String symbol, String after, String limit) throws Exception {
        List<String> list1 = getOrdes(symbol, "submitted", after, limit, "sell");
        List<String> list2 = getOrdes(symbol, "partial_filled", after, limit, "sell");
        list1.addAll(list2);
        return list1;
    }

    public boolean cancelOrders(List<String> orderIds) throws Exception {
        if (orderIds == null || orderIds.size() == 0) {
            return false;
        }
        String urlPath = "https://api.fcoin.com/v2/orders/%s/submit-cancel";
        for (String orderId : orderIds) {
            retryTemplate.execute(retryContext -> {
                String url = String.format(urlPath, orderId);
                Long timeStamp = System.currentTimeMillis();
                HttpHeaders headers = new HttpHeaders();
                headers.add("FC-ACCESS-KEY", app_key);
                headers.add("FC-ACCESS-TIMESTAMP", timeStamp.toString());
                try {
                    headers.add("FC-ACCESS-SIGNATURE",
                            getSign("POST" + url + timeStamp, app_secret));
                } catch (Exception e) {
                    logger.error(e.toString());
                }
                MediaType t = MediaType.parseMediaType("application/json; charset=UTF-8");
                headers.setContentType(t);
                headers.setAccept(Collections.singletonList(MediaType.ALL));

                HttpEntity<String> requestEntity = new HttpEntity<String>(null, headers);
                RestTemplate client = new RestTemplate();
                client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                ResponseEntity<String> response = client.exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (StringUtils.isEmpty(response.getBody())) {
                    throw new Exception("cacel order error");
                }
                boolean flag = JSON.parseObject(response.getBody()).getBoolean("data");
                if (!flag) {
                    throw new Exception("cacel order error");
                }
                return true;
            });
        }
        return true;
    }

    private Map<String, Balance> buildBalance(String balance) {
        Map<String, Balance> map = new HashMap<>();

        try {
            JSONObject jsonObject = JSON.parseObject(balance);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            jsonArray.stream().forEach(jsonObj -> {
                JSONObject obj = (JSONObject) jsonObj;
                Balance balanceVo = new Balance();
                balanceVo.setAvailable(Double.valueOf(obj.getString("available")));
                balanceVo.setBalance(Double.valueOf(obj.getString("balance")));
                balanceVo.setFrozen(Double.valueOf(obj.getString("frozen")));
                map.put(obj.getString("currency"), balanceVo);
            });
        } catch (Exception e) {
            logger.error("build balance error", e);
        }

        return map;
    }

    private boolean isHaveInitBuyAndSell(double ft, double usdt, double marketPrice, double initUsdt, String symbol, String type, double increment) throws Exception {
        //初始化小的
        double ftValue = ft * marketPrice;
        double num = Math.min((Math.abs(usdt - ftValue) / 2), initUsdt);
        BigDecimal b = getNum(num / marketPrice);//现价的数量都为ft的数量
        if (b.doubleValue() - minLimitPriceOrderNum < 0) {
            logger.info("小于最小限价数量");
            return false;
        }
        if (ftValue < usdt && Math.abs(ftValue - usdt) > 0.1 * (ftValue + usdt)) {
            //买ft
            try {
                buy(symbol, type, b, getMarketPrice(marketPrice * (1-increment)));//此处不需要重试，让上次去判断余额后重新平衡
            } catch (Exception e) {
                logger.error("初始化买有异常发生", e);
                throw new Exception(e);
            }

        } else if (usdt < ftValue && Math.abs(ftValue - usdt) > 0.1 * (ftValue + usdt)) {
            //卖ft
            try {
                sell(symbol, type, b, getMarketPrice(marketPrice*(1+increment)));//此处不需要重试，让上次去判断余额后重新平衡
            } catch (Exception e) {
                logger.error("初始化卖有异常发生", e);
                throw new Exception(e);
            }
        } else {
            return false;
        }

        Thread.sleep(3000);
        return true;
    }

    /**
     * 整点之前是否可以交易
     *
     * @return
     */
    public boolean isTrade() {
        LocalDateTime localDateTime = LocalDateTime.now();

        LocalDateTime localDateTimeInt =
                LocalDateTime.of(localDateTime.getYear(), localDateTime.getMonth(), localDateTime.getDayOfMonth(), localDateTime.getHour() + 1, 1);

        if (localDateTime.compareTo(localDateTimeInt) < 0
                && Duration.between(localDateTime, localDateTimeInt).toMinutes() <= 2) {//只能进行买
            return false;
        }

        return true;
    }

    /**
     * 自买自卖，但是增加挂单超时取消功能
     *
     * @param symbol   交易对
     * @param ftName   交易币名称
     * @param usdtName 市场币名称
     * @throws Exception
     */
    public void ftusdt(String symbol, String ftName, String usdtName) throws Exception {
        int tradeCount = 0;
        int frozenCount = 0;
        while (true) {

            //查询余额
            String balance = null;
            try {
                balance = retryTemplate.execute(retryContext ->
                        getBalance()
                );
            } catch (Exception e) {
                logger.error("==========fcoinUtils.getBalance重试后还是异常============", e);
                continue;
            }

            Map<String, Balance> balances = buildBalance(balance);
            Balance ftBalance = balances.get(ftName);
            Balance usdtBalance = balances.get(usdtName);

            double ft = ftBalance.getBalance();
            double usdt = usdtBalance.getBalance();
            //判断是否有冻结的，如果冻结太多冻结就休眠，进行下次挖矿
            if (ftBalance.getFrozen() > 0.099 * ft || usdtBalance.getFrozen() > 0.099 * usdt) {
                frozenCount++;
                if (frozenCount % 40 == 0) {
                    try {
                        cancelOrders(getNotTradeOrders(symbol, "0", "100"));
                    } catch (Exception e) {
                        logger.error("cancel error",e);
                    }
                }
                Thread.sleep(3000);
                continue;
            }

            logger.info("===============balance: usdt:{},ft:{}========================", usdt, ft);

            if ("ftusdt".equals(symbol) && !isTrade()) {//整点十分钟之内不能交易
                //cancelOrders(getNotTradeSellOrders(symbol, "0", "100"));
                Thread.sleep(5000);
                break;
            }

            Map<String, Double> priceInfo = getPriceInfo(symbol, ftName, usdtName);
            Double marketPrice = priceInfo.get("marketPrice");
            //usdt小于51并且ft的价值小于51
            if ((usdt < (minUsdt + 1) && ft < ((minUsdt + 1) / marketPrice))
                    || (usdt < (minUsdt + 1) && Math.abs(ft * marketPrice - usdt) < 11)
                    || (ft < ((minUsdt + 1) / marketPrice) && Math.abs(ft * marketPrice - usdt) < 11)) {
                logger.info("跳出循环，ustd:{}, marketPrice:{}", usdt, marketPrice);
                break;
            }

            //ft:usdt=1:0.6 平衡资金
            double ftValue = ft * marketPrice;
            double initUsdt = maxNum * initMultiple * marketPrice;
            if ((ftValue < initUsdt || usdt < initUsdt)
                    && tradeCount % initInterval == 0
                    && !(ftBalance.getFrozen() > 0 || usdtBalance.getFrozen() > 0)) {
                //需要去初始化了
                try {
                    if (isHaveInitBuyAndSell(ft, usdt, marketPrice, initUsdt, symbol, "limit", 0.0)) {
                        //进行了两个币种的均衡，去进行余额查询，并判断是否成交完
                        logger.info("================有进行初始化均衡操作=================");
                        tradeCount++;
                        continue;
                    }
                } catch (Exception e) {//初始化失败，需要重新判断余额初始化
                    tradeCount = 0;
                    continue;
                }
            }

            //买单 卖单
            double price = Math.min(Math.min(ftBalance.getAvailable() * marketPrice, usdtBalance.getAvailable()), maxNum * marketPrice);

            BigDecimal ftAmount = getNum(price * 0.99 / marketPrice);
            if (ftAmount.doubleValue() - minLimitPriceOrderNum < 0) {
                logger.info("小于最小限价数量");
                break;
            }
            tradeCount++;
            logger.info("=============================交易对开始=========================");
            try {
                buyNotLimit(symbol, "limit", ftAmount, getMarketPrice(marketPrice));
            } catch (Exception e) {
                logger.error("交易对买出错", e);
                tradeCount = 0;
            }

            try {
                sellNotLimit(symbol, "limit", ftAmount, getMarketPrice(marketPrice));
            } catch (Exception e) {
                logger.error("交易对卖出错", e);
                tradeCount = 0;
            }

            logger.info("=============================交易对结束=========================");
            Thread.sleep(1000);
        }
    }

    /**
     * 自买自卖交易
     *
     * @param symbol    交易对
     * @param ftName    交易币名称
     * @param usdtName  市场币名称
     * @param increment 收益率一半
     * @throws Exception
     */
    public void ftusdt1(String symbol, String ftName, String usdtName, double increment) throws Exception {

            //查询余额
            String balance = null;
            try {
                balance = //retryTemplate.execute(retryContext ->
                        getBalance()
                ;
            } catch (Exception e) {
                logger.error("==========fcoinUtils.getBalance重试后还是异常============", e);
                return;
            }

            Map<String, Balance> balances = buildBalance(balance);
            Balance ftBalance = balances.get(ftName);
            Balance usdtBalance = balances.get(usdtName);

            double ft = ftBalance.getBalance();
            double usdt = usdtBalance.getBalance();

            //判断是否有冻结的，如果冻结太多冻结就休眠，进行下次挖矿
            if (ftBalance.getFrozen() > 0.099 * ft && usdtBalance.getFrozen() > 0.099 * usdt) {
                return;
            }

            logger.info("===============balance: usdt:{},ft:{}========================", usdt, ft);

            if ("ftusdt".equals(symbol) && !isTrade()) {//整点十分钟之内不能交易
                return;
            }

            Map<String, Double> priceInfo = getPriceInfo(symbol, ftName, usdtName);
            Double marketPrice = priceInfo.get("marketPrice");
            //usdt小于51并且ft的价值小于51
            if ((usdt < (minUsdt + 1) && ft < ((minUsdt + 1) / marketPrice))
                    || (usdt < (minUsdt + 1) && Math.abs(ft * marketPrice - usdt) < minUsdt / 5)
                    || (ft < ((minUsdt + 1) / marketPrice) && Math.abs(ft * marketPrice - usdt) < minUsdt / 5)) {
                logger.info("跳出循环，ustd:{}, marketPrice:{}", usdt, marketPrice);
                return;
            }

            //ft:usdt=1:0.6
            double initUsdt = maxNum * initMultiple * marketPrice;

            //初始化
            if (!(ftBalance.getFrozen() > 0 && usdtBalance.getFrozen() > 0)) {
                if (isHaveInitBuyAndSell(ftBalance.getAvailable(), usdtBalance.getAvailable(), marketPrice, initUsdt, symbol, "limit", increment)) {
                    logger.info("================有进行初始化均衡操作=================");
                    return;
                }
            }

            //买单 卖单
            double price = Math.min((ftBalance.getAvailable()) * marketPrice, usdtBalance.getAvailable());

            BigDecimal ftAmount = getNum(price * 0.99 / marketPrice);//预留点来扣手续费
            if (ftAmount.doubleValue() - minLimitPriceOrderNum < 0) {
                logger.info("小于最小限价数量");
                return;
            }

            logger.info("=============================交易对开始=========================");

            try {
                buyNotLimit(symbol, "limit", ftAmount, getMarketPrice(marketPrice * (1 - increment)));
            } catch (Exception e) {
                logger.error("交易对买出错", e);
            }
            try {
                sellNotLimit(symbol, "limit", ftAmount, getMarketPrice(marketPrice * (1 + increment)));
            } catch (Exception e) {
                logger.error("交易对卖出错", e);
            }
            logger.info("=============================交易对结束=========================");

    }

    /**
     * 做波段调用此函数
     *
     * @param symbol    交易对
     * @param ftName    交易币的名称
     * @param usdtName  市场币名称
     * @param increment 收益率的一半
     * @throws Exception
     */
    public void  ftusdt2(String symbol, String ftName, String usdtName, double increment) throws Exception {

        while (true) {

            //查询余额
            String balance = null;
            try {
                balance = retryTemplate.execute(retryContext ->
                        getBalance()
                );
            } catch (Exception e) {
                logger.error("==========fcoinUtils.getBalance重试后还是异常============", e);
                continue;
            }

            Map<String, Balance> balances = buildBalance(balance);
            Balance ftBalance = balances.get(ftName);
            Balance usdtBalance = balances.get(usdtName);

            double ft = ftBalance.getBalance();
            double usdt = usdtBalance.getBalance();
            //判断是否有冻结的，如果冻结太多冻结就休眠，进行下次挖矿
            if (ftBalance.getFrozen() > 0.099 * ft || usdtBalance.getFrozen() > 0.099 * usdt) {
                Thread.sleep(3000);
                continue;
            }

            logger.info("===============balance: usdt:{},ft:{}========================", usdt, ft);

            /*if ("ftusdt".equals(symbol) && !isTrade()) {//整点十分钟之内不能交易，波段可以交易的，也不需要取消订单
                cancelOrders(getNotTradeSellOrders(symbol, "0", "100"));
                Thread.sleep(5000);
                break;
            }*/

            Map<String, Double> priceInfo = getPriceInfo(symbol, ftName, usdtName);
            Double marketPrice = priceInfo.get("marketPrice");
            //usdt小于51并且ft的价值小于51
            if ((usdt < (minUsdt + 1) && ft < ((minUsdt + 1) / marketPrice))
                    || (usdt < (minUsdt + 1) && Math.abs(ft * marketPrice - usdt) < 11)
                    || (ft < ((minUsdt + 1) / marketPrice) && Math.abs(ft * marketPrice - usdt) < 11)) {
                logger.info("跳出循环，ustd:{}, marketPrice:{}", usdt, marketPrice);
                break;
            }

            //在波段内才能交易
            double avgPrice = priceInfo.get("24HPrice");
            logger.info("avgPrice:{}", avgPrice);
            if (Math.abs(marketPrice - avgPrice) > avgPrice * increment / 5) {
                Thread.sleep(3000);
                continue;
            }
            //ft:usdt=1:0.6
            double initUsdt = maxNum * initMultiple * marketPrice;

            //初始化
            if (!(ftBalance.getFrozen() > 0 || usdtBalance.getFrozen() > 0)) {
                if (isHaveInitBuyAndSell(ft, usdt, marketPrice, initUsdt, symbol, "limit", 0.0)) {
                    logger.info("================有进行初始化均衡操作=================");
                    continue;
                }
            }

            //买单 卖单
            double price = Math.min(ftBalance.getAvailable() * marketPrice, usdtBalance.getAvailable());

            BigDecimal ftAmount = getNum(price * 0.99 / marketPrice);
            if (ftAmount.doubleValue() - minLimitPriceOrderNum < 0) {
                logger.info("小于最小限价数量");
                break;
            }
            logger.info("=============================交易对开始=========================");

            try {
                buyNotLimit(symbol, "limit", ftAmount, getMarketPrice(avgPrice * (1 - increment)));
            } catch (Exception e) {
                logger.error("交易对买出错", e);
            }
            try {
                sellNotLimit(symbol, "limit", ftAmount, getMarketPrice(avgPrice * (1 + increment)));
            } catch (Exception e) {
                logger.error("交易对卖出错", e);
            }
            logger.info("=============================交易对结束=========================");

            Thread.sleep(1000);
        }
    }

    /**
     * 动态调整策略
     *
     * @param symbol    交易对
     * @param ftName    交易币的名称
     * @param usdtName  市场币名称
     * @param increment 收益率的一半
     * @throws Exception
     */
    public void  ftusdt3(String symbol, String ftName, String usdtName, double increment) throws Exception {

        cancelOrders(getNotTradeOrders(symbol, "0", "100"));

        //查询余额
        String balance = null;
        try {
            balance = //retryTemplate.execute(retryContext ->
                    getBalance()
            ;
        } catch (Exception e) {
            logger.error("==========fcoinUtils.getBalance重试后还是异常============", e);
            return;
        }

        Map<String, Balance> balances = buildBalance(balance);
        Balance ftBalance = balances.get(ftName);
        Balance usdtBalance = balances.get(usdtName);

        Map<String, Double> priceInfo = getPriceInfo(symbol, ftName, usdtName);
        Double marketPrice = priceInfo.get("marketPrice");


        double allAsset= ftBalance.getBalance() * marketPrice + usdtBalance.getBalance();
        logger.info("ftbalance:{}, usdtbalance:{}, allAsset:{}, asset/2:{}, ftbalance-usdt:{}", ftBalance.getBalance(), usdtBalance.getBalance(),
                allAsset, allAsset/2, ftBalance.getBalance() * marketPrice );

        BigDecimal usdtChange = null;
        BigDecimal ftChange = null;
        if (allAsset/2 - ftBalance.getBalance() * marketPrice  > allAsset * increment) {
            BigDecimal amount = new BigDecimal(allAsset/2-ftBalance.getBalance()* marketPrice).setScale(numPrecision, BigDecimal.ROUND_FLOOR);
            //买入
            if (amount.doubleValue() - marketPrice * minLimitPriceOrderNum < 0) {
                logger.info("小于最小限价数量");
            } else {
                BigDecimal ftamount = amount.divide(new BigDecimal(marketPrice),
                        numPrecision, BigDecimal.ROUND_DOWN);
                usdtChange = ftamount.multiply(getMarketPrice(marketPrice)).negate();
                ftChange = ftamount;
                buy(symbol, "limit", ftamount , getMarketPrice(marketPrice));//此处不需要重试，让上次去判断余额后重新平衡
            }
            logger.info("ftbalance:{}, usdtbalance:{}", ftBalance.getBalance() + amount.doubleValue(),
                    usdtBalance.getBalance() - amount.doubleValue() * getMarketPrice(marketPrice).doubleValue());
            logger.info("buy {}, price:{}", amount, marketPrice);
        }


        if (ftBalance.getBalance() * marketPrice - allAsset/2 > allAsset * increment) {
            //卖出
            BigDecimal amount = new BigDecimal(ftBalance.getBalance()* marketPrice-allAsset/2).setScale(numPrecision, BigDecimal.ROUND_FLOOR);
            if (amount.doubleValue() - marketPrice * minLimitPriceOrderNum < 0) {
                logger.info("小于最小限价数量");
            } else {
                BigDecimal ftamount = amount.divide(new BigDecimal(marketPrice),
                        numPrecision, BigDecimal.ROUND_DOWN);
                usdtChange = ftamount.multiply(getMarketPrice(marketPrice));
                ftChange = ftamount.negate();
                sell(symbol, "limit", ftamount, getMarketPrice(marketPrice));//此处不需要重试，让上次去判断余额后重新平衡

            }
            logger.info("ftbalance:{}, usdtbalance:{}", ftBalance.getBalance() - amount.doubleValue(),
                    usdtBalance.getBalance() + amount.doubleValue() * getMarketPrice(marketPrice).doubleValue());
            logger.info("sell {}, price:{}", amount, marketPrice);

        }
        if (usdtChange != null && ftChange != null) {
            CurrencyBalanceLog currencyBalanceLog = CurrencyBalanceLog.builder()
                    .currency("usdt")
                    .balance(usdtChange.add(BigDecimal.valueOf(usdtBalance.getBalance())).setScale(16))
                    .site("fcoin")
                    .createTime(new Date())
                    .build();
            currencyBalanceLogMapper.insert(currencyBalanceLog);
            currencyBalanceLog = CurrencyBalanceLog.builder()
                    .currency("ft")
                    .balance(ftChange.add(BigDecimal.valueOf(ftBalance.getBalance())).setScale(16))
                    .site("fcoin")
                    .createTime(new Date())
                    .build();
            currencyBalanceLogMapper.insert(currencyBalanceLog);
        }

    }

    public static void main(String[] args) throws Exception {
        //getSymbols();
        //getBalance();
        //getPriceInfo("ftusdt");
        new FcoinUtils().ftusdt3("ftusdt", "ft", "usdt", 0.01);
        //FcoinUtils.sell("ftusdt", "limit", new BigDecimal("3.35"), new BigDecimal("0.404247"));//此处不需要重试，让上次去判断余额后重新平衡

        //new FcoinUtils().getNotTradeOrders("ftusdt", "0", "100");
    }
}
