package com.cjie.cryptocurrency.quant.strategy.okex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.fcoin.FcoinRetry;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.result.Wallet;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Account;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.ResponseResult;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotAccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotOrderAPIServive;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.mapper.CurrencyRatioMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.CurrencyRatio;
import com.cjie.cryptocurrency.quant.model.MineConfig;
import com.cjie.cryptocurrency.quant.service.ApiKeyService;
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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private AccountAPIService accountAPIService;

    @Autowired
    private ApiKeyService  apiKeyService;

    private static final RetryTemplate retryTemplate = FcoinRetry.getRetryTemplate();



    private static double initMultiple = 3;

    private static double maxNum = 100;

    private static int numPrecision = 8;

    @Autowired
    private WeiXinMessageService weiXinMessageService;

    @Autowired
    private CurrencyRatioMapper currencyRatioMapper;

    private static Map<String, Double> minLimitPriceOrderNums = new HashMap<>();

    private static final int pricePrecision = 8;

    private static Map<String, Double> maxNums = new HashMap<>();


    static {
        minLimitPriceOrderNums.put("pax", 0.1);
        minLimitPriceOrderNums.put("eos", 0.1);
        minLimitPriceOrderNums.put("eth", 0.001);
        minLimitPriceOrderNums.put("ltc", 0.001);
        minLimitPriceOrderNums.put("okb", 1.0);
        minLimitPriceOrderNums.put("cac", 1.0);

        maxNums.put("eos", 0.3);
        maxNums.put("cac", 50.0);
        maxNums.put("okb", 3.0);
        maxNums.put("eth", 0.005);
        maxNums.put("pax", 1D);

    }

    public ValuationTicker getValuationTicker() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Referer", "www.okex.com");
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url =  "https://www.okex.com/v2/futures/market/indexTicker.do?symbol=f_usd_btc";
        RestTemplate client = new RestTemplate();
        log.info(url);
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
        ResponseResult<ValuationTicker> result = JSON.parseObject(body,new TypeReference<ResponseResult<ValuationTicker>>(){});
        return result.getData();

        //return spotProductAPIService.getTickerByProductId(baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase());
    }
    public void collectBalance() throws InterruptedException {

        StringBuilder sb = new StringBuilder();

        String[] sites = new String[]{"coinall", "okex", "oktop"};
        BigDecimal sum = BigDecimal.ZERO;
        ValuationTicker valuationTicker = getValuationTicker();
        for (String site  : sites) {
            BigDecimal spotAmount = getSpotValuation(site).setScale(8, RoundingMode.DOWN);
            BigDecimal spotUsdtAmount = spotAmount.multiply(valuationTicker.getLast()).setScale(8, RoundingMode.DOWN);
            BigDecimal spotCnyAmount = spotUsdtAmount.multiply(valuationTicker.getUsdCnyRate()).setScale(8, RoundingMode.DOWN);
            sb.append(site).append(":").append("spot:").append(spotAmount)
                    .append("usdt:").append(spotUsdtAmount)
                    .append("cny:").append(spotCnyAmount)
                    .append("\r\n\r\n");
            sum = sum.add(spotAmount);
            BigDecimal walletAmount = getWalletValuation(site).setScale(8, RoundingMode.DOWN);
            BigDecimal walletUsdtAmount = walletAmount.multiply(valuationTicker.getLast()).setScale(8, RoundingMode.DOWN);
            BigDecimal walletCnyAmount = walletUsdtAmount.multiply(valuationTicker.getUsdCnyRate()).setScale(8, RoundingMode.DOWN);
            sb.append(site).append(":").append("wallet:").append(walletAmount)
                    .append("usdt:").append(walletUsdtAmount)
                    .append("cny:").append(walletCnyAmount)
                    .append("\r\n\n");
            sum = sum.add(walletAmount);
        }
        sum = sum.setScale(8, RoundingMode.DOWN);
        BigDecimal usdtSum = sum.multiply(valuationTicker.getLast()).setScale(8, RoundingMode.DOWN);
        BigDecimal cnySum = usdtSum.multiply(valuationTicker.getUsdCnyRate()).setScale(8, RoundingMode.DOWN);
        sb.append("all").append(":").append(sum).append("usdt:").append(usdtSum).append("cny:").append(cnySum);
        weiXinMessageService.sendMessage("balance",  sb.toString());

    }

    private BigDecimal getSpotValuation(String site) throws InterruptedException {
        List<String> noValueCurrencies = new ArrayList<>();
        noValueCurrencies.add("NGC");
        noValueCurrencies.add("MAG");
        noValueCurrencies.add("AUTO");
        noValueCurrencies.add("GSC");
        noValueCurrencies.add("BCH");

        BigDecimal sum = BigDecimal.ZERO;
        List<Account> accounts = spotAccountAPIService.getAccounts(site);
        if (!CollectionUtils.isEmpty(accounts)) {
            for (Account account : accounts) {

                if (noValueCurrencies.contains(account.getCurrency().toUpperCase())) {
                    continue;
                }
                if (Double.parseDouble(account.getBalance()) > 0) {
                    if ("usdt".equalsIgnoreCase(account.getCurrency())) {
                        Ticker ticker = retryTemplate.execute(retryContext->getTicker(site, "btc", "usdt"));
                        sum = sum.add(new BigDecimal(account.getBalance()).divide(new BigDecimal(ticker.getLast()), 8, RoundingMode.DOWN));

                    } else if (!"btc".equalsIgnoreCase(account.getCurrency())) {
                        Ticker ticker = retryTemplate.execute(retryContext->getTicker(site, account.getCurrency(), "btc"));
                        sum = sum.add(new BigDecimal(account.getBalance()).multiply(new BigDecimal(ticker.getLast())));
                    } else {
                        sum = sum.add(new BigDecimal(account.getBalance()));
                    }
                }
                Thread.sleep(200);
            }
        }
        return sum;
    }

    private BigDecimal getWalletValuation(String site) throws InterruptedException {
        List<String> noValueCurrencies = new ArrayList<>();
        noValueCurrencies.add("EON");
        noValueCurrencies.add("ADD");
        noValueCurrencies.add("CHL");
        noValueCurrencies.add("EOP");
        noValueCurrencies.add("EOX");
        noValueCurrencies.add("HORUS");
        noValueCurrencies.add("IQ");
        noValueCurrencies.add("MEETONE");
        noValueCurrencies.add("HYC");
        noValueCurrencies.add("SDA");
        noValueCurrencies.add("ONG");
        noValueCurrencies.add("MAG");
        noValueCurrencies.add("NGC");
        noValueCurrencies.add("AUTO");
        noValueCurrencies.add("GSC");
        noValueCurrencies.add("BCH");




        BigDecimal sum = BigDecimal.ZERO;
        List<Wallet> accounts = accountAPIService.getWallet(site);
        if (!CollectionUtils.isEmpty(accounts)) {
            for (Wallet account : accounts) {
                if (noValueCurrencies.contains(account.getCurrency().toUpperCase())) {
                    continue;
                }
                if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                    if ("usdt".equalsIgnoreCase(account.getCurrency())) {
                        Ticker ticker = retryTemplate.execute(retryContext->getTicker(site, "btc", "usdt"));
                        sum = sum.add(account.getBalance().divide(new BigDecimal(ticker.getLast()), 8, RoundingMode.DOWN));

                    } else if (!"btc".equalsIgnoreCase(account.getCurrency())) {
                        Ticker ticker = retryTemplate.execute(retryContext->getTicker(site, account.getCurrency(), "btc"));
                        sum = sum.add(account.getBalance().multiply(new BigDecimal(ticker.getLast())));
                    } else {
                        sum = sum.add(account.getBalance());
                    }
                }
                Thread.sleep(200);
            }
        }
        return sum;
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
        if (baseHold > 0.999 * baseBalance
                && quotaHold > 0.999 * quotaBalance) {
            return;
        }

        log.info("===============balance: base:{},quota:{}========================", baseBalance, quotaBalance);

        Ticker ticker = getTicker(site, baseName, quotaName);
        Double marketPrice = Double.parseDouble(ticker.getLast());
        log.info("ticker last {} -{}:{}", baseName, quotaName, marketPrice);
        //usdt小于51并且ft的价值小于51

        //ft:usdt=1:0.6
        double initUsdt = maxNums.get(baseName.toLowerCase()) * initMultiple * marketPrice;
