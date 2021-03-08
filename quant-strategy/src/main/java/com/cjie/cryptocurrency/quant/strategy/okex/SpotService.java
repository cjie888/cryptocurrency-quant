package com.cjie.cryptocurrency.quant.strategy.okex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.param.PlaceOrderParam;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Account;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderInfo;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.OrderResult;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrder;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.ApiOrderResultVO;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.ApiPositionVO;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.ApiPositionsVO;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.ApiTickerVO;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotAccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotOrderAPIServive;
import com.cjie.cryptocurrency.quant.mapper.SpotOrderMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.SpotOrder;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.ApiKeyService;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import javafx.scene.layout.BackgroundImage;
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
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class SpotService {


    @Autowired
    private SpotOrderAPIServive spotOrderAPIServive;

    @Autowired
    private SpotOrderMapper spotOrderMapper;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private SpotAccountAPIService spotAccountAPIService;

    @Autowired
    private AccountAPIService accountAPIService;

    @Autowired
    private WeiXinMessageService weiXinMessageService;


    public void netGrid(String site, String symbol, String size, Double increment) {


        //获取等待提交订单
        List<Integer> unProcessedStatuses = new ArrayList<>();
        unProcessedStatuses.add(99);
        unProcessedStatuses.add(0);
        unProcessedStatuses.add(1);
        try {
            List<SpotOrder> swapOrders = spotOrderMapper.selectByStatus(symbol, "netGrid", unProcessedStatuses);
            if (CollectionUtils.isNotEmpty(swapOrders)) {
                log.info("unprocessed spot orders {}", JSON.toJSONString(swapOrders));
                for (SpotOrder swapOrder : swapOrders) {
                    OrderInfo result = spotOrderAPIServive.getOrderByOrderId(site, symbol, Long.parseLong(swapOrder.getOrderId()));

                    log.info("spot order status {}", JSON.toJSONString(result));
                    if (result == null) {
                        return;
                    }
                    Integer status = Integer.parseInt(result.getState());
                    if (!swapOrder.getStatus().equals(status)) {
                        spotOrderMapper.updateStatus(swapOrder.getOrderId(), status);
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
                if (System.currentTimeMillis() - 30 * 60 * 1000L > spotOrder.getCreateTime().getTime() ) {
                    spotOrderAPIServive.cancleOrderByOrderId(site, symbol, Long.parseLong(spotOrder.getOrderId()));
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
                spotOrderAPIServive.cancleOrderByOrderId(site, symbol, Long.parseLong(spotOrder.getOrderId()));
                log.info("取消未成交订单{}-{}", symbol, spotOrder.getOrderId());
            }
        }

        Ticker spotTicker = getTicker(site,symbol);
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
        String quotaCurrency = symbol.substring(symbol.indexOf("-")+1);


        Account baseAccount = spotAccountAPIService.getAccountByCurrency(site, baseCurrency);
        if (Objects.nonNull(baseAccount) && Double.parseDouble(baseAccount.getAvailable()) <  Double.parseDouble(size) * 1.01) {

            BigDecimal transferAmount = new BigDecimal(size).multiply(new BigDecimal("1.01"));
            JSONObject result1 = accountAPIService.purchaseRedempt(site, baseCurrency, transferAmount.toPlainString(), "redempt");
            log.info("transfer {} {} from financial to asset", transferAmount, JSON.toJSONString(result1));
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                //ignore
            }
            Transfer transferIn = new Transfer();
            transferIn.setCurrency(baseCurrency);
            transferIn.setFrom(6);
            transferIn.setTo(1);
            transferIn.setAmount(transferAmount);
            try {
                accountAPIService.transfer(site, transferIn);
                log.info("transfer {} {} from asset to spot", size, baseCurrency);
            } catch (Exception e) {
                log.info("transfer {} {} from asset to spot error", size, baseCurrency, e);
            }
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                //ignore
            }

        }

        baseAccount = spotAccountAPIService.getAccountByCurrency(site, baseCurrency);
        if (Objects.nonNull(baseAccount) && Double.parseDouble(baseAccount.getAvailable()) <  Double.parseDouble(size) * 1.01) {
            //3倍买入
            PlaceOrderParam placeOrderParam = new PlaceOrderParam();
            placeOrderParam.setProduct_id(symbol);
            placeOrderParam.setPrice(spotTicker.getLast());
            placeOrderParam.setSize(new BigDecimal(size).multiply(new BigDecimal("3")).toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setType("limit");

            OrderResult orderResult = spotOrderAPIServive.addOrder(site,placeOrderParam);
            log.info("买入{}-{}", symbol, JSON.toJSONString(placeOrderParam));
            if (orderResult.isResult()) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size).multiply(new BigDecimal("3")));
                spotOrder.setOrderId(String.valueOf(orderResult.getOrder_id()));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }

            return;
        }

            Account quotaAccount = spotAccountAPIService.getAccountByCurrency(site, quotaCurrency);
        if (Objects.nonNull(quotaAccount) && Double.parseDouble(quotaAccount.getAvailable()) <  Double.parseDouble(size) * currentPrice * 1.01) {

            BigDecimal transferAmount = new BigDecimal(size).multiply(new BigDecimal(spotTicker.getLast())).multiply(new BigDecimal("1.01"));
            JSONObject result1 = accountAPIService.purchaseRedempt(site, quotaCurrency, transferAmount.toPlainString(), "redempt");
            log.info("transfer {} {} from financial to asset", transferAmount, JSON.toJSONString(result1));
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                //ignore
            }
            Transfer transferIn = new Transfer();
            transferIn.setCurrency(quotaCurrency);
            transferIn.setFrom(6);
            transferIn.setTo(1);
            transferIn.setAmount(transferAmount);
            accountAPIService.transfer(site, transferIn);
            log.info("transfer {} {} from asset to spot", Double.parseDouble(size) * currentPrice , quotaCurrency);

        }

        if (lastOrder == null) {
            //买入
            PlaceOrderParam placeOrderParam = new PlaceOrderParam();
            placeOrderParam.setProduct_id(symbol);
            placeOrderParam.setPrice(spotTicker.getLast());
            placeOrderParam.setSize(new BigDecimal(size).multiply(new BigDecimal("1.01")).toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setType("limit");

            OrderResult orderResult = spotOrderAPIServive.addOrder(site,placeOrderParam);
            log.info("买入{}-{}", symbol, JSON.toJSONString(placeOrderParam));
            if (orderResult.isResult()) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size).multiply(new BigDecimal("1.01")));
                spotOrder.setOrderId(String.valueOf(orderResult.getOrder_id()));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }

            return;

        }
        Double lastPrice = lastOrder.getPrice().doubleValue();
        log.info("当前价格：{}, 上次价格:{}", currentPrice, lastPrice);
        if (currentPrice > lastPrice && (currentPrice - lastPrice)/lastPrice > increment ) {
            //价格上涨
            //获取最新成交多单
            //卖出
            PlaceOrderParam placeOrderParam = new PlaceOrderParam();
            placeOrderParam.setProduct_id(symbol);
            placeOrderParam.setPrice(spotTicker.getLast());
            placeOrderParam.setSize(size);
            placeOrderParam.setSide("sell");
            placeOrderParam.setType("limit");

            OrderResult orderResult = spotOrderAPIServive.addOrder(site,placeOrderParam);
            log.info("卖出{}-{}", symbol, JSON.toJSONString(placeOrderParam));
            if (orderResult.isResult()) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("2"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size));
                spotOrder.setOrderId(String.valueOf(orderResult.getOrder_id()));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }
            return;

        }
        if (currentPrice < lastPrice && (lastPrice - currentPrice)/lastPrice > increment ) {
            //价格下跌
            //获取最新成交空单
            //买入
            PlaceOrderParam placeOrderParam = new PlaceOrderParam();
            placeOrderParam.setProduct_id(symbol);
            placeOrderParam.setPrice(spotTicker.getLast());
            placeOrderParam.setSize(new BigDecimal(size).multiply(new BigDecimal("1.01")).toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setType("limit");

            OrderResult orderResult = spotOrderAPIServive.addOrder(site,placeOrderParam);
            log.info("买入{}-{}", symbol, JSON.toJSONString(placeOrderParam));
            if (orderResult.isResult()) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("netGrid");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(spotTicker.getLast()));
                spotOrder.setSize(new BigDecimal(size).multiply(new BigDecimal("1.01")));
                spotOrder.setOrderId(String.valueOf(orderResult.getOrder_id()));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }
        }

    }

    public Ticker getTicker(String site, String symbol) {
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

    public void computeBenefit(String title, LocalDateTime startTime, LocalDateTime endTime) {
        List<SpotOrder> spotOrders = spotOrderMapper.groupBySymbol(startTime, endTime);
        log.info("spot orders:{}", JSON.toJSONString(spotOrders));
        Map<String, Integer> buyCounts =  new HashMap<>();
        Map<String, Integer> sellCounts =  new HashMap<>();

        Map<String, BigDecimal> buySums =  new HashMap<>();
        Map<String, BigDecimal> sellSums =  new HashMap<>();

        Map<String, BigDecimal> buyAmounts =  new HashMap<>();
        Map<String, BigDecimal> sellAmounts =  new HashMap<>();

        if (CollectionUtils.isNotEmpty(spotOrders)) {
            int buyCount = 0;
            int sellCount = 0;
            for (SpotOrder spotOrder : spotOrders) {
                if (spotOrder.getIsMock() == Byte.valueOf("1")) {
                    continue;
                }
                if (spotOrder.getStatus() != 2) {
                    continue;
                }
                if (spotOrder.getType() == Byte.valueOf("1")) {
                    buyCount++;
                    Integer symBuyCount = buyCounts.get(spotOrder.getSymbol());
                    if (symBuyCount == null) {
                        symBuyCount = 0;
                    }
                    buyCounts.put(spotOrder.getSymbol(), symBuyCount + 1);

                    BigDecimal symBuyAmount = buyAmounts.get(spotOrder.getSymbol());
                    if (symBuyAmount == null) {
                        symBuyAmount = BigDecimal.ZERO;
                    }
                    buyAmounts.put(spotOrder.getSymbol(), symBuyAmount.add(spotOrder.getSize()));

                    BigDecimal symBuySum = buySums.get(spotOrder.getSymbol());
                    if (symBuySum == null) {
                        symBuySum = BigDecimal.ZERO;
                    }
                    buySums.put(spotOrder.getSymbol(), symBuySum.add(spotOrder.getSize().multiply(spotOrder.getPrice())));
                }
                if (spotOrder.getType() ==  Byte.valueOf("2")) {
                    sellCount++;
                    Integer symSellCount = sellCounts.get(spotOrder.getSymbol());
                    if (symSellCount == null) {
                        symSellCount = 0;
                    }
                    sellCounts.put(spotOrder.getSymbol(), symSellCount + 1);

                    BigDecimal symSellAmount = sellAmounts.get(spotOrder.getSymbol());
                    if (symSellAmount == null) {
                        symSellAmount = BigDecimal.ZERO;
                    }
                    sellAmounts.put(spotOrder.getSymbol(), symSellAmount.add(spotOrder.getSize()));

                    BigDecimal symSellSum = sellSums.get(spotOrder.getSymbol());
                    if (symSellSum == null) {
                        symSellSum = BigDecimal.ZERO;
                    }
                    sellSums.put(spotOrder.getSymbol(), symSellSum.add(spotOrder.getSize().multiply(spotOrder.getPrice())));
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            String message = MessageFormat.format("买入次数:{0},卖出次数:{1}\r\n\r\n", buyCount, sellCount) ;
            stringBuilder.append(message);
            Set<String> allSymbols = new HashSet<>();
            allSymbols.addAll(buyCounts.keySet());
            allSymbols.addAll(sellCounts.keySet());
            for (String symbol : allSymbols) {
                Integer buyCountSymbol = buyCounts.get(symbol);
                if (buyCountSymbol == null) {
                    buyCountSymbol = 0;
                }
                Integer sellCountSymbol = sellCounts.get(symbol);
                if (sellCountSymbol == null) {
                    sellCountSymbol = 0;
                }
                BigDecimal buyAmountSymbol = buyAmounts.get(symbol);
                if (buyAmountSymbol == null) {
                    buyAmountSymbol = BigDecimal.ZERO;
                }
                BigDecimal sellAmountSymbol = sellAmounts.get(symbol);
                if (sellAmountSymbol == null) {
                    sellAmountSymbol = BigDecimal.ZERO;
                }

                BigDecimal buySumSymbol = buySums.get(symbol);
                if (buySumSymbol == null) {
                    buySumSymbol = BigDecimal.ZERO;
                } else {
                    buySumSymbol = buySumSymbol.divide(buyAmountSymbol, 4, BigDecimal.ROUND_DOWN);
                }
                BigDecimal sellSumSymbol = sellSums.get(symbol);
                if (sellSumSymbol == null) {
                    sellSumSymbol = BigDecimal.ZERO;
                } else {
                    sellSumSymbol = sellSumSymbol.divide(sellAmountSymbol, 4, BigDecimal.ROUND_DOWN);
                }
                stringBuilder.append(symbol + ":买入(次数:" + buyCountSymbol + ":数量" + buyAmountSymbol.setScale(2, RoundingMode.DOWN)
                        + ":价格" + buySumSymbol + ")，卖出(次数:" + sellCountSymbol
                        + "，数量" + sellAmountSymbol.setScale(2, RoundingMode.DOWN)
                        + "，价格" + sellSumSymbol
                        + ")\r\n\r\n");

            }
            weiXinMessageService.sendMessage(title,  stringBuilder.toString());

        }
    }
}
