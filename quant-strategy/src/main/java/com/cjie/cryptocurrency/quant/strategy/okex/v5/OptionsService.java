package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountDetail;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.PositionInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.OptionMarketData;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.OrderBook;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.PriceLimitData;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.PlaceOrder;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.FundingAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.marketData.MarketDataAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.publicData.PublicDataAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.trade.TradeAPIService;
import com.cjie.cryptocurrency.quant.mapper.OptionsOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.SpotOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.APIKey;
import com.cjie.cryptocurrency.quant.model.OptionsOrder;
import com.cjie.cryptocurrency.quant.model.SpotOrder;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.MessageService;
import com.google.common.collect.Maps;
import javafx.scene.effect.Light;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Component
@Slf4j
public class OptionsService {

    @Autowired
    private MarketDataAPIService marketDataAPIService;

    @Autowired
    private PublicDataAPIService publicDataAPIService;

    @Autowired
    private AccountAPIV5Service accountAPIV5Service;

    @Autowired
    private TradeAPIService tradeAPIService;

    @Autowired
    private FundingAPIService fundingAPIService;

    @Autowired
    @Qualifier("telegramMessageServiceImpl")
    private MessageService messageService;


    @Autowired
    private SwapOrderMapper swapOrderMapper;

    @Autowired
    private OptionsOrderMapper optionsOrderMapper;

    @Autowired
    private SpotOrderMapper spotOrderMapper;


    @Autowired
    private SpotV5Service spotV5Service;


    private static Map<String, Integer> STATES = Maps.newHashMap();

    private static Map<String, BigDecimal> optionsCtVal = Maps.newHashMap();

    static {
//        canceled：撤单成功
//        live：等待成交
//        partially_filled：部分成交
//        filled：完全成交

        STATES.put("live", 0);
        STATES.put("partially_filled", 1);
        STATES.put("filled", 2);
        STATES.put("canceled", -1);

        optionsCtVal.put("BTC-USD", new BigDecimal("0.01"));
        optionsCtVal.put("ETH-USD", new BigDecimal("0.1"));

    }

    private  String  getOptionExpireTime() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

//        // 计算下下周周五
//        LocalDate nextNextFriday = today
////                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)) // 本周五（如果今天是周五，则返回今天）
////                .plusWeeks(2); // 加两周（下下周）
//                .plusDays(2);
        // 获取两个月后的日期
        LocalDate twoMonthsLater = today.plusDays(45);