//
        //买单 卖单
        double price = Math.min(maxNums.get(baseName.toLowerCase()) * marketPrice, Math.min((baseBalance - baseHold) * marketPrice, quotaBalance - quotaHold));

        BigDecimal baseAmount = getNum(price * 0.99 / marketPrice);//预留点来扣手续费
        if (baseAmount.doubleValue() - minLimitPriceOrderNums.get(baseName) < 0) {
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

    private double getRatio(CurrencyRatio currencyRatio, double marketPrice, boolean isTransfer) {
        double baseRatio = currencyRatio.getRatio();
        if (baseRatio >= 0.99) {
            return baseRatio;
        }
        if (baseRatio <= 0.01) {
            return baseRatio;
        }//        if ("cac".equalsIgnoreCase(currencyRatio.getBaseCurrency())) {
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
                .multiply(new BigDecimal("1.1")).doubleValue()) {
            baseRatio  = baseRatio - 0.03;
            if (isTransfer) {
                transfer(currencyRatio.getSite(), currencyRatio.getBaseCurrency(), currencyRatio.getQuotaCurrency(), 0.05);
            }
        } else if (marketPrice  < currencyRatio.getCurrentPrice()
                .multiply(new BigDecimal("0.9")).doubleValue()) {
            //下跌10%，买入， base增加
            baseRatio  = baseRatio + 0.03;
            if (isTransfer) {
                transfer(currencyRatio.getSite(), currencyRatio.getQuotaCurrency(), currencyRatio.getBaseCurrency(), 0.05);
            }

        }
        return baseRatio;
    }


    public void transfer(String site, String inCurrency, String outCurrency, double ratio) {

        List<Wallet> wallets = accountAPIService.getWallet(site, inCurrency);
        if (!CollectionUtils.isEmpty(wallets)) {
            Wallet wallet = wallets.get(0);
            if (wallet.getAvailable().doubleValue() > 0) {
                Transfer transferIn = new Transfer();
                transferIn.setCurrency(inCurrency);
                transferIn.setFrom(6);
                transferIn.setTo(1);
                BigDecimal amount = wallet.getAvailable().multiply(BigDecimal.valueOf(ratio));
                transferIn.setAmount(amount);
                accountAPIService.transfer("coinall", transferIn);
                log.info("transfer {} {} from wallet to spot", amount, inCurrency);
            }
        }

        Account account = spotAccountAPIService.getAccountByCurrency(site, outCurrency);
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

//        if ("okb".equalsIgnoreCase(baseName)) {
//
//            CurrencyRatio currencyRatio = currencyRatioMapper.getLatestRatio(site,
//                    baseName.toLowerCase(), quotaName.toLowerCase());
//            if (Objects.isNull(currencyRatio)) {
//                log.error("Get currency {}-{} ratio error", baseName, quotaName);
//                throw new RuntimeException("Get currency ratio error");
//            }
//            log.info("origin base ratio:{}, price:{}", currencyRatio.getRatio(), currencyRatio.getCurrentPrice());
//
//            baseRatio = getRatio(currencyRatio, marketPrice, false);
//
//            if (Math.abs(baseRatio - currencyRatio.getRatio()) > 0.001) {
//                if (baseRatio >= 0.99 || baseRatio <=0.01) {
//                    return;
//                }
//                StringBuilder sb = new StringBuilder();
//                sb.append("origin rate:");
//                sb.append(currencyRatio.getRatio());
//                sb.append(",");
//                sb.append(currencyRatio.getCurrentPrice());
//
//                log.info("current base ratio:{}, price:{}", baseRatio, marketPrice);
//                CurrencyRatio currentRatio = CurrencyRatio.builder()
//                        .site(currencyRatio.getSite())
//                        .baseCurrency(currencyRatio.getBaseCurrency())
//                        .quotaCurrency(currencyRatio.getQuotaCurrency())
//                        .currentPrice(BigDecimal.valueOf(marketPrice))
//                        .ratio(baseRatio)
//                        .createTime(new Date())
//                        .build();
//                currencyRatioMapper.insert(currentRatio);
//                sb.append("current rate:");
//                sb.append(currentRatio.getRatio());
//                sb.append(",");
//                sb.append(currentRatio.getCurrentPrice());
//
//                weiXinMessageService.sendMessage("ratio changed", sb.toString());
//                return;
//            }
//        }

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
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);
        log.info(url);
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
        return JSON.parseObject(body,Ticker.class);

        //return spotProductAPIService.getTickerByProductId(baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase());
    }

    public List<OrderInfo> getNotTradeOrders(String site, String symbol, String after, String limit) throws Exception {
        List<OrderInfo> list1 = new ArrayList<>();
        List<OrderInfo> list2 = new ArrayList<>();
        try {
            list1 = getOrders(site, symbol, "open", after, limit, null);
            list2 = getOrders(site, symbol, "part_filled", after, limit, null);
        } catch (Exception e) {
            log.error("get order error",e);
        }
        list1.addAll(list2);
        return list1;
    }

    public List<OrderInfo> getOrders(String site, String symbol, String states, String after, String limit, String side) throws Exception {
        return retryTemplate.execute(retryContext -> spotOrderAPIService.getOrders(site, symbol, states, null, null, null));

    }

    public static void main(String[] args) {
        new MineService().getTicker("oktop","top", "usdt");
    }

}
