package com.cjie.cryptocurrency.quant.strategy.okex;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Account;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotAccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotOrderAPIServive;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.service.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MineService {

    @Autowired
    private SpotProductAPIService spotProductAPIService;

    @Autowired
    private SpotAccountAPIService spotAccountAPIService;

    @Autowired
    private SpotOrderAPIServive spotOrderAPIService;

    @Autowired
    private ApiKeyService  apiKeyService;


    private static double initMultiple = 3;

    private static double maxNum = 100;

    private static int numPrecision = 8;


    private static Map<String, Double> minLimitPriceOrderNums = new HashMap<>();

    private static final int pricePrecision = 8;

    static {
        minLimitPriceOrderNums.put("eos", 0.1);
        minLimitPriceOrderNums.put("ltc", 0.001);
        minLimitPriceOrderNums.put("bch", 0.001);
        minLimitPriceOrderNums.put("okb", 1.0);
    }



    /**
     * 自买自卖交易
     *
     * @param baseName    交易币名称
     * @param quotaName  市场币名称
     * @param increment 收益率一半
     * @throws Exception
     */
    public void mine1(String site, String baseName, String quotaName, double increment) throws Exception {
        String symbol = baseName.toUpperCase() + "-" + quotaName.toUpperCase();

        //查询余额
        Account baseAccount = getBalance(site, baseName);
        double baseHold = new BigDecimal(baseAccount.getBalance()).doubleValue() - new BigDecimal(baseAccount.getAvailable()).doubleValue();
        double baseBalance = new BigDecimal(baseAccount.getBalance()).doubleValue();


        Account quotaAccount = getBalance(site, quotaName);
        double quotaHold = new BigDecimal(quotaAccount.getBalance()).doubleValue() - new BigDecimal(quotaAccount.getAvailable()).doubleValue();
        double quotaBalance = new BigDecimal(quotaAccount.getBalance()).doubleValue();


        //判断是否有冻结的，如果冻结太多冻结就休眠，进行下次挖矿
        if (baseHold > 0.099 * baseBalance
                || quotaHold > 0.099 * quotaBalance) {
            return;
        }

        log.info("===============balance: base:{},quota:{}========================", baseBalance, quotaBalance);

        Ticker ticker = getTicker(site, baseName, quotaName);
        Double marketPrice = Double.parseDouble(ticker.getLast());
        log.info("ticker last {} -{}:{}", baseName, quotaName, marketPrice);
        //usdt小于51并且ft的价值小于51
//        if ((usdt < (minUsdt + 1) && ft < ((minUsdt + 1) / marketPrice))
//                || (usdt < (minUsdt + 1) && Math.abs(ft * marketPrice - usdt) < minUsdt / 5)
//                || (ft < ((minUsdt + 1) / marketPrice) && Math.abs(ft * marketPrice - usdt) < minUsdt / 5)) {
//            logger.info("跳出循环，ustd:{}, marketPrice:{}", usdt, marketPrice);
//            return;
//        }

        //ft:usdt=1:0.6
        double initUsdt = maxNum * initMultiple * marketPrice;
//
        //初始化
        if (!(baseHold > 0 || quotaHold > 0)) {
            if (isHaveInitBuyAndSell(site, baseBalance - baseHold, quotaBalance - quotaHold, marketPrice, initUsdt, symbol, "limit", increment)) {
                log.info("================有进行初始化均衡操作=================");
                return;
            }
        }
//
        //买单 卖单
        double price = Math.min((baseBalance - baseHold) * marketPrice, quotaBalance - quotaHold);

        BigDecimal baseAmount = getNum(price * 0.99 / marketPrice);//预留点来扣手续费
        if (baseAmount.doubleValue() - minLimitPriceOrderNums.get(baseName.toLowerCase()) < 0) {
            log.info("小于最小限价数量");
            return;
        }

        log.info("=============================交易对开始=========================");
//
        try {
            buyNotLimit(site, symbol, "limit", baseAmount, getMarketPrice(marketPrice * (1 - increment)));
        } catch (Exception e) {
            log.error("交易对买出错", e);
        }
        try {
            sellNotLimit(site, symbol, "limit", baseAmount, getMarketPrice(marketPrice * (1 + increment)));
        } catch (Exception e) {
            log.error("交易对卖出错", e);
        }
        log.info("=============================交易对结束=========================");

    }

    /**
     * 动态调整策略
     *
     * @param baseName    交易币名称
     * @param quotaName  市场币名称
     * @param increment 收益率一半
     * @throws Exception
     * @throws Exception
     */
    public void  mine3(String site, String baseName, String quotaName, double increment, double baseRatio) throws Exception {


        String symbol = baseName.toUpperCase() + "-" + quotaName.toUpperCase();
        cancelOrders(site, getNotTradeOrders(site, symbol, "0", "100"));


        //查询余额
        Account baseAccount = getBalance(site, baseName);
        double baseHold = new BigDecimal(baseAccount.getBalance()).doubleValue() - new BigDecimal(baseAccount.getAvailable()).doubleValue();
        double baseBalance = new BigDecimal(baseAccount.getBalance()).doubleValue();


        Account quotaAccount = getBalance(site, quotaName);
        double quotaHold = new BigDecimal(quotaAccount.getBalance()).doubleValue() - new BigDecimal(quotaAccount.getAvailable()).doubleValue();
        double quotaBalance = new BigDecimal(quotaAccount.getBalance()).doubleValue();


        Ticker ticker = getTicker(site, baseName, quotaName);
        Double marketPrice = Double.parseDouble(ticker.getLast());
        log.info("ticker last {} -{}:{}", baseName, quotaName, marketPrice);


        double allAsset= baseBalance * marketPrice + quotaBalance;
        log.info("basebalance:{}, qutobalance:{}, allAsset:{}, asset/2:{}, basebalance-quota:{}",
                baseBalance, quotaBalance, allAsset, allAsset*baseRatio, baseBalance * marketPrice );

        BigDecimal quotaChange = null;
        BigDecimal baseChange = null;
        if (allAsset*baseRatio - baseBalance * marketPrice  > allAsset * increment) {
            BigDecimal amount = new BigDecimal(allAsset*baseRatio -baseBalance* marketPrice).setScale(numPrecision, BigDecimal.ROUND_FLOOR);
            log.info("basebalance:{}, quotabalance:{}", baseBalance + amount.doubleValue(),
                    quotaBalance - amount.doubleValue() * getMarketPrice(marketPrice).doubleValue());
            log.info("buy {}, price:{}", amount, marketPrice);
            //买入
            if (amount.doubleValue() - minLimitPriceOrderNums.get(baseName.toLowerCase()) * marketPrice < 0) {
                log.info("小于最小限价数量");
            } else {
                BigDecimal baseamount = amount.divide(new BigDecimal(marketPrice),
                        numPrecision, BigDecimal.ROUND_DOWN);
                quotaChange = baseamount.multiply(getMarketPrice(marketPrice)).negate();
                baseChange = baseamount;
                buy(site, symbol, "limit", baseamount , getMarketPrice(marketPrice));//此处不需要重试，让上次去判断余额后重新平衡
            }
        }


        if (baseBalance * marketPrice - allAsset * baseRatio > allAsset * increment) {
            //卖出
            BigDecimal amount = new BigDecimal(baseBalance* marketPrice-allAsset * baseRatio).setScale(numPrecision, BigDecimal.ROUND_FLOOR);
            log.info("basebalance:{}, quotabalance:{}", baseBalance - amount.doubleValue(),
                    quotaBalance + amount.doubleValue() * getMarketPrice(marketPrice).doubleValue());
            log.info("sell {}, price:{}", amount, marketPrice);
            if (amount.doubleValue() - minLimitPriceOrderNums.get(baseName.toLowerCase()) * marketPrice < 0) {
                log.info("小于最小限价数量");
            } else {
                BigDecimal baseamount = amount.divide(new BigDecimal(marketPrice),
                        numPrecision, BigDecimal.ROUND_DOWN);
                quotaChange = baseamount.multiply(getMarketPrice(marketPrice));
                baseChange = baseamount.negate();
                sell(site, symbol, "limit", baseamount, getMarketPrice(marketPrice));//此处不需要重试，让上次去判断余额后重新平衡

            }

        }

    }
    public boolean cancelOrders(String site, List<OrderInfo> orderIds) throws Exception {
        if (orderIds == null || orderIds.size() == 0) {
            return false;
        }
        for (OrderInfo orderInfo : orderIds) {
            PlaceOrderParam placeOrderParam = new PlaceOrderParam();
            placeOrderParam.setProduct_id(orderInfo.getProduct_id());
            spotOrderAPIService.cancleOrderByOrderId(site, placeOrderParam, orderInfo.getOrder_id());
        }
        return true;
    }

    public void buyNotLimit(String site, String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        subBuy(site, amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
    }

    public void sellNotLimit(String site, String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        subSell(site, amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
    }

    private boolean isHaveInitBuyAndSell(String site, double base, double quota, double marketPrice, double initUsdt, String symbol, String type, double increment) throws Exception {
        //初始化小的
        double baseValue = base * marketPrice;
        double num = Math.min((Math.abs(quota - baseValue) / 2), initUsdt);
        BigDecimal b = getNum(num / marketPrice);//现价的数量都为ft的数量
        if (b.doubleValue() - minLimitPriceOrderNums.get(symbol.split("-")[0].toLowerCase()) < 0) {
            log.info("小于最小限价数量");
            return false;
        }
        if (baseValue < quota && Math.abs(baseValue - quota) > 0.1 * (baseValue + quota)) {
            //买ft
            try {
                buy(site, symbol, type, b, getMarketPrice(marketPrice * (1-increment)));//此处不需要重试，让上次去判断余额后重新平衡
            } catch (Exception e) {
                log.error("初始化买有异常发生", e);
                throw new Exception(e);
            }

        } else if (quota < baseValue && Math.abs(baseValue - quota) > 0.1 * (baseValue + quota)) {
            //卖ft
            try {
                sell(site, symbol, type, b, getMarketPrice(marketPrice*(1+increment)));//此处不需要重试，让上次去判断余额后重新平衡
            } catch (Exception e) {
                log.error("初始化卖有异常发生", e);
                throw new Exception(e);
            }
        } else {
            return false;
        }

        Thread.sleep(3000);
        return true;
    }

    public void buy(String site, String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        BigDecimal maxNumDeci = getNum(maxNum);
        while (amount.doubleValue() > 0) {
            if (amount.compareTo(maxNumDeci) > 0) {
                subBuy(site, maxNumDeci.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
            } else {
                subBuy(site, amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
                break;
            }
            amount = amount.subtract(maxNumDeci);

            Thread.sleep(5000);
        }

    }

    public void sell(String site, String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        BigDecimal maxNumDeci = getNum(maxNum);
        while (amount.doubleValue() > 0) {
            if (amount.compareTo(maxNumDeci) > 0) {
                subSell(site, maxNumDeci.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
            } else {
                subSell(site, amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
                break;
            }
            amount = amount.subtract(maxNumDeci);

            Thread.sleep(5000);
        }
    }

    public void subSell(String site, String amount, String price, String symbol, String type, String marketPrice) throws Exception {
        createOrder(site, amount, price, "sell", symbol, type, marketPrice);

    }

    public void subBuy(String site,String amount, String price, String symbol, String type, String marketPrice) throws Exception {
        createOrder(site, amount, price, "buy", symbol, type, marketPrice);

    }

    private void createOrder(String site, String amount, String price, String buy, String symbol, String type, String marketPrice) {
        PlaceOrderParam placeOrderParam = new PlaceOrderParam();
        placeOrderParam.setProduct_id(symbol);
        placeOrderParam.setPrice(price);
        placeOrderParam.setSize(amount);
        placeOrderParam.setSide(buy);
        placeOrderParam.setType(type);

        spotOrderAPIService.addOrder(site,placeOrderParam);
    }

    public static BigDecimal getMarketPrice(double marketPrice) {
        return getBigDecimal(marketPrice, pricePrecision);
    }

    public static BigDecimal getBigDecimal(double value, int scale) {
        return new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
    public BigDecimal getNum(double b) {//为了尽量能够成交，数字向下精度
        return new BigDecimal(b).setScale(numPrecision, BigDecimal.ROUND_DOWN);
    }
    public Account getBalance(String site, String currency) throws Exception {

        return spotAccountAPIService.getAccountByCurrency(site, currency);
    }

    public Ticker getTicker(String site, String baseCurrency, String quotaCurrency) {
        String symbol = baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase();
        MultiValueMap<String, String> headers = new HttpHeaders();
        APIKey apiKey = apiKeyService.getApiKey(site);
        headers.add("Referer", apiKey.getDomain());
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url =  apiKey.getDomain() + "/api/spot/v3/products/"+symbol+"/ticker";
        RestTemplate client = new RestTemplate();

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
        return JSON.parseObject(body,Ticker.class);

        //return spotProductAPIService.getTickerByProductId(baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase());
    }

    public List<OrderInfo> getNotTradeOrders(String site, String symbol, String after, String limit) throws Exception {
        List<OrderInfo> list1 = getOrders(site, symbol, "open", after, limit, null);
        List<OrderInfo> list2 = getOrders(site, symbol, "part_filled", after, limit, null);
        list1.addAll(list2);
        return list1;
    }

    public List<OrderInfo> getOrders(String site, String symbol, String states, String after, String limit, String side) throws Exception {
        return spotOrderAPIService.getOrders(site, symbol, states, null, null, null);

    }

    public static void main(String[] args) {
        new MineService().getTicker("oktop","top", "usdt");
    }

}