        // 获取该月的最后一个星期五
        LocalDate lastFriday = twoMonthsLater.with(TemporalAdjusters.lastInMonth(DayOfWeek.FRIDAY));
        // 格式化为 yyMMdd（如 20250526）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String formattedDate = lastFriday.format(formatter);
        return formattedDate;
    }

    private String getNextNDay(int next) {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        LocalDate nDaysLater = today.plusDays(next);

        // 格式化为 yyMMdd（如 20250526）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String formattedDate = nDaysLater.format(formatter);
        return formattedDate;
    }

    public void swapAndOptionHedging(String site, String instrumentId, String symbol, Double increment, int size) {
        try {

            List<Integer> unProcessedStatuses = new ArrayList<>();
            unProcessedStatuses.add(99);
            unProcessedStatuses.add(0);
            unProcessedStatuses.add(1);
            try {
                List<SwapOrder> swapOrders = swapOrderMapper.selectByStatus(instrumentId, "swapAndOptionHedging", unProcessedStatuses);
                if (CollectionUtils.isNotEmpty(swapOrders)) {
                    log.info("unprocessed orders {}", JSON.toJSONString(swapOrders));
                    for (SwapOrder swapOrder : swapOrders) {
                        JSONObject result = tradeAPIService.getOrderDetails(site, instrumentId, swapOrder.getOrderId(), null);

                        log.info("swap order status {}", JSON.toJSONString(result));
                        if (result == null) {
                            return;
                        }
                        String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                        if (state == null || STATES.get(state) == null) {
                            return;
                        }
                        Integer status = STATES.get(state);
                        if (!swapOrder.getStatus().equals(status)) {
                            swapOrderMapper.updateStatus(swapOrder.getOrderId(), status);
                        }
                    }
                }
            } catch (Exception e) {
                log.info("update status error, instrumentId:{}", instrumentId, e);
                return;
            }

            try {
                List<OptionsOrder> optionsOrders = optionsOrderMapper.selectByStatus(symbol, "swapAndOptionHedging", unProcessedStatuses);
                if (CollectionUtils.isNotEmpty(optionsOrders)) {
                    log.info("unprocessed options orders {}", JSON.toJSONString(optionsOrders));
                    for (OptionsOrder optionsOrder : optionsOrders) {
                        JSONObject result = tradeAPIService.getOrderDetails(site, optionsOrder.getInstrumentId(), optionsOrder.getOrderId(), null);

                        log.info("options order status {}", JSON.toJSONString(result));
                        if (result == null) {
                            return;
                        }
                        String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                        if (state == null || STATES.get(state) == null) {
                            return;
                        }
                        Integer status = STATES.get(state);
                        if (!optionsOrder.getStatus().equals(status)) {
                            optionsOrderMapper.updateStatus(optionsOrder.getOrderId(), status);
                        }
                    }
                }
            } catch (Exception e) {
                log.info("update status error, symbol:{}", symbol, e);
                return;
            }


            HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, instrumentId);

            if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
                return;
            }
            Ticker apiTickerVO = swapTicker.getData().get(0);
            Double currentPrice = Double.valueOf(apiTickerVO.getLast());

            String expireTime = getOptionExpireTime();
            log.info("到期日期{}:{}", instrumentId, expireTime);


            SwapOrder lastOrder = null;
            List<Integer> selledStatuses = new ArrayList<>();
            selledStatuses.add(2);
            List<SwapOrder> selledOrders = swapOrderMapper.selectByStatus(instrumentId, "swapAndOptionHedging", selledStatuses);
            if (CollectionUtils.isNotEmpty(selledOrders)) {
                for (SwapOrder swapOrder : selledOrders) {
                    if (swapOrder.getType() == 2) {
                        if (lastOrder == null) {
                            lastOrder = swapOrder;
                        }
                    }
                }
            }


            OptionsOrder lastOptionsOrder = null;
            List<OptionsOrder> selledOptionsOrders = optionsOrderMapper.selectByStatus(symbol, "swapAndOptionHedging", selledStatuses);
            if (CollectionUtils.isNotEmpty(selledOptionsOrders)) {
                for (OptionsOrder optionsOrder : selledOptionsOrders) {
                    if (lastOptionsOrder == null) {
                        lastOptionsOrder = optionsOrder;
                        break;
                    }
                }
            }


            //获取期权数据
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", expireTime);

            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentOptionMarketData = null;
                Long currentStrikePrice = null;
                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3]) || !"C".equals(optionInstArr[4])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    if (strikePrice < currentPrice) {
                        if (currentOptionMarketData == null || strikePrice > currentStrikePrice) {
                            currentStrikePrice = strikePrice;
                            currentOptionMarketData = optionMarketData;
                        }
                    }
                }
                String optionInstId = currentOptionMarketData.getInstId();
                if (currentOptionMarketData != null) {
                    log.info("期权市场数据{}:{}", currentOptionMarketData.getInstId(), JSON.toJSONString(currentOptionMarketData));
                    //获取期权的价格数据
                    HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, optionInstId, null);
                    log.info("期权深度数据{}:{}", optionInstId, JSON.toJSONString(optionOrderBookDatas));
                    if (!"0".equals(optionOrderBookDatas.getCode()) || optionOrderBookDatas.getData().size() <= 0
                            || optionOrderBookDatas.getData().get(0).getAsks().size() <= 0) {
                        return;
                    }
                    String optionAskPrice = optionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
                    log.info("期权卖一价{}:{}", optionInstId, JSON.toJSONString(optionAskPrice));
                    if (lastOrder == null || lastOptionsOrder == null) {
                        //期权买入 合约开空
                        PlaceOrder ppUpOrder = new PlaceOrder();
                        ppUpOrder.setInstId(optionInstId);
                        ppUpOrder.setTdMode("isolated");
                        ppUpOrder.setPx(new BigDecimal(optionAskPrice).toPlainString());
                        ppUpOrder.setSz(String.valueOf(size));
                        ppUpOrder.setSide("buy");
                        ppUpOrder.setOrdType("fok");
                        ppUpOrder.setType("1");
                        OptionsOrder optionsOrder = new OptionsOrder();
                        optionsOrder.setInstrumentId(optionInstId);
                        optionsOrder.setCreateTime(new Date());
                        optionsOrder.setStrategy("swapAndOptionHedging");
                        optionsOrder.setIsMock(Byte.valueOf("0"));
                        optionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
                        optionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
                        optionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));

                        optionsOrder.setSymbol(symbol);
                        optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                        optionsOrder.setDelta(new BigDecimal(currentOptionMarketData.getDelta()));
                        optionsOrder.setGamma(new BigDecimal(currentOptionMarketData.getGamma()));
                        optionsOrder.setVega(new BigDecimal(currentOptionMarketData.getVega()));
                        optionsOrder.setTheta(new BigDecimal(currentOptionMarketData.getTheta()));
                        optionsOrder.setVolLv(new BigDecimal(currentOptionMarketData.getVolLv()));

                        //下单
                        String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                        log.info("买入看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                        messageService.sendStrategyMessage("swapAndOptionHedging买入看涨期权", "swapAndOptionHedging买入看涨期权-instId:" + optionInstId + ",price:" + optionAskPrice);

                        if (orderId == null) {
                            return;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                        if (optionsOrder == null) {
                            return;
                        }
                        JSONObject result = tradeAPIService.getOrderDetails(site, optionInstId, orderId, null);

                        log.info("options order status {}", JSON.toJSONString(result));
                        if (result == null) {
                            return;
                        }
                        String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                        if (state == null || STATES.get(state) == null) {
                            return;
                        }
                        Integer status = STATES.get(state);
                        if (!optionsOrder.getStatus().equals(status)) {
                            swapOrderMapper.updateStatus(orderId, status);
                        }
                        if (status != 2) {
                            return;
                        }

                        PlaceOrder ppDownOrder = new PlaceOrder();
                        ppDownOrder.setInstId(instrumentId);
                        ppDownOrder.setTdMode("cross");
                        ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                        ppDownOrder.setSz(String.valueOf(size));
                        ppDownOrder.setSide("sell");
                        ppDownOrder.setOrdType("market");
                        ppDownOrder.setPosSide("short");
                        ppDownOrder.setType("2");
                        JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "swapAndOptionHedging");
                        messageService.sendStrategyMessage("swapAndOptionHedging合约开空", "swapAndOptionHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice);
                        log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
                        return;
                    }

                    Double lastPrice = lastOrder.getPrice().doubleValue();
                    if (lastOptionsOrder.getCreateTime().after(lastOrder.getCreateTime())) {
                        lastPrice = lastOptionsOrder.getSwapPrice().doubleValue();
                    }
                    log.info("当前价格{}:{},上次价格:{}", instrumentId, apiTickerVO.getLast(), lastPrice);


                    //价格上涨
                    if (currentPrice > lastPrice && currentPrice - lastPrice > lastPrice * increment * 1.05) {
                      //卖出看涨期权 合约做空
                        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, "OPTION", null, null);
                        log.info("期权持仓{}-看涨总{}, result:{}", symbol, JSON.toJSONString(positionsResult));
                        if (positionsResult == null || !positionsResult.getCode().equals("0")) {
                            return;
                        }
                        PositionInfo upPosition = null;
                        double longPosition = 0;
                        for (PositionInfo apiPositionVO : positionsResult.getData()) {
                            if (apiPositionVO.getAvailPos().equals("")) {
                                continue;
                            }
                            if (apiPositionVO.getPosSide().equals("net") && Double.valueOf(apiPositionVO.getPos()) >= Double.valueOf(size) && Double.valueOf(apiPositionVO.getAvailPos()) >= Double.valueOf(size)) {
                                if (upPosition == null) {
                                    upPosition = apiPositionVO;
                                    longPosition = Double.valueOf(apiPositionVO.getPos());
                                } else if (Long.parseLong(upPosition.getInstId().split("-")[2]) > Long.parseLong(apiPositionVO.getInstId().split("-")[2])) {
                                    upPosition = apiPositionVO;
                                    longPosition = Double.valueOf(apiPositionVO.getPos());
                                } else if (Long.parseLong(upPosition.getInstId().split("-")[2]) == Long.parseLong(apiPositionVO.getInstId().split("-")[2]) && apiPositionVO.getcTime() < upPosition.getcTime()) {
                                    upPosition = apiPositionVO;
                                    longPosition = Double.valueOf(apiPositionVO.getPos());
                                }
                            }

                        }
                        log.info("期权持仓{}-看涨{}, result:{}", instrumentId, longPosition, JSON.toJSONString(upPosition));
                        if (upPosition != null && longPosition >= size) {

                            optionOrderBookDatas = marketDataAPIService.getOrderBook(site, optionInstId, null);
                            log.info("期权深度数据{}:{}", optionInstId, JSON.toJSONString(optionOrderBookDatas));
                            if (!"0".equals(optionOrderBookDatas.getCode()) || optionOrderBookDatas.getData().size() <= 0
                                    || optionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                                return;
                            }
                            String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                            log.info("期权买一价{}:{}", optionInstId, JSON.toJSONString(optionBidPrice));

                            PlaceOrder ppUpOrder = new PlaceOrder();
                            ppUpOrder.setInstId(upPosition.getInstId());
                            ppUpOrder.setTdMode("isolated");
                            ppUpOrder.setPx(new BigDecimal(optionBidPrice).toPlainString());
                            ppUpOrder.setSz(String.valueOf(size));
                            ppUpOrder.setSide("sell");
                            ppUpOrder.setOrdType("fok");
//                ppUpOrder.setPosSide("long");
                            ppUpOrder.setType("3");
                            OptionsOrder optionsOrder = new OptionsOrder();
                            optionsOrder.setInstrumentId(upPosition.getInstId());
                            optionsOrder.setCreateTime(new Date());
                            optionsOrder.setStrategy("swapAndOptionHedging");
                            optionsOrder.setIsMock(Byte.valueOf("0"));
                            optionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
                            optionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
                            optionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));

                            optionsOrder.setSymbol(symbol);
                            optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                            optionsOrder.setDelta(new BigDecimal(currentOptionMarketData.getDelta()));
                            optionsOrder.setGamma(new BigDecimal(currentOptionMarketData.getGamma()));
                            optionsOrder.setVega(new BigDecimal(currentOptionMarketData.getVega()));
                            optionsOrder.setTheta(new BigDecimal(currentOptionMarketData.getTheta()));
                            optionsOrder.setVolLv(new BigDecimal(currentOptionMarketData.getVolLv()));

                            //下单
                            String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                            log.info("卖出看涨期权 {}-{},orderId:{}", upPosition.getInstId(), JSON.toJSONString(ppUpOrder), orderId);
                            messageService.sendStrategyMessage("swapAndOptionHedging卖出看涨期权", "swapAndOptionHedging卖出看涨期权-instId:" + upPosition.getInstId() + ",price:" + optionBidPrice);

                            if (orderId == null) {
                                return;
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                            if (optionsOrder == null) {
                                return;
                            }
                            JSONObject result = tradeAPIService.getOrderDetails(site, upPosition.getInstId(), orderId, null);

                            log.info("options order status {}", JSON.toJSONString(result));
                            if (result == null) {
                                return;
                            }
                            String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                            if (state == null || STATES.get(state) == null) {
                                return;
                            }
                            Integer status = STATES.get(state);
                            if (!optionsOrder.getStatus().equals(status)) {
                                optionsOrderMapper.updateStatus(orderId, status);
                            }
                            if (status != 2) {
                                return;
                            }

                        }

                        PlaceOrder ppDownOrder = new PlaceOrder();
                        ppDownOrder.setInstId(instrumentId);
                        ppDownOrder.setTdMode("cross");
                        ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                        ppDownOrder.setSz(String.valueOf(size));
                        ppDownOrder.setSide("sell");
                        ppDownOrder.setOrdType("market");
                        ppDownOrder.setPosSide("short");
                        ppDownOrder.setType("2");
                        JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "swapAndOptionHedging");

                        log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
                        messageService.sendStrategyMessage("swapAndOptionHedging合约开空", "swapAndOptionHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice);

                        return;

                    }

                    if (currentPrice < lastPrice && lastPrice - currentPrice > lastPrice * increment) {
                        //买入期权 合约平空
                        PlaceOrder ppUpOrder = new PlaceOrder();
                        ppUpOrder.setInstId(optionInstId);
                        ppUpOrder.setTdMode("isolated");
                        ppUpOrder.setPx(new BigDecimal(optionAskPrice).toPlainString());
                        ppUpOrder.setSz(String.valueOf(size));
                        ppUpOrder.setSide("buy");
                        ppUpOrder.setOrdType("fok");
//                ppUpOrder.setPosSide("long");
                        ppUpOrder.setType("1");
                        OptionsOrder optionsOrder = new OptionsOrder();
                        optionsOrder.setInstrumentId(optionInstId);
                        optionsOrder.setCreateTime(new Date());
                        optionsOrder.setStrategy("swapAndOptionHedging");
                        optionsOrder.setIsMock(Byte.valueOf("0"));
                        optionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
                        optionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
                        optionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));

                        optionsOrder.setSymbol(symbol);
                        optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                        optionsOrder.setDelta(new BigDecimal(currentOptionMarketData.getDelta()));
                        optionsOrder.setGamma(new BigDecimal(currentOptionMarketData.getGamma()));
                        optionsOrder.setVega(new BigDecimal(currentOptionMarketData.getVega()));
                        optionsOrder.setTheta(new BigDecimal(currentOptionMarketData.getTheta()));
                        optionsOrder.setVolLv(new BigDecimal(currentOptionMarketData.getVolLv()));

                        //下单
                        String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                        log.info("买入看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                        messageService.sendStrategyMessage("swapAndOptionHedging买入看涨期权", "swapAndOptionHedging买入看张-instId:" + optionInstId + ",price:" + optionAskPrice);

                        if (orderId == null) {
                            return;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                        if (optionsOrder == null) {
                            return;
                        }
                        JSONObject result = tradeAPIService.getOrderDetails(site, optionInstId, orderId, null);

                        log.info("options order status {}", JSON.toJSONString(result));
                        if (result == null) {
                            return;
                        }
                        String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                        if (state == null || STATES.get(state) == null) {
                            return;
                        }
                        Integer status = STATES.get(state);
                        if (!optionsOrder.getStatus().equals(status)) {
                            optionsOrderMapper.updateStatus(orderId, status);
                        }
                        if (status != 2) {
                            return;
                        }

                        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, null, instrumentId, null);
                        if (positionsResult == null || !positionsResult.getCode().equals("0")) {
                            return;
                        }
                        PositionInfo downPosition = null;
                        double shortPosition = 0;
                        for (PositionInfo apiPositionVO : positionsResult.getData()) {
                            if (apiPositionVO.getAvailPos().equals("")) {
                                continue;
                            }
                            if (apiPositionVO.getPosSide().equals("short") && Double.valueOf(apiPositionVO.getPos()) >= Double.valueOf(size) && Double.valueOf(apiPositionVO.getAvailPos()) >= Double.valueOf(size)) {
                                downPosition = apiPositionVO;
                                shortPosition = Double.valueOf(apiPositionVO.getPos());
                            }

                        }
                        log.info("持仓{}-空{}", instrumentId, shortPosition);
                        if (downPosition != null && shortPosition >= size) {
                            PlaceOrder placeOrderParam = new PlaceOrder();
                            placeOrderParam.setInstId(instrumentId);
                            placeOrderParam.setTdMode("cross");
                            placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                            placeOrderParam.setSz(String.valueOf(size));
                            placeOrderParam.setSide("buy");
                            placeOrderParam.setOrdType("market");
                            placeOrderParam.setPosSide("short");
                            placeOrderParam.setType("4");
//                placeOrderParam.setTgtCcy("base_ccy");
//                orders.add(placeOrderParam);
                            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "swapAndOptionHedging");

                            log.info("平空{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
                            messageService.sendStrategyMessage("swapAndOptionHedging合约平空", "swapAndOptionHedging合约平空-instId:" + instrumentId + ",price:" + currentPrice);

                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("swapAndOptionHedging error:{}", e.getMessage(), e);
        }

    }


    void  netGrid(String site, String instrumentId, String symbol, int size, double callIncrement, double putDecrement) {

        HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, symbol + "-USDT");
        if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
            messageService.sendStrategyMessage("netGrid获取不到价格", "netGrid获取不到价格,请手动检查");
            return;
        }
        Ticker apiTickerVO = swapTicker.getData().get(0);
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());

        log.info("当前价格{}-{}-{}", site, currentPrice, symbol);

        //查看要行权的期权
        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, "OPTION", null, null);
        log.info("期权持仓{}-总持仓{}, result:{}", symbol, JSON.toJSONString(positionsResult));
        if (positionsResult != null && positionsResult.getCode().equals("0")) {
            String today = getNextNDay(0);
            for (PositionInfo apiPositionVO : positionsResult.getData()) {
                if (apiPositionVO.getAvailPos().equals("")) {
                    continue;
                }
                if (apiPositionVO.getPosSide().equals("net")) {
                    String optionInstId = apiPositionVO.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    long strikeDate = Long.parseLong(optionInstArr[2]);
                    if (!symbol.equals(optionInstArr[0])) {
                        continue;
                    }
                    if (strikeDate != Long.parseLong(today)) {
                        continue;
                    }

                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    //卖出看跌期权到期时低于行权价，买入相应标的
                    if ("P".equals(optionInstArr[4]) && strikePrice > currentPrice) {

                        List<Integer> filledStatuses = new ArrayList<>();
                        filledStatuses.add(2);
                        List<SpotOrder> spotOrders = spotOrderMapper.selectByStatus(symbol + "-USDT","optionNetGrid", filledStatuses);
                        boolean exists = false;
                        if (spotOrders != null && spotOrders.size() > 0)  {
                            for (SpotOrder spotOrder : spotOrders) {
                                if (spotOrder.getCreateTime().before(new Date(System.currentTimeMillis() - 3600L * 3000))) {
                                    continue;
                                }
                                if (!optionInstId.equals(spotOrder.getReferSymbol())) {
                                    continue;
                                }
                                exists = true;
                                break;
                            }

                        }
                        if (exists) {
                            continue;
                        }
                        log.info("当前价格低于看跌期权行权价{}-当前价{}, 行权价:{}", optionInstId, currentPrice, optionInstArr[4]);
                        BigDecimal buySize = new BigDecimal((strikePrice-currentPrice) * Double.valueOf(apiPositionVO.getPos())/currentPrice).multiply(optionsCtVal.get(symbol+"-USD")).abs().setScale(5, RoundingMode.CEILING);
                        PlaceOrder placeOrderParam = new PlaceOrder();
                        placeOrderParam.setInstId(symbol + "-USDT");
                        placeOrderParam.setTdMode("cross");
                        placeOrderParam.setPx(apiTickerVO.getLast());
                        placeOrderParam.setSz(buySize.toPlainString());
                        placeOrderParam.setSide("buy");
                        placeOrderParam.setTgtCcy("base_ccy");
                        placeOrderParam.setOrdType("market");
                        JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
                        log.info("买入{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));
                        messageService.sendStrategyMessage("optionNetGrid买入现货", "optionNetGrid买入现货-symbol:" + symbol + ",size:" + buySize + ",price:" + currentPrice);
                        if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                            SpotOrder spotOrder = new SpotOrder();
                            spotOrder.setSymbol(symbol + "-USDT");
                            spotOrder.setCreateTime(new Date());
                            spotOrder.setStrategy("optionNetGrid");
                            spotOrder.setIsMock(Byte.valueOf("0"));
                            spotOrder.setType(Byte.valueOf("1"));
                            spotOrder.setPrice(new BigDecimal(apiTickerVO.getLast()));
                            spotOrder.setSize(buySize);
                            spotOrder.setOrderId(String.valueOf(((JSONObject) orderResult.getJSONArray("data").get(0)).getString("ordId")));
                            spotOrder.setStatus(2);
                            spotOrder.setReferSymbol(optionInstId);
                            spotOrderMapper.insert(spotOrder);
                        }
                    }

                }

            }
        }
        try {
            Thread.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= 4; i++) {
            String expireTime = getNextNDay(i);
            double callStrikePrice = currentPrice * (1 + i * callIncrement);
            double putStrikePrice =  currentPrice * (1 - i * putDecrement);
            log.info("到期日期{}:{}, 预估看涨行权价:{},预估看跌行权价:{}", instrumentId, expireTime, callStrikePrice, putStrikePrice);
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", expireTime);

            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentCallOptionMarketData = null;
                Long currentCallStrikePrice = null;
                OptionMarketData currentPutOptionMarketData = null;
                Long currentPutStrikePrice = null;
                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    if ("C".equals(optionInstArr[4]) && strikePrice > callStrikePrice) {
                        if (currentCallOptionMarketData == null || strikePrice < currentCallStrikePrice) {
                            currentCallStrikePrice = strikePrice;
                            currentCallOptionMarketData = optionMarketData;
                        }
                    }
                    if ("P".equals(optionInstArr[4]) && strikePrice < putStrikePrice) {
                        if (currentPutOptionMarketData == null || strikePrice > currentPutStrikePrice) {
                            currentPutStrikePrice = strikePrice;
                            currentPutOptionMarketData = optionMarketData;
                        }
                    }
                }
                log.info("到期日期{}:{},看涨：{}", instrumentId, expireTime, JSON.toJSONString(currentCallOptionMarketData));
                log.info("到期日期{}:{},看跌：{}", instrumentId, expireTime, JSON.toJSONString(currentPutOptionMarketData));
                if (currentCallOptionMarketData != null) {
                    //卖出看涨期权
                    String optionInstId = currentCallOptionMarketData.getInstId();

                    List<Integer> filledStatuses = new ArrayList<>();
                    filledStatuses.add(2);
                    List<OptionsOrder> optionsOrders = optionsOrderMapper.selectByStatus(symbol,"optionNetGrid", filledStatuses);
                    boolean exists = false;
                    if (optionsOrders != null && optionsOrders.size() > 0) {
                        for (OptionsOrder optionsOrder : optionsOrders) {
                            if (optionInstId.split("-")[0].equals(optionsOrder.getInstrumentId().split("-")[0])
                                    && optionInstId.split("-")[2].equals(optionsOrder.getInstrumentId().split("-")[2])
                                    && optionInstId.split("-")[4].equals(optionsOrder.getInstrumentId().split("-")[4])
                                    && optionsOrder.getCreateTime().after(new Date(System.currentTimeMillis() - 3600L * 3000))) {
                                exists = true;
                                break;
                            }
                        }

                    }
                    if (!exists) {
                        HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, optionInstId, null);
                        log.info("期权深度数据{}:{}", optionInstId, JSON.toJSONString(optionOrderBookDatas));
                        if ("0".equals(optionOrderBookDatas.getCode()) && optionOrderBookDatas.getData().size() > 0
                                && optionOrderBookDatas.getData().get(0).getBids().size() > 0) {
                            String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                            log.info("期权买一价{}:{}", optionInstId, JSON.toJSONString(optionBidPrice));

                            if (Double.parseDouble(optionBidPrice) > 0.0006) {

                                PlaceOrder ppUpOrder = new PlaceOrder();
                                ppUpOrder.setInstId(optionInstId);
                                ppUpOrder.setTdMode("cross");
                                ppUpOrder.setPx(new BigDecimal(optionBidPrice).toPlainString());
                                ppUpOrder.setSz(String.valueOf(size));
                                ppUpOrder.setSide("sell");
                                ppUpOrder.setOrdType("fok");
//                ppUpOrder.setPosSide("long");
                                ppUpOrder.setType("3");
                                OptionsOrder optionsOrder = new OptionsOrder();
                                optionsOrder.setInstrumentId(optionInstId);
                                optionsOrder.setCreateTime(new Date());
                                optionsOrder.setStrategy("optionNetGrid");
                                optionsOrder.setIsMock(Byte.valueOf("0"));
                                optionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
                                optionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
                                optionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));

                                optionsOrder.setSymbol(symbol);
                                optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                                optionsOrder.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                                optionsOrder.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                                optionsOrder.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                                optionsOrder.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                                optionsOrder.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));

                                //下单
                                String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                                log.info("卖出看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                                messageService.sendStrategyMessage("optionNetGrid卖出看涨期权", "optionNetGrid卖出看涨期权-instId:" + optionInstId + ",price:" + optionBidPrice);
                                if (orderId != null) {
                                    optionsOrderMapper.updateStatus(orderId, 2);
                                }
                            }
                        }
                    }


                }
                if (currentPutOptionMarketData != null) {
                    //卖出看跌期权
                    String optionInstId = currentPutOptionMarketData.getInstId();

                    List<Integer> filledStatuses = new ArrayList<>();
                    filledStatuses.add(2);
                    List<OptionsOrder> optionsOrders = optionsOrderMapper.selectByStatus(symbol,"optionNetGrid", filledStatuses);
                    boolean exists = false;
                    if (optionsOrders != null && optionsOrders.size() > 0)  {
                        for (OptionsOrder optionsOrder : optionsOrders) {
                            if (optionInstId.split("-")[0].equals(optionsOrder.getInstrumentId().split("-")[0])
                                    && optionInstId.split("-")[2].equals(optionsOrder.getInstrumentId().split("-")[2])
                                    && optionInstId.split("-")[4].equals(optionsOrder.getInstrumentId().split("-")[4])
                                    && optionsOrder.getCreateTime().after(new Date(System.currentTimeMillis() - 3600L * 3000))) {
                                exists = true;
                                break;
                            }
                        }

                    }
                    if (!exists) {
                        HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, optionInstId, null);
                        log.info("期权深度数据{}:{}", optionInstId, JSON.toJSONString(optionOrderBookDatas));
                        if ("0".equals(optionOrderBookDatas.getCode()) && optionOrderBookDatas.getData().size() > 0
                                && optionOrderBookDatas.getData().get(0).getBids().size() > 0) {
                            String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                            log.info("期权买一价{}:{}", optionInstId, JSON.toJSONString(optionBidPrice));
                            if (Double.parseDouble(optionBidPrice) > 0.0006) {
                                PlaceOrder ppUpOrder = new PlaceOrder();
                                ppUpOrder.setInstId(optionInstId);
                                ppUpOrder.setTdMode("cross");
                                ppUpOrder.setPx(new BigDecimal(optionBidPrice).toPlainString());
                                ppUpOrder.setSz(String.valueOf(size));
                                ppUpOrder.setSide("sell");
                                ppUpOrder.setOrdType("fok");
                                ppUpOrder.setType("4");
                                OptionsOrder optionsOrder = new OptionsOrder();
                                optionsOrder.setInstrumentId(optionInstId);
                                optionsOrder.setCreateTime(new Date());
                                optionsOrder.setStrategy("optionNetGrid");
                                optionsOrder.setIsMock(Byte.valueOf("0"));
                                optionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
                                optionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
                                optionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));

                                optionsOrder.setSymbol(symbol);
                                optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                                optionsOrder.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                                optionsOrder.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                                optionsOrder.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                                optionsOrder.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                                optionsOrder.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));

                                //下单
                                String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                                log.info("卖出看跌期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                                messageService.sendStrategyMessage("optionNetGrid卖出看跌期权", "optionNetGrid卖出看跌期权-instId:" + optionInstId + ",price:" + optionBidPrice);
                                if (orderId != null) {
                                    optionsOrderMapper.updateStatus(orderId, 2);
                                }
                            }
                        }
                    }


                }
            }
        }
    }

}
