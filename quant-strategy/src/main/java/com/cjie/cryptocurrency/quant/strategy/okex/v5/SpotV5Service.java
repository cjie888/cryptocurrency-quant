package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.FundsTransfer;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.PiggyBankPurchaseRedemption;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.CancelOrder;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.PlaceOrder;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.FundingAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.trade.TradeAPIService;
import com.cjie.cryptocurrency.quant.mapper.SpotOrderMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.SpotOrder;
import com.cjie.cryptocurrency.quant.service.ApiKeyService;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class SpotV5Service {


    @Autowired
    private TradeAPIService tradeAPIService;

    @Autowired
    private SpotOrderMapper spotOrderMapper;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private FundingAPIService fundingAPIService;

    @Autowired
    private AccountAPIV5Service accountAPIService;

    @Autowired
    private WeiXinMessageService weiXinMessageService;


    private static Map<String, Integer> STATES = Maps.newHashMap();

    static {
//        canceled：撤单成功
//        live：等待成交
//        partially_filled：部分成交
//        filled：完全成交

        STATES.put("live", 0);
        STATES.put("partially_filled", 1);
        STATES.put("filled", 2);
        STATES.put("canceled", -1);
    }


    public void netGrid(String site, String symbol, String size, Double increment) {


        //获取等待提交订单
        List<Integer> unProcessedStatuses = new ArrayList<>();
        unProcessedStatuses.add(99);
        unProcessedStatuses.add(0);
        unProcessedStatuses.add(1);
        try {
            List<SpotOrder> spotOrders = spotOrderMapper.selectByStatus(symbol, "netGrid", unProcessedStatuses);
            if (CollectionUtils.isNotEmpty(spotOrders)) {
                log.info("unprocessed spot orders {}", JSON.toJSONString(spotOrders));
                for (SpotOrder spotOrder : spotOrders) {
                    JSONObject result = tradeAPIService.getOrderDetails(site, symbol, spotOrder.getOrderId(), null);

                    log.info("spot order status {}", JSON.toJSONString(result));
                    if (result == null) {
                        return;
                    }
                    String state = ((JSONObject)result.getJSONArray("data").get(0)).getString("state");
                    if ( state == null || STATES.get(state) == null) {
                        return;
                    }
                    Integer status = STATES.get(state);
                    if (!spotOrder.getStatus().equals(status)) {
                        spotOrderMapper.updateStatus(spotOrder.getOrderId(), status);
                    }
                }
            }
        } catch (Exception e) {
            log.info("update status error, symbol:{}", symbol, e);
            return;
        }

        List<Integer> unSettledStatuses = new ArrayList<>();
        unSettledStatuses.add(1);
        List<SpotOrder> unSettledOrders = spotOrderMapper.selectByStatus(symbol, "netGrid", unSettledStatuses);
        if (CollectionUtils.isNotEmpty(unSettledOrders)) {
            for (SpotOrder spotOrder : unSettledOrders) {
                if (System.currentTimeMillis() - 30 * 60 * 1000L > spotOrder.getCreateTime().getTime()) {
                    CancelOrder cancelOrder = new CancelOrder();
                    cancelOrder.setInstId(symbol);
                    cancelOrder.setOrdId(spotOrder.getOrderId());
                    tradeAPIService.cancelOrder(site, cancelOrder);
                    log.info("取消部分成交订单{}-{}", symbol, spotOrder.getOrderId());
                }
            }
            return;
        }

        List<Integer> unSelledStatuses = new ArrayList<>();
        unSelledStatuses.add(0);
        List<SpotOrder> unSelledOrders = spotOrderMapper.selectByStatus(symbol, "netGrid", unSelledStatuses);
        if (CollectionUtils.isNotEmpty(unSelledOrders)) {
            for (SpotOrder spotOrder : unSelledOrders) {
                CancelOrder cancelOrder = new CancelOrder();
                cancelOrder.setInstId(symbol);
                cancelOrder.setOrdId(spotOrder.getOrderId());
                tradeAPIService.cancelOrder(site, cancelOrder);
                log.info("取消未成交订单{}-{}", symbol, spotOrder.getOrderId());
            }
        }

        Ticker spotTicker = getTicker(site, symbol);
        log.info("当前价格{}-{}", site, spotTicker.getLast());

        SpotOrder lastOrder = null;
        List<Integer> selledStatuses = new ArrayList<>();
        selledStatuses.add(2);
        List<SpotOrder> selledOrders = spotOrderMapper.selectByStatus(symbol, "netGrid", selledStatuses);
        if (CollectionUtils.isNotEmpty(selledOrders)) {
            for (SpotOrder spotOrder : selledOrders) {
                if (lastOrder == null) {
                    lastOrder = spotOrder;
                    break;
                }
            }
        }
        Double currentPrice = Double.valueOf(spotTicker.getLast());

        String baseCurrency = symbol.substring(0, symbol.indexOf("-"));
        String quotaCurrency = symbol.substring(symbol.indexOf("-") + 1);


        HttpResult<List<AccountInfo>> baseAccountResult = accountAPIService.getBalance(site, baseCurrency);
        if (Objects.nonNull(baseAccountResult) && "0".equals(baseAccountResult.getCode())
                && (baseAccountResult.getData().get(0).getDetails().size() == 0
                || Double.parseDouble(baseAccountResult.getData().get(0).getDetails().get(0).getAvailBal()) < Double.parseDouble(size) * 1.01)) {

            BigDecimal transferAmount = new BigDecimal(size).multiply(new BigDecimal("1.01"));
            try {
                PiggyBankPurchaseRedemption piggyBankPurchaseRedemption = new PiggyBankPurchaseRedemption();
                piggyBankPurchaseRedemption.setCcy(baseCurrency);
                piggyBankPurchaseRedemption.setAmt(transferAmount.toPlainString());
                piggyBankPurchaseRedemption.setSide("redempt");
                JSONObject result1 = fundingAPIService.piggyBankPurchaseRedemption(site, piggyBankPurchaseRedemption);
                log.info("transfer {} {} from financial to asset", transferAmount, JSON.toJSONString(result1));
                Thread.sleep(1000);
            } catch (Exception e) {
                //ignore
            }
            FundsTransfer transferIn = new FundsTransfer();
            transferIn.setCcy(baseCurrency);
            transferIn.setFrom("6");
            transferIn.setTo("18");
            transferIn.setAmt(transferAmount.toPlainString());
            try {
                JSONObject transferResult = fundingAPIService.fundsTransfer(site, transferIn);
                log.info("transfer {} {} from asset to spot,result:{}", size, baseCurrency, JSON.toJSONString(transferResult));
            } catch (Exception e) {
                log.info("transfer {} {} from asset to spot error", size, baseCurrency, e);
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //ignore
            }

        }

        baseAccountResult = accountAPIService.getBalance(site, baseCurrency);
        if (Objects.nonNull(baseAccountResult) && "0".equals(baseAccountResult.getCode()) &&
                (baseAccountResult.getData().get(0).getDetails().size() == 0 ||
                        Double.parseDouble(baseAccountResult.getData().get(0).getDetails().get(0).getAvailBal()) < Double.parseDouble(size) * 1.01)) {
            //3倍买入

            //{
            //    "instId":"BTC-USDT",
            //    "tdMode":"cash",
            //    "clOrdId":"b15",
            //    "side":"buy",
            //    "ordType":"limit",
            //    "px":"2.15",
            //    "sz":"2"
            //}
            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(symbol);
            placeOrderParam.setTdMode("cash");
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(spotTicker.getLast())));
            placeOrderParam.setSz(new BigDecimal(size).multiply(new BigDecimal("3")).toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setOrdType("limit");

            JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
            log.info("买入{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));
            if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size).multiply(new BigDecimal("3")));
                spotOrder.setOrderId(String.valueOf(((JSONObject)orderResult.getJSONArray("data").get(0)).getString("ordId")));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }

            return;
        }

        HttpResult<List<AccountInfo>> quotaAccountResult = accountAPIService.getBalance(site, quotaCurrency);
        if (Objects.nonNull(quotaAccountResult) && "0".equals(quotaAccountResult.getCode())
               && (quotaAccountResult.getData().get(0).getDetails().size() == 0 ||
                Double.parseDouble(quotaAccountResult.getData().get(0).getDetails().get(0).getAvailBal()) < Double.parseDouble(size) * currentPrice * 1.01 * 3))
        {

            BigDecimal transferAmount = new BigDecimal(size).multiply(new BigDecimal(spotTicker.getLast())).multiply(new BigDecimal("1.01"));
            PiggyBankPurchaseRedemption piggyBankPurchaseRedemption = new PiggyBankPurchaseRedemption();
            piggyBankPurchaseRedemption.setCcy(quotaCurrency);
            piggyBankPurchaseRedemption.setAmt(transferAmount.toPlainString());
            piggyBankPurchaseRedemption.setSide("redempt");
            JSONObject result1 = fundingAPIService.piggyBankPurchaseRedemption(site, piggyBankPurchaseRedemption);
            log.info("transfer {} {} from financial to asset", transferAmount, JSON.toJSONString(result1));
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                //ignore
            }
            try {
                FundsTransfer transferIn = new FundsTransfer();
                transferIn.setCcy(quotaCurrency);
                transferIn.setFrom("9");
                transferIn.setTo("18");
                transferIn.setAmt(transferAmount.toPlainString());
                JSONObject transferResult = fundingAPIService.fundsTransfer(site, transferIn);
                log.info("transfer {} {} from asset to spot,result:{}", size, baseCurrency, JSON.toJSONString(transferResult));
            } catch (Exception e) {
                //ignore
                log.error("transfer {} {} from asset to spot error", transferAmount, quotaCurrency, e);
            }

        }

        if (lastOrder == null) {
            //买入

            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(symbol);
            placeOrderParam.setTdMode("cash");
            //placeOrderParam.setPx(spotTicker.getLast());
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(spotTicker.getLast())));

            placeOrderParam.setSz(new BigDecimal(size).multiply(new BigDecimal("1.01")).toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setOrdType("limit");


            JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
            log.info("买入{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));
            if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size).multiply(new BigDecimal("1.01")));
                spotOrder.setOrderId(String.valueOf(((JSONObject)orderResult.getJSONArray("data").get(0)).getString("ordId")));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }

            return;

        }
        Double lastPrice = lastOrder.getPrice().doubleValue();
        log.info("当前价格：{}, 上次价格:{}", currentPrice, lastPrice);
        if (currentPrice > lastPrice && (currentPrice - lastPrice) / lastPrice > increment) {
            //价格上涨
            //获取最新成交多单
            //卖出

            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(symbol);
            placeOrderParam.setTdMode("cash");
            //placeOrderParam.setPx(spotTicker.getLast());
            placeOrderParam.setSz(size);
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(spotTicker.getLast())));

            placeOrderParam.setSide("sell");
            placeOrderParam.setOrdType("limit");

            JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
            log.info("卖出{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
            if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("2"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size));
                spotOrder.setOrderId(String.valueOf(((JSONObject)orderResult.getJSONArray("data").get(0)).getString("ordId")));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }
            return;

        }
        if (currentPrice < lastPrice && (lastPrice - currentPrice) / lastPrice > increment) {
            //价格下跌
            //获取最新成交空单
            //买入


            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(symbol);
            placeOrderParam.setTdMode("cash");
            //placeOrderParam.setPx(spotTicker.getLast());
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(spotTicker.getLast())));

            placeOrderParam.setSz(new BigDecimal(size).multiply(new BigDecimal("1.01")).toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setOrdType("limit");

            JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
            log.info("买入{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
            if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size).multiply(new BigDecimal("1.01")));
                spotOrder.setOrderId(String.valueOf(((JSONObject)orderResult.getJSONArray("data").get(0)).getString("ordId")));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }
        }

    }

    public Ticker getTicker(String site, String symbol) {

//        {"code":"0","msg":"","data":[{"instType":"SPOT","instId":"BTC-USDT","last":"49518.2","lastSz":"0.00001",
//                "askPx":"49518.3","askSz":"3.79193808","bidPx":"49518.2","bidSz":"0.24591572","open24h":"48479.4",
//                "high24h":"49683","low24h":"48158.2","volCcy24h":"452055919.14128222","vol24h":"9233.29462214",
//                "ts":"1639311735562","sodUtc0":"49388.2","sodUtc8":"48752.4"}]}
        //https://www.okex.com/api/v5/market/ticker?instId=BTC-USDT
        MultiValueMap<String, String> headers = new HttpHeaders();
        APIKey apiKey = apiKeyService.getApiKey(site);
        headers.add("Referer", apiKey.getDomain());
        headers.add("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36");
        HttpEntity requestEntity = new HttpEntity<>(headers);

        String url = apiKey.getDomain() + "api/v5/market/ticker?instId=" + symbol;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);// 设置超时
        requestFactory.setReadTimeout(10000);
        RestTemplate client = new RestTemplate(requestFactory);
        log.info(url);
        client.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
        String body = response.getBody();
        log.info(body);
        JSONObject res = JSON.parseObject(body);
        if (res != null && res.get("data") != null) {
            JSONArray data = res.getJSONArray("data");
            return JSON.parseObject(JSON.toJSONString(data.get(0)), Ticker.class);

        }
        return null;
        //return spotProductAPIService.getTickerByProductId(baseCurrency.toUpperCase() + "-" + quotaCurrency.toUpperCase());
    }


}
