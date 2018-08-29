package com.cjie.cryptocurrency.quant.strategy.okex;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.fcoin.FcoinRetry;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Account;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Book;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotAccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotOrderAPIServive;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.mapper.CurrencyRatioMapper;
import com.cjie.cryptocurrency.quant.mapper.MineConfigMapper;
import com.cjie.cryptocurrency.quant.model.CurrencyRatio;
import com.cjie.cryptocurrency.quant.model.MineConfig;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class CoinAllMineService {

    @Autowired
    private SpotProductAPIService spotProductAPIService;

    @Autowired
    private SpotAccountAPIService spotAccountAPIService;

    @Autowired
    private SpotOrderAPIServive spotOrderAPIService;

    @Autowired
    private AccountAPIService accountAPIService;

    @Autowired
    private WeiXinMessageService weiXinMessageService;

    @Autowired
    private CurrencyRatioMapper currencyRatioMapper;

    @Autowired
    private MineConfigMapper mineConfigMapper;

    private static final RetryTemplate retryTemplate = FcoinRetry.getRetryTemplate();


    private static double initMultiple = 3;

    private static double maxNum = 50;

    private static Map<String, Integer> maxNums = new HashMap<>();


    private static Map<String, Double> minLimitPriceOrderNums = new HashMap<>();

    static {
        minLimitPriceOrderNums.put("eos", 0.1);
        minLimitPriceOrderNums.put("ltc", 0.001);
        minLimitPriceOrderNums.put("okb", 1.0);
        minLimitPriceOrderNums.put("cac", 1.0);

        maxNums.put("cac", 50);
        maxNums.put("okb", 20);
    }


    private static int numPrecision = 8;

    private static double minLimitPriceOrderNum = 1;

    private static final int pricePrecision = 8;


    /**
     * 自买自卖交易
     *
     * @param baseName    交易币名称
     * @param quotaName  市场币名称
     * @param increment 收益率一半
     * @throws Exception
     */
    public void mine1(String baseName, String quotaName, double increment) throws Exception {
        String symbol = baseName.toUpperCase() + "-" + quotaName.toUpperCase();

        MineConfig mineConfig = mineConfigMapper.getLatestConfig("coinall",
                baseName.toLowerCase(), quotaName.toLowerCase());
        if (Objects.isNull(mineConfig) || mineConfig.getStatus() == 0) {
            log.info("mine {} - {} closed", baseName, quotaName);
            return;
        }

//        if (!"okb".equalsIgnoreCase(baseName)) {
//            cancelOrders(getNotTradeOrders(symbol, "0", "100"), 300);
//        }
        //查询余额
        Account baseAccount = getBalance(baseName);
        double baseHold = new BigDecimal(baseAccount.getBalance()).doubleValue() - new BigDecimal(baseAccount.getAvailable()).doubleValue();
        double baseBalance = new BigDecimal(baseAccount.getBalance()).doubleValue();


        Account quotaAccount = getBalance(quotaName);
        double quotaHold = new BigDecimal(quotaAccount.getBalance()).doubleValue() - new BigDecimal(quotaAccount.getAvailable()).doubleValue();
        double quotaBalance = new BigDecimal(quotaAccount.getBalance()).doubleValue();


        //判断是否有冻结的，如果冻结太多冻结就休眠，进行下次挖矿
        if (baseHold > 0.999 * baseBalance
                && quotaHold > 0.999 * quotaBalance) {
            return;
        }

        log.info("===============balance: base:{},quota:{}========================", baseBalance, quotaBalance);

        Ticker ticker = getTicker(baseName, quotaName);
        Double marketPrice = Double.parseDouble(ticker.getLast());
        log.info("ticker last {} -{}:{}", baseName, quotaName, marketPrice);
        //usdt小于51并且ft的价值小于51
//        if ((usdt < (minUsdt + 1) && ft < ((minUsdt + 1) / marketPrice))
//                || (usdt < (minUsdt + 1) && Math.abs(ft * marketPrice - usdt) < minUsdt / 5)
//                || (ft < ((minUsdt + 1) / marketPrice) && Math.abs(ft * marketPrice - usdt) < minUsdt / 5)) {
//            logger.info("跳出循环，ustd:{}, marketPrice:{}", usdt, marketPrice);
//            return;
//        }
        if (marketPrice > mineConfig.getMaxPrice().doubleValue() || marketPrice < mineConfig.getMinPrice().doubleValue()) {
            log.info("close {} -{} mine, current price is {}", baseName, quotaName, marketPrice);
            mineConfig.setStatus(0);
            mineConfigMapper.updateByPrimaryKey(mineConfig);
            weiXinMessageService.sendMessage("Mine Close", "price:" + marketPrice);
        }

        //ft:usdt=1:0.6
        double initUsdt = maxNums.get(baseName.toLowerCase()) * initMultiple * marketPrice;
//
        //初始化
//        if (!(baseHold > 0 || quotaHold > 0)) {
//            if (isHaveInitBuyAndSell(baseBalance, quotaBalance, marketPrice, initUsdt, symbol, "limit",increment)) {
//                log.info("================有进行初始化均衡操作=================");
//                return;
//            }
//        }
//
        //买单 卖单
        double price = Math.min(maxNums.get(baseName.toLowerCase()) *  marketPrice, Math.min((baseBalance - baseHold) * marketPrice, quotaBalance - quotaHold));

        BigDecimal baseAmount = getNum(price * 0.99 / marketPrice);//预留点来扣手续费
        if (baseAmount.doubleValue() - minLimitPriceOrderNum < 0) {
            log.info("小于最小限价数量");
            return;
        }

        log.info("=============================交易对开始=========================");
//
        try {
            buyNotLimit(symbol, "limit", baseAmount, getMarketPrice(marketPrice * (1 - increment)));
        } catch (Exception e) {
            log.error("交易对买出错", e);
        }
        try {
            sellNotLimit(symbol, "limit", baseAmount, getMarketPrice(marketPrice * (1 + increment)));
        } catch (Exception e) {
            log.error("交易对卖出错", e);
        }
        log.info("=============================交易对结束=========================");

    }

    private double getRatio(CurrencyRatio currencyRatio, double marketPrice, boolean isTransfer) {
        double baseRatio = currencyRatio.getRatio();
//        if ("cac".equalsIgnoreCase(currencyRatio.getBaseCurrency())) {
//            if (marketPrice > 0.4) {
//                baseRatio = 0.2;
//            } else if (marketPrice > 0.35) {
//                baseRatio = 0.3;
//            } else if (marketPrice > 0.3) {
//                baseRatio = 0.35;
//            }  else if (marketPrice > 0.25) {
//                baseRatio = 0.4;
//            }  else if (marketPrice > 0.2) {
//                baseRatio = 0.45;
//            }  else if (marketPrice > 0.15) {
//                baseRatio = 0.5;
//            }  else if (marketPrice > 0.12) {
//                baseRatio = 0.6;
//            }  else if (marketPrice > 0.1) {
//                baseRatio = 0.7;
//            } else {
//                baseRatio = 0.8;
//            }
//        }
        //上涨10%，卖出， base减少
        if (marketPrice  > currencyRatio.getCurrentPrice()
                .multiply(new BigDecimal("1.05")).doubleValue()) {
            baseRatio  = baseRatio - 0.02;
            if (isTransfer && baseRatio > 0.50) {
                transfer(currencyRatio.getBaseCurrency(), currencyRatio.getQuotaCurrency(), baseRatio-0.50);
            }
        } else if (marketPrice  < currencyRatio.getCurrentPrice()
                .multiply(new BigDecimal("0.95")).doubleValue()) {
            //下跌10%，买入， base增加
            baseRatio  = baseRatio + 0.02;
            if (isTransfer && baseRatio < 0.5) {
                transfer(currencyRatio.getQuotaCurrency(), currencyRatio.getBaseCurrency(), 0.5 - baseRatio);
            }

        }
        return baseRatio;
    }


    public void transfer(String inCurrency, String outCurrency, double ratio) {

        List<Wallet> wallets = accountAPIService.getWallet("coinall", inCurrency);
        if (!CollectionUtils.isEmpty(wallets)) {
            Wallet wallet = wallets.get(0);
            if (wallet.getAvailable().doubleValue() > 0) {
                Transfer transferIn = new Transfer();
                transferIn.setCurrency(inCurrency);
                transferIn.setFrom(6);
                transferIn.setTo(1);
                BigDecimal available = wallet.getAvailable();
                if ("cac".equalsIgnoreCase(inCurrency)) {
                    available = available.subtract(new BigDecimal("4000"));
                }
                BigDecimal amount = available.multiply(BigDecimal.valueOf(ratio));

                transferIn.setAmount(amount);
                if (amount.doubleValue() > 0) {
                    accountAPIService.transfer("coinall", transferIn);
                }
                log.info("transfer {} {} from wallet to spot", amount, inCurrency);
            }
        }

        Account account = spotAccountAPIService.getAccountByCurrency("coinall", outCurrency);
        if (Objects.nonNull(account) && Double.parseDouble(account.getAvailable()) > 0) {

            Transfer transferOut = new Transfer();
            transferOut.setCurrency(outCurrency);
            transferOut.setFrom(1);
            transferOut.setTo(6);
            BigDecimal amount = new BigDecimal(account.getAvailable()).multiply(BigDecimal.valueOf(ratio));
            transferOut.setAmount(amount);
            accountAPIService.transfer("coinall", transferOut);
            log.info("transfer {} {} from spot to wallet", amount, outCurrency);

        }
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
    public void  mine3(String baseName, String quotaName, double increment) throws Exception {


        String symbol = baseName.toUpperCase() + "-" + quotaName.toUpperCase();
        cancelOrders(getNotTradeOrders(symbol, "0", "100"), 0);


        //查询余额
        Account baseAccount = getBalance(baseName);
        double baseHold = new BigDecimal(baseAccount.getBalance()).doubleValue() - new BigDecimal(baseAccount.getAvailable()).doubleValue();
        double baseBalance = new BigDecimal(baseAccount.getBalance()).doubleValue();
        double baseAvailable = new BigDecimal(baseAccount.getAvailable()).doubleValue();
        if (baseAvailable > 500) {
            return;
        }


        Account quotaAccount = getBalance(quotaName);
        double quotaHold = new BigDecimal(quotaAccount.getBalance()).doubleValue() - new BigDecimal(quotaAccount.getAvailable()).doubleValue();
        double quotaBalance = new BigDecimal(quotaAccount.getBalance()).doubleValue();
        double quotaAvailable = new BigDecimal(quotaAccount.getAvailable()).doubleValue();

        if (quotaAvailable > 100) {
            return;
        }

        Ticker ticker = getTicker(baseName, quotaName);
        Double marketPrice = Double.parseDouble(ticker.getLast());
        log.info("ticker last {} -{}:{}", baseName, quotaName, marketPrice);
        CurrencyRatio currencyRatio = currencyRatioMapper.getLatestRatio("coinall",
                baseName.toLowerCase(), quotaName.toLowerCase());
        if (Objects.isNull(currencyRatio)) {
            log.error("Get currency {}-{} ratio error", baseName, quotaName);
            throw new RuntimeException("Get currency ratio error");
        }
        log.info("origin base ratio:{}, price:{}", currencyRatio.getRatio(), currencyRatio.getCurrentPrice());

        double baseRatio = getRatio(currencyRatio, marketPrice, false);

        if (Math.abs(baseRatio - currencyRatio.getRatio()) > 0.001) {
            StringBuilder sb = new StringBuilder();
            sb.append("origin rate:");
            sb.append(currencyRatio.getRatio());
            sb.append(",");
            sb.append(currencyRatio.getCurrentPrice());

            log.info("current base ratio:{}, price:{}", baseRatio, marketPrice);
            CurrencyRatio currentRatio = CurrencyRatio.builder()
                    .site(currencyRatio.getSite())
                    .baseCurrency(currencyRatio.getBaseCurrency())
                    .quotaCurrency(currencyRatio.getQuotaCurrency())
                    .currentPrice(BigDecimal.valueOf(marketPrice))
                    .ratio(baseRatio)
                    .createTime(new Date())
                    .build();
            currencyRatioMapper.insert(currentRatio);
            sb.append("current rate:");
            sb.append(currentRatio.getRatio());
            sb.append(",");
            sb.append(currentRatio.getCurrentPrice());

            weiXinMessageService.sendMessage("ratio changed", sb.toString());
            return;
        }



        double allAsset= baseAvailable * marketPrice + quotaAvailable;
        log.info("basebalance:{}, qutobalance:{}, allAsset:{}, asset/2:{}, basebalance-quota:{}",
                baseAvailable, quotaAvailable, allAsset, allAsset*baseRatio, baseAvailable * marketPrice );

        BigDecimal quotaChange = null;
        BigDecimal baseChange = null;
        if (allAsset*baseRatio - baseAvailable * marketPrice  > allAsset * increment) {
            BigDecimal amount = new BigDecimal(allAsset*baseRatio -baseAvailable* marketPrice).setScale(numPrecision, BigDecimal.ROUND_FLOOR);
            log.info("basebalance:{}, quotabalance:{}", baseAvailable + amount.doubleValue(),
                    quotaAvailable - amount.doubleValue() * getMarketPrice(marketPrice).doubleValue());
            log.info("buy {}, price:{}", amount, marketPrice);
            //买入
            if (amount.doubleValue() - minLimitPriceOrderNum * marketPrice < 0) {
                log.info("小于最小限价数量");
            } else {
                BigDecimal baseamount = amount.divide(new BigDecimal(marketPrice),
                        numPrecision, BigDecimal.ROUND_DOWN);
                quotaChange = baseamount.multiply(getMarketPrice(marketPrice)).negate();
                baseChange = baseamount;
                buy(symbol, "limit", baseamount , getMarketPrice(marketPrice));//此处不需要重试，让上次去判断余额后重新平衡
            }
        }


        if (baseAvailable * marketPrice - allAsset * baseRatio > allAsset * increment) {
            //卖出
            BigDecimal amount = new BigDecimal(baseAvailable* marketPrice-allAsset * baseRatio).setScale(numPrecision, BigDecimal.ROUND_FLOOR);
            log.info("basebalance:{}, quotabalance:{}", baseAvailable - amount.doubleValue(),
                    quotaAvailable + amount.doubleValue() * getMarketPrice(marketPrice).doubleValue());
            log.info("sell {}, price:{}", amount, marketPrice);
            if (amount.doubleValue() - minLimitPriceOrderNum * marketPrice < 0) {
                log.info("小于最小限价数量");
            } else {
                BigDecimal baseamount = amount.divide(new BigDecimal(marketPrice),
                        numPrecision, BigDecimal.ROUND_DOWN);
                quotaChange = baseamount.multiply(getMarketPrice(marketPrice));
                baseChange = baseamount.negate();
                sell(symbol, "limit", baseamount, getMarketPrice(marketPrice));//此处不需要重试，让上次去判断余额后重新平衡

            }

        }

    }

    /**
     * 根据深度买入卖出
     *
     * @param baseName    交易币名称
     * @param quotaName  市场币名称
     * @param increment 收益率一半
     * @throws Exception
     */
    public void mine4(String baseName, String quotaName, double increment, double priceIncrement) throws Exception {
        String symbol = baseName.toUpperCase() + "-" + quotaName.toUpperCase();

        cancelOrders(getNotTradeOrders(symbol, "0", "100"), 10);
        //查询余额
        Account baseAccount = getBalance(baseName);
        double baseHold = new BigDecimal(baseAccount.getBalance()).doubleValue() - new BigDecimal(baseAccount.getAvailable()).doubleValue();
        double baseBalance = new BigDecimal(baseAccount.getBalance()).doubleValue();


        Account quotaAccount = getBalance(quotaName);
        double quotaHold = new BigDecimal(quotaAccount.getBalance()).doubleValue() - new BigDecimal(quotaAccount.getAvailable()).doubleValue();
        double quotaBalance = new BigDecimal(quotaAccount.getBalance()).doubleValue();


        //判断是否有冻结的，如果冻结太多冻结就休眠，进行下次挖矿
        if (baseHold > 0.99 * baseBalance
                && quotaHold > 0.99 * quotaBalance) {
            return;
        }

        log.info("===============balance: base:{},quota:{}========================", baseBalance, quotaBalance);

        Book book = getBook(baseName, quotaName);
        Random random = new Random();
        //int inc = random.nextInt(5) + 1;
        int inc = 1;
        Double sellPrice = Double.parseDouble(book.getAsks().get(0)[0]) - priceIncrement * inc;
        Double buyPrice = Double.parseDouble(book.getBids().get(0)[0]) + priceIncrement * inc;
        if (sellPrice <= buyPrice) {
            return;
        }
        //Double marketPrice = Double.parseDouble(ticker.getLast());
        log.info("book last {} -{}:{}-{}", baseName, quotaName, sellPrice, buyPrice);



        log.info("=============================交易对开始=========================");

        int r = random.nextInt(1000);
//
        try {
            //买单
            double price = Math.min(maxNums.get(baseName.toLowerCase()) *  buyPrice, quotaBalance - quotaHold);

            BigDecimal baseAmount = getNum(price * 0.99 / buyPrice);//预留点来扣手续费
            if (baseAmount.doubleValue() - minLimitPriceOrderNum < 0) {
                log.info("小于最小限价数量");
            } else {
                buyNotLimit(symbol, "limit", baseAmount.subtract(new BigDecimal(r * 0.01)),
                        getMarketPrice(buyPrice));
            }
        } catch (Exception e) {
            log.error("交易对买出错", e);
        }
        try {
            //买单
            double price = Math.min(maxNums.get(baseName.toLowerCase()) *  sellPrice, (baseBalance - baseHold) * sellPrice);

            BigDecimal baseAmount = getNum(price * 0.99 / sellPrice);//预留点来扣手续费
            if (baseAmount.doubleValue() - minLimitPriceOrderNum < 0) {
                log.info("小于最小限价数量");
                return;
            }
            sellNotLimit(symbol, "limit", baseAmount.subtract(new BigDecimal(r * 0.01)),
                    getMarketPrice(sellPrice));
        } catch (Exception e) {
            log.error("交易对卖出错", e);
        }
        log.info("=============================交易对结束=========================");

    }
    public boolean cancelOrders(List<OrderInfo> orderIds, int minutes) throws Exception {
        if (orderIds == null || orderIds.size() == 0) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        for (OrderInfo orderInfo : orderIds) {
            if (new BigDecimal(orderInfo.getSize()).compareTo(new BigDecimal(90)) >=0) {
                continue;
            }
            if (System.currentTimeMillis() - 8 * 3600 * 1000 - dateFormat.parse(orderInfo.getCreated_at()).getTime() < minutes * 60 * 1000) {
                continue;
            }
            PlaceOrderParam placeOrderParam = new PlaceOrderParam();
            placeOrderParam.setProduct_id(orderInfo.getProduct_id());
            spotOrderAPIService.cancleOrderByOrderId("coinall",placeOrderParam, orderInfo.getOrder_id());
        }
        return true;
    }

    public void buyNotLimit(String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        subBuy(amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
    }

    public void sellNotLimit(String symbol, String type, BigDecimal amount, BigDecimal marketPrice) throws Exception {
        subSell(amount.toString(), marketPrice.toString(), symbol, type, marketPrice.toPlainString());
    }

    private boolean isHaveInitBuyAndSell(double base, double quota, double marketPrice, double initUsdt, String symbol, String type, double increment) throws Exception {
        //初始化小的
        double baseValue = base * marketPrice;
        double num = Math.min((Math.abs(quota - baseValue) / 2), initUsdt);
        BigDecimal b = getNum(num / marketPrice);//现价的数量都为ft的数量
        if (b.doubleValue() - minLimitPriceOrderNum < 0) {
            log.info("小于最小限价数量");
            return false;
        }
        if (baseValue < quota && Math.abs(baseValue - quota) > 0.1 * (baseValue + quota)) {
            //买ft
            try {
                buy(symbol, type, b, getMarketPrice(marketPrice * (1-increment)));//此处不需要重试，让上次去判断余额后重新平衡
            } catch (Exception e) {
                log.error("初始化买有异常发生", e);
                throw new Exception(e);
            }

        } else if (quota < baseValue && Math.abs(baseValue - quota) > 0.1 * (baseValue + quota)) {
            //卖ft
            try {
                sell(symbol, type, b, getMarketPrice(marketPrice*(1+increment)));//此处不需要重试，让上次去判断余额后重新平衡
            } catch (Exception e) {
                log.error("初始化卖有异常发生", e);
                throw new Exception(e);
            }
        } else {
            return false;
        }

        Thread.sleep(100);
        return true;
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

            Thread.sleep(100);
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

            Thread.sleep(100);
        }
    }

    public void subSell(String amount, String price, String symbol, String type, String marketPrice) throws Exception {
        createOrder(amount, price, "sell", symbol, type, marketPrice);

    }

    public void subBuy(String amount, String price, String symbol, String type, String marketPrice) throws Exception {
        createOrder(amount, price, "buy", symbol, type, marketPrice);

    }

    private void createOrder(String amount, String price, String buy, String symbol, String type, String marketPrice) {
        PlaceOrderParam placeOrderParam = new PlaceOrderParam();
        placeOrderParam.setProduct_id(symbol);
        placeOrderParam.setPrice(price);
        placeOrderParam.setSize(amount);
        placeOrderParam.setSide(buy);
        placeOrderParam.setType(type);

        spotOrderAPIService.addOrder("coinall",placeOrderParam);
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
    public Account getBalance(String currency) throws Exception {

        return retryTemplate.execute(retryContext -> spotAccountAPIService.getAccountByCurrency("coinall", currency));
    }

    public Ticker getTicker(String baseCurrency, String quotaCurrency) {
        String symbol = baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase();
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Referer", "https://www.coinall.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url = "https://www.coinall.com/api/spot/v3/products/"+symbol+"/ticker";
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);

        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
        return JSON.parseObject(body,Ticker.class);

        //return spotProductAPIService.getTickerByProductId(baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase());
    }
    public Book getBook(String baseCurrency, String quotaCurrency) {
        String symbol = baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase();
        Book book = spotProductAPIService.bookProductsByProductId("coinall", symbol, 10, null);
        log.info("book+" + JSON.toJSONString(book));
        return book;
    }

    public List<OrderInfo> getNotTradeOrders(String symbol, String after, String limit) throws Exception {
        List<OrderInfo> list1 = getOrders(symbol, "open", after, limit, null);
        List<OrderInfo> list2 = getOrders(symbol, "part_filled", after, limit, null);
        list1.addAll(list2);
        return list1;
    }

    public List<OrderInfo> getOrders(String symbol, String states, String after, String limit, String side) throws Exception {
        return retryTemplate.execute(
                retryContext -> spotOrderAPIService.getOrders("coinall", symbol, states, null, null, null)
        );

    }

    public static void main(String[] args) {
//        new CoinAllMineService().getTicker("okb", "usdt");
        new CoinAllMineService().transfer("cac", "usdt", 0.001);
    }

    public void collectBalance() throws UnsupportedEncodingException {
        List<Account> accounts = spotAccountAPIService.getAccounts("coinall");
        StringBuilder sb = new StringBuilder();
        if (!CollectionUtils.isEmpty(accounts)) {
            for (Account account : accounts) {
                if (Double.parseDouble(account.getBalance()) > 0) {
                    sb.append(account.getCurrency() + ":" + account.getBalance() + "\r\n\r\n");
                }
            }
            weiXinMessageService.sendMessage("balance",  sb.toString());
        }
    }
}
