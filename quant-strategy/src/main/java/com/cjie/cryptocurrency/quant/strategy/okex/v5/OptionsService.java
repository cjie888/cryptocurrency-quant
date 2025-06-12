package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.PositionInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.OptionMarketData;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.OrderBook;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.PlaceOrder;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.FundingAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.marketData.MarketDataAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.publicData.PublicDataAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.trade.TradeAPIService;
import com.cjie.cryptocurrency.quant.mapper.OptionsOrderLogMapper;
import com.cjie.cryptocurrency.quant.mapper.OptionsOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.SpotOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.OptionsOrder;
import com.cjie.cryptocurrency.quant.model.OptionsOrderLog;
import com.cjie.cryptocurrency.quant.model.SpotOrder;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.MessageService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private OptionsOrderLogMapper optionsOrderLogMapper;

    @Autowired
    private SpotOrderMapper spotOrderMapper;


    @Autowired
    private SpotV5Service spotV5Service;


    private static Map<String, Integer> STATES = Maps.newHashMap();

    private static Map<String, BigDecimal> optionsCtVal = Maps.newHashMap();

    private static Map<String, BigDecimal> optionsSwapCtVal = Maps.newHashMap();


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

        optionsSwapCtVal.put("BTC-USD", new BigDecimal("10"));
        optionsSwapCtVal.put("ETH-USD", new BigDecimal("1"));
    }

    private String getOptionExpireTime(int days) {
        // 获取当前日期
        LocalDate today = LocalDate.now();

//        // 计算下下周周五
//        LocalDate nextNextFriday = today
////                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)) // 本周五（如果今天是周五，则返回今天）
////                .plusWeeks(2); // 加两周（下下周）
//                .plusDays(2);
        // 获取两个月后的日期
        LocalDate twoMonthsLater = today.plusDays(days);

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

            String expireTime = getOptionExpireTime(45);
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
                    if (strikePrice > currentPrice) {
                        if (currentOptionMarketData == null || strikePrice < currentStrikePrice) {
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
                            optionsOrderMapper.updateStatus(orderId, status);
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
                        messageService.sendStrategyMessage("swapAndOptionHedging合约开空", "swapAndOptionHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + ppDownOrder.getSz());
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

                            optionOrderBookDatas = marketDataAPIService.getOrderBook(site, upPosition.getInstId(), null);
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
                            messageService.sendStrategyMessage("swapAndOptionHedging卖出看涨期权", "swapAndOptionHedging卖出看涨期权-instId:" + upPosition.getInstId() + ",price:" + optionBidPrice  + ",size:" + optionsOrder.getSize());

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
                        messageService.sendStrategyMessage("swapAndOptionHedging合约开空", "swapAndOptionHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice  + ",size:" + ppDownOrder.getSz());

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
                        messageService.sendStrategyMessage("swapAndOptionHedging买入看涨期权", "swapAndOptionHedging买入看张-instId:" + optionInstId + ",price:" + optionAskPrice  + ",size:" + optionsOrder.getSize());

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
                            messageService.sendStrategyMessage("swapAndOptionHedging合约平空", "swapAndOptionHedging合约平空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + placeOrderParam.getSz());

                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("swapAndOptionHedging error:{}", e.getMessage(), e);
        }

    }


    void netGrid1(String site, String instrumentId, String symbol, int size, double callIncrement, double putDecrement) {

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
                    if (Double.valueOf(apiPositionVO.getPos()) < 0) {
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
                            List<SpotOrder> spotOrders = spotOrderMapper.selectByStatus(symbol + "-USDT", "optionNetGrid", filledStatuses);
                            boolean exists = false;
                            if (spotOrders != null && spotOrders.size() > 0) {
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
                            BigDecimal buySize = new BigDecimal((strikePrice - currentPrice) * Double.valueOf(apiPositionVO.getPos()) / currentPrice).multiply(optionsCtVal.get(symbol + "-USD")).abs().setScale(5, RoundingMode.CEILING);
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
                            messageService.sendStrategyMessage("optionNetGrid买入现货", "optionNetGrid买入现货-symbol:" + symbol + ",size:" + buySize + ",price:" + currentPrice + ",instId:" + optionInstId);
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
//                    if (Double.valueOf(apiPositionVO.getPos()) > 0) {
//                        String optionInstId = apiPositionVO.getInstId();
//                        String[] optionInstArr = optionInstId.split("-");
//                        long strikeDate = Long.parseLong(optionInstArr[2]);
//                        if (!symbol.equals(optionInstArr[0])) {
//                            continue;
//                        }
//                        if (strikeDate != Long.parseLong(today)) {
//                            continue;
//                        }
//
//                        Long strikePrice = Long.parseLong(optionInstArr[3]);
//                        //买入看跌期权到期时低于行权价，卖出相应标的
//                        if ("P".equals(optionInstArr[4]) && strikePrice > currentPrice) {
//                            log.info("当前价格低于看跌期权行权价{}-当前价{}, 行权价:{}", optionInstId, currentPrice, optionInstArr[4]);
//                            HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, optionInstId, null);
//                            log.info("期权深度数据{}:{}", optionInstId, JSON.toJSONString(optionOrderBookDatas));
//                            if ("0".equals(optionOrderBookDatas.getCode()) && optionOrderBookDatas.getData().size() > 0
//                                    && optionOrderBookDatas.getData().get(0).getBids().size() > 0) {
//                                String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
//                                log.info("期权买一价{}:{}", optionInstId, JSON.toJSONString(optionBidPrice));
//
//                            PlaceOrder ppUpOrder = new PlaceOrder();
//                            ppUpOrder.setInstId(optionInstId);
//                            ppUpOrder.setTdMode("cross");
//                            ppUpOrder.setPx(new BigDecimal(optionBidPrice).toPlainString());
//                            ppUpOrder.setSz(String.valueOf(size));
//                            ppUpOrder.setSide("sell");
//                            ppUpOrder.setOrdType("fok");
//                            ppUpOrder.setType("4");
//                            OptionsOrder optionsOrder = new OptionsOrder();
//                            optionsOrder.setInstrumentId(optionInstId);
//                            optionsOrder.setCreateTime(new Date());
//                            optionsOrder.setStrategy("optionNetGrid");
//                            optionsOrder.setIsMock(Byte.valueOf("0"));
//                            optionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
//                            optionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
//                            optionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));
//
//                            optionsOrder.setSymbol(symbol);
//                            optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
//                            optionsOrder.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
//                            optionsOrder.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
//                            optionsOrder.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
//                            optionsOrder.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
//                            optionsOrder.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
//
//                            //下单
//                            String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
//                            log.info("卖出看跌期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
//                            messageService.sendStrategyMessage("optionNetGrid卖出看跌期权", "optionNetGrid卖出看跌期权-instId:" + optionInstId + ",price:" + optionBidPrice);
//                            if (orderId != null) {
//                                optionsOrderMapper.updateStatus(orderId, 2);
//                        }
//                    }
                    //卖出看涨期权到期时高于行权价，卖出相应标的
//                    if ("C".equals(optionInstArr[4]) && strikePrice < currentPrice) {
//
//                        List<Integer> filledStatuses = new ArrayList<>();
//                        filledStatuses.add(2);
//                        List<SpotOrder> spotOrders = spotOrderMapper.selectByStatus(symbol + "-USDT","optionNetGrid", filledStatuses);
//                        boolean exists = false;
//                        if (spotOrders != null && spotOrders.size() > 0)  {
//                            for (SpotOrder spotOrder : spotOrders) {
//                                if (spotOrder.getCreateTime().before(new Date(System.currentTimeMillis() - 3600L * 3000))) {
//                                    continue;
//                                }
//                                if (!optionInstId.equals(spotOrder.getReferSymbol())) {
//                                    continue;
//                                }
//                                exists = true;
//                                break;
//                            }
//
//                        }
//                        if (exists) {
//                            continue;
//                        }
//                        log.info("当前价格高于看涨期权行权价{}-当前价{}, 行权价:{}", optionInstId, currentPrice, optionInstArr[4]);
//                        BigDecimal sellSize = new BigDecimal((strikePrice-currentPrice) * Double.valueOf(apiPositionVO.getPos())/currentPrice).multiply(optionsCtVal.get(symbol+"-USD")).abs().setScale(5, RoundingMode.CEILING);
//                        PlaceOrder placeOrderParam = new PlaceOrder();
//                        placeOrderParam.setInstId(symbol + "-USDT");
//                        placeOrderParam.setTdMode("cross");
//                        placeOrderParam.setPx(apiTickerVO.getLast());
//                        placeOrderParam.setSz(sellSize.toPlainString());
//                        placeOrderParam.setSide("sell");
//                        placeOrderParam.setTgtCcy("base_ccy");
//                        placeOrderParam.setOrdType("market");
//                        JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
//                        log.info("卖出{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));
//                        messageService.sendStrategyMessage("optionNetGrid卖出现货", "optionNetGrid卖出现货-symbol:" + symbol + ",size:" + sellSize + ",price:" + currentPrice  + ",instId:" + optionInstId);
//                        if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {
//
//                            SpotOrder spotOrder = new SpotOrder();
//                            spotOrder.setSymbol(symbol + "-USDT");
//                            spotOrder.setCreateTime(new Date());
//                            spotOrder.setStrategy("optionNetGrid");
//                            spotOrder.setIsMock(Byte.valueOf("0"));
//                            spotOrder.setType(Byte.valueOf("2"));
//                            spotOrder.setPrice(new BigDecimal(apiTickerVO.getLast()));
//                            spotOrder.setSize(sellSize);
//                            spotOrder.setOrderId(String.valueOf(((JSONObject) orderResult.getJSONArray("data").get(0)).getString("ordId")));
//                            spotOrder.setStatus(2);
//                            spotOrder.setReferSymbol(optionInstId);
//                            spotOrderMapper.insert(spotOrder);
//                        }
//                    }
                }
            }
        }
    }

    void netGrid2(String site, String instrumentId, String symbol, int size, double callIncrement, double putDecrement) {

        HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, symbol + "-USDT");
        if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
            messageService.sendStrategyMessage("netGrid获取不到价格", "netGrid获取不到价格,请手动检查");
            return;
        }
        Ticker apiTickerVO = swapTicker.getData().get(0);
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());

        log.info("当前价格{}-{}-{}", site, currentPrice, symbol);
        for (int i = 1; i <= 4; i++) {
            String expireTime = getNextNDay(i - 1);
            for (int j = i; j <= 4; j++) {
                double callStrikePrice = currentPrice * (1 + 0.008 + j * callIncrement);
                double putStrikePrice = currentPrice * (1 - 0.008 - j * putDecrement);
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
                        List<OptionsOrder> optionsOrders = optionsOrderMapper.selectByStatus(symbol, "optionNetGrid", filledStatuses);
                        boolean exists = false;
                        if (optionsOrders != null && optionsOrders.size() > 0) {
                            for (OptionsOrder optionsOrder : optionsOrders) {
                                if (optionInstId.equals(optionsOrder.getInstrumentId())
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
                                        //再买入一张看涨期权，构成铁鹰
//                                        OptionMarketData currentBuyCallOptionMarketData = null;
//                                        Long currentBuyCallStrikePrice = null;
//                                        for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
//                                            String buyCallOptionInstId = optionMarketData.getInstId();
//                                            String[] optionInstArr = buyCallOptionInstId.split("-");
//                                            if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
//                                                continue;
//                                            }
//                                            Long strikePrice = Long.parseLong(optionInstArr[3]);
//                                            if ("C".equals(optionInstArr[4]) && strikePrice > currentCallStrikePrice) {
//                                                if (currentBuyCallOptionMarketData == null || strikePrice < currentBuyCallStrikePrice) {
//                                                    currentBuyCallStrikePrice = strikePrice;
//                                                    currentBuyCallOptionMarketData = optionMarketData;
//                                                }
//                                            }
//                                        }
//
//                                        if (currentBuyCallOptionMarketData != null) {
//                                            //买入看涨期权
//                                            String buyCallOptionInstId = currentBuyCallOptionMarketData.getInstId();
//
//                                            HttpResult<List<OrderBook>> buyCallOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, buyCallOptionInstId, null);
//                                            log.info("期权深度数据{}:{}", buyCallOptionInstId, JSON.toJSONString(buyCallOptionOrderBookDatas));
//                                            if ("0".equals(buyCallOptionOrderBookDatas.getCode()) && buyCallOptionOrderBookDatas.getData().size() > 0
//                                                    && buyCallOptionOrderBookDatas.getData().get(0).getBids().size() > 0) {
//                                                String buyCallOptionBidPrice = optionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
//                                                log.info("期权卖一价{}:{}", buyCallOptionInstId, JSON.toJSONString(buyCallOptionBidPrice));
//
//                                                PlaceOrder ppBuyUpOrder = new PlaceOrder();
//                                                ppBuyUpOrder.setInstId(buyCallOptionInstId);
//                                                ppBuyUpOrder.setTdMode("isolated");
//                                                ppBuyUpOrder.setPx(new BigDecimal(buyCallOptionBidPrice).toPlainString());
//                                                ppBuyUpOrder.setSz(String.valueOf(size));
//                                                ppBuyUpOrder.setSide("buy");
//                                                ppBuyUpOrder.setOrdType("fok");
////                ppUpOrder.setPosSide("long");
//                                                ppBuyUpOrder.setType("1");
//                                                OptionsOrder bulCallOptionsOrder = new OptionsOrder();
//                                                bulCallOptionsOrder.setInstrumentId(buyCallOptionInstId);
//                                                bulCallOptionsOrder.setCreateTime(new Date());
//                                                bulCallOptionsOrder.setStrategy("optionNetGrid");
//                                                bulCallOptionsOrder.setIsMock(Byte.valueOf("0"));
//                                                bulCallOptionsOrder.setType(Byte.valueOf(ppBuyUpOrder.getType()));
//                                                bulCallOptionsOrder.setPrice(new BigDecimal(ppBuyUpOrder.getPx()));
//                                                bulCallOptionsOrder.setSize(new BigDecimal(ppBuyUpOrder.getSz()));
//
//                                                bulCallOptionsOrder.setSymbol(symbol);
//                                                bulCallOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
//                                                bulCallOptionsOrder.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
//                                                bulCallOptionsOrder.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
//                                                bulCallOptionsOrder.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
//                                                bulCallOptionsOrder.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
//                                                bulCallOptionsOrder.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
//
//                                                //下单
//                                                String buyCallOrderId = tradeAPIService.placeOptionsOrder(site, ppBuyUpOrder, bulCallOptionsOrder);
//                                                log.info("买入看涨期权 {}-{},orderId:{}", buyCallOptionInstId, JSON.toJSONString(ppBuyUpOrder), buyCallOrderId);
//                                                messageService.sendStrategyMessage("optionNetGrid买入看涨期权", "optionNetGrid买入看涨期权-instId:" + buyCallOptionInstId + ",price:" + buyCallOptionBidPrice);
//
//                                                if (buyCallOrderId != null) {
//                                                    optionsOrderMapper.updateStatus(buyCallOrderId, 2);
//                                                }
//                                            }
//                                        }
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
                        List<OptionsOrder> optionsOrders = optionsOrderMapper.selectByStatus(symbol, "optionNetGrid", filledStatuses);
                        boolean exists = false;
                        if (optionsOrders != null && optionsOrders.size() > 0) {
                            for (OptionsOrder optionsOrder : optionsOrders) {
                                if (optionInstId.equals(optionsOrder.getInstrumentId())
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
                                    optionsOrder.setDelta(new BigDecimal(currentPutOptionMarketData.getDelta()));
                                    optionsOrder.setGamma(new BigDecimal(currentPutOptionMarketData.getGamma()));
                                    optionsOrder.setVega(new BigDecimal(currentPutOptionMarketData.getVega()));
                                    optionsOrder.setTheta(new BigDecimal(currentPutOptionMarketData.getTheta()));
                                    optionsOrder.setVolLv(new BigDecimal(currentPutOptionMarketData.getVolLv()));

                                    //下单
                                    String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                                    log.info("卖出看跌期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                                    messageService.sendStrategyMessage("optionNetGrid卖出看跌期权", "optionNetGrid卖出看跌期权-instId:" + optionInstId + ",price:" + optionBidPrice);
                                    if (orderId != null) {
                                        optionsOrderMapper.updateStatus(orderId, 2);

//                                        //再买入一张看跌期权，构成铁鹰
//                                        OptionMarketData currentBuyPutOptionMarketData = null;
//                                        Long currentBuyPutStrikePrice = null;
//                                        for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
//                                            String buyPutOptionInstId = optionMarketData.getInstId();
//                                            String[] optionInstArr = buyPutOptionInstId.split("-");
//                                            if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
//                                                continue;
//                                            }
//                                            Long strikePrice = Long.parseLong(optionInstArr[3]);
//                                            if ("P".equals(optionInstArr[4]) && strikePrice < currentPutStrikePrice) {
//                                                if (currentBuyPutOptionMarketData == null || strikePrice > currentBuyPutStrikePrice) {
//                                                    currentBuyPutStrikePrice = strikePrice;
//                                                    currentBuyPutOptionMarketData = optionMarketData;
//                                                }
//                                            }
//                                        }
//
//                                        if (currentBuyPutOptionMarketData != null) {
//                                            //买入看跌期权
//                                            String buyPutOptionInstId = currentBuyPutOptionMarketData.getInstId();
//
//                                            HttpResult<List<OrderBook>> buyPutOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, buyPutOptionInstId, null);
//                                            log.info("期权深度数据{}:{}", buyPutOptionInstId, JSON.toJSONString(buyPutOptionOrderBookDatas));
//                                            if ("0".equals(buyPutOptionOrderBookDatas.getCode()) && buyPutOptionOrderBookDatas.getData().size() > 0
//                                                    && buyPutOptionOrderBookDatas.getData().get(0).getBids().size() > 0) {
//                                                String buyPutOptionBidPrice = optionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
//                                                log.info("期权卖一价{}:{}", buyPutOptionInstId, JSON.toJSONString(buyPutOptionBidPrice));
//
//                                                PlaceOrder ppBuyDownOrder = new PlaceOrder();
//                                                ppBuyDownOrder.setInstId(buyPutOptionInstId);
//                                                ppBuyDownOrder.setTdMode("isolated");
//                                                ppBuyDownOrder.setPx(new BigDecimal(buyPutOptionBidPrice).toPlainString());
//                                                ppBuyDownOrder.setSz(String.valueOf(size));
//                                                ppBuyDownOrder.setSide("buy");
//                                                ppBuyDownOrder.setOrdType("fok");
////                ppUpOrder.setPosSide("long");
//                                                ppBuyDownOrder.setType("2");
//                                                OptionsOrder bulPutOptionsOrder = new OptionsOrder();
//                                                bulPutOptionsOrder.setInstrumentId(buyPutOptionInstId);
//                                                bulPutOptionsOrder.setCreateTime(new Date());
//                                                bulPutOptionsOrder.setStrategy("optionNetGrid");
//                                                bulPutOptionsOrder.setIsMock(Byte.valueOf("0"));
//                                                bulPutOptionsOrder.setType(Byte.valueOf(ppBuyDownOrder.getType()));
//                                                bulPutOptionsOrder.setPrice(new BigDecimal(ppBuyDownOrder.getPx()));
//                                                bulPutOptionsOrder.setSize(new BigDecimal(ppBuyDownOrder.getSz()));
//
//                                                bulPutOptionsOrder.setSymbol(symbol);
//                                                bulPutOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
//                                                bulPutOptionsOrder.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
//                                                bulPutOptionsOrder.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
//                                                bulPutOptionsOrder.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
//                                                bulPutOptionsOrder.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
//                                                bulPutOptionsOrder.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
//
//                                                //下单
//                                                String buyPutOrderId = tradeAPIService.placeOptionsOrder(site, ppBuyDownOrder, bulPutOptionsOrder);
//                                                log.info("买入看跌期权 {}-{},orderId:{}", buyPutOptionInstId, JSON.toJSONString(ppBuyDownOrder), buyPutOrderId);
//                                                messageService.sendStrategyMessage("optionNetGrid买入看跌期权", "optionNetGrid买入看跌期权-instId:" + buyPutOptionInstId + ",price:" + buyPutOptionBidPrice);
//
//                                                if (buyPutOrderId != null) {
//                                                    optionsOrderMapper.updateStatus(buyPutOrderId, 2);
//                                                }
//                                            }
//                                        }
                                    }
                                }
                            }
                        }


                    }
                }
            }
        }
    }

    public void dynamicDeltaHedging(String site, String instrumentId, String symbol, Double increment, int size) {

        List<Integer> unProcessedStatuses = new ArrayList<>();
        unProcessedStatuses.add(99);
        unProcessedStatuses.add(0);
        unProcessedStatuses.add(1);
        try {
            List<SwapOrder> swapOrders = swapOrderMapper.selectByStatus(instrumentId, "dynamicDeltaHedging", unProcessedStatuses);
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
            List<OptionsOrder> optionsOrders = optionsOrderMapper.selectByStatus(symbol, "dynamicDeltaHedging", unProcessedStatuses);
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

        String strikeDate = getOptionExpireTime(75);
        List<Integer> processingStatuses = new ArrayList<>();
        processingStatuses.add(2);

        List<OptionsOrder> processingOrders = optionsOrderMapper.selectByStatus(symbol, "dynamicDeltaHedging", processingStatuses);

        if (CollectionUtils.isEmpty(processingOrders)) {
            log.info("dynamicDeltaHedging 当前无进行中订单");
            //买入看涨期权
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentCallOptionMarketData = null;
                Long currentCallStrikePrice = null;
                Double currentCallStrikeDelta = null;
                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    if (strikePrice < currentPrice) {
                        continue;
                    }
                    double delta = Double.parseDouble(optionMarketData.getDelta());
                    if ("C".equals(optionInstArr[4]) && delta > 0.25) {
                        if (currentCallOptionMarketData == null || delta < currentCallStrikeDelta) {
                            currentCallStrikeDelta = delta;
                            currentCallOptionMarketData = optionMarketData;
                        }
                    }
                }
                if (currentCallOptionMarketData != null) {
                    log.info("期权市场数据{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(currentCallOptionMarketData));
                    //获取期权的价格数据
                    HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentCallOptionMarketData.getInstId(), null);
                    log.info("期权深度数据{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(optionOrderBookDatas));
                    if (!"0".equals(optionOrderBookDatas.getCode()) || optionOrderBookDatas.getData().size() <= 0
                            || optionOrderBookDatas.getData().get(0).getAsks().size() <= 0) {
                        return;
                    }
                    String optionAskPrice = optionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
                    log.info("期权卖一价{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(optionAskPrice));

                    String optionInstId = currentCallOptionMarketData.getInstId();
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
                    optionsOrder.setStrategy("dynamicDeltaHedging");
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
                    log.info("买入看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                    if (orderId == null) {
                        return;
                    }
                    optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                    if (optionsOrder == null) {
                        return;
                    }
                    //插入操作log
                    OptionsOrderLog optionsOrderLog = new OptionsOrderLog();
                    optionsOrderLog.setInstrumentId(optionInstId);
                    optionsOrderLog.setCreateTime(new Date());
                    optionsOrderLog.setStrategy("dynamicDeltaHedging");
                    optionsOrderLog.setIsMock(Byte.valueOf("0"));
                    optionsOrderLog.setType(Byte.valueOf(ppUpOrder.getType()));
                    optionsOrderLog.setPrice(new BigDecimal(ppUpOrder.getPx()));
                    optionsOrderLog.setSize(new BigDecimal(ppUpOrder.getSz()));

                    optionsOrderLog.setSymbol(symbol);
                    optionsOrderLog.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    optionsOrderLog.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                    optionsOrderLog.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                    optionsOrderLog.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                    optionsOrderLog.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                    optionsOrderLog.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
                    optionsOrderLog.setReferId(optionsOrder.getId());

                    optionsOrderLogMapper.insert(optionsOrderLog);

                    messageService.sendStrategyMessage("dynamicDeltaHedging买入看涨期权", "dynamicDeltaHedging买入看涨期权:" + currentCallOptionMarketData.getInstId() +
                            ",price:" + currentPrice + ",delta:" + currentCallOptionMarketData.getDelta() + ",gamma:" + currentCallOptionMarketData.getGamma()
                            + ",vega:" + currentCallOptionMarketData.getVega() + ",theta:" + currentCallOptionMarketData.getTheta() + ",vol:" + currentCallOptionMarketData.getVolLv());

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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

                    //查看合约持仓
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
                        if (apiPositionVO.getPosSide().equals("short") && Double.valueOf(apiPositionVO.getAvailPos()) > 0) {
                            downPosition = apiPositionVO;
                            shortPosition = Double.valueOf(apiPositionVO.getAvailPos());
                        }

                    }
                    log.info("持仓{}-空{}", instrumentId, shortPosition);
                    BigDecimal swapSize = new BigDecimal(size).multiply(optionsOrder.getDelta()).setScale(2, RoundingMode.CEILING);
                    if (downPosition != null) {
                        if (shortPosition > swapSize.doubleValue()) {
                            PlaceOrder ppDownOrder = new PlaceOrder();
                            ppDownOrder.setInstId(instrumentId);
                            ppDownOrder.setTdMode("cross");
                            ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                            ppDownOrder.setSz(new BigDecimal(Double.valueOf(downPosition.getPos()) - swapSize.doubleValue()).setScale(2, RoundingMode.CEILING).toPlainString());
                            ppDownOrder.setSide("buy");
                            ppDownOrder.setOrdType("market");
                            ppDownOrder.setPosSide("short");
                            ppDownOrder.setType("4");
                            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "dynamicDeltaHedging");
                            messageService.sendStrategyMessage("dynamicDeltaHedging合约平空", "dynamicDeltaHedging合约平空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + ppDownOrder.getSz());
                            log.info("合约平空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
                        } else if (shortPosition < swapSize.doubleValue()) {
                            PlaceOrder ppDownOrder = new PlaceOrder();
                            ppDownOrder.setInstId(instrumentId);
                            ppDownOrder.setTdMode("cross");
                            ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                            ppDownOrder.setSz(new BigDecimal(Double.valueOf(downPosition.getPos()) - swapSize.doubleValue()).abs().setScale(2, RoundingMode.CEILING).toPlainString());
                            ppDownOrder.setSide("sell");
                            ppDownOrder.setOrdType("market");
                            ppDownOrder.setPosSide("short");
                            ppDownOrder.setType("2");
                            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "dynamicDeltaHedging");
                            messageService.sendStrategyMessage("dynamicDeltaHedging合约开空", "dynamicDeltaHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + ppDownOrder.getSz());
                            log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
                        }
                    } else {
                        //合约开空
                        PlaceOrder ppDownOrder = new PlaceOrder();
                        ppDownOrder.setInstId(instrumentId);
                        ppDownOrder.setTdMode("cross");
                        ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                        ppDownOrder.setSz(swapSize.toPlainString());
                        ppDownOrder.setSide("sell");
                        ppDownOrder.setOrdType("market");
                        ppDownOrder.setPosSide("short");
                        ppDownOrder.setType("2");
                        JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "dynamicDeltaHedging");
                        messageService.sendStrategyMessage("dynamicDeltaHedging合约开空", "dynamicDeltaHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + ppDownOrder.getSz());
                        log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
                    }
                }
            }

            return;
        }

        for (OptionsOrder optionsOrder : processingOrders) {
            log.info("dynamicDeltaHedging 进行中订单, orderId:{}, instId:{}", optionsOrder.getId(), optionsOrder.getInstrumentId());
            //计算delta
            OptionsOrderLog optionsOrderLog = optionsOrderLogMapper.selectByReferId(optionsOrder.getId());
            if (optionsOrderLog == null) {
                continue;
            }
            log.info("dynamicDeltaHedging 进行中订单, orderId:{}, order logId:{}, instId:{}", optionsOrder.getId(), optionsOrderLog.getId(), optionsOrder.getInstrumentId());
            double lastDelta = optionsOrderLog.getDelta().doubleValue();
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentCallOptionMarketData = null;
                Long currentCallStrikePrice = null;
                Double currentCallStrikeDelta = null;
                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    double delta = Double.parseDouble(optionMarketData.getDelta());
                    if (optionInstId.equals(optionsOrderLog.getInstrumentId())) {
                        currentCallStrikeDelta = delta;
                        currentCallOptionMarketData = optionMarketData;
                    }
                }
                if (currentCallOptionMarketData != null) {
                    double currentDelta = Double.valueOf(currentCallStrikeDelta);
                    log.info("dynamicDeltaHedging 进行中订单, orderId:{}, order logId:{}, instId:{},当前delta:{}, 上次delta:{}",
                            optionsOrder.getId(), optionsOrderLog.getId(), optionsOrder.getInstrumentId(), currentDelta, lastDelta);
                    if (currentDelta > 0.75) {
                        //获取期权的价格数据
                        HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentCallOptionMarketData.getInstId(), null);
                        log.info("期权深度数据{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(optionOrderBookDatas));
                        if (!"0".equals(optionOrderBookDatas.getCode()) || optionOrderBookDatas.getData().size() <= 0
                                || optionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                            continue;
                        }
                        String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                        log.info("期权买一价{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(optionBidPrice));

                        String optionInstId = currentCallOptionMarketData.getInstId();
                        PlaceOrder ppUpOrder = new PlaceOrder();
                        ppUpOrder.setInstId(optionInstId);
                        ppUpOrder.setTdMode("isolated");
                        ppUpOrder.setPx(new BigDecimal(optionBidPrice).toPlainString());
                        ppUpOrder.setSz(String.valueOf(size));
                        ppUpOrder.setSide("sell");
                        ppUpOrder.setOrdType("fok");
                        ppUpOrder.setType("2");
                        OptionsOrder sellOptionsOrder = new OptionsOrder();
                        sellOptionsOrder.setInstrumentId(optionInstId);
                        sellOptionsOrder.setCreateTime(new Date());
                        sellOptionsOrder.setStrategy("dynamicDeltaHedging");
                        sellOptionsOrder.setIsMock(Byte.valueOf("0"));
                        sellOptionsOrder.setType(Byte.valueOf(ppUpOrder.getType()));
                        sellOptionsOrder.setPrice(new BigDecimal(ppUpOrder.getPx()));
                        sellOptionsOrder.setSize(new BigDecimal(ppUpOrder.getSz()));

                        sellOptionsOrder.setSymbol(symbol);
                        sellOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                        sellOptionsOrder.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                        sellOptionsOrder.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                        sellOptionsOrder.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                        sellOptionsOrder.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                        sellOptionsOrder.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));

                        //下单
                        String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, optionsOrder);
                        log.info("卖出看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                        if (orderId == null) {
                            return;
                        }
                        optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                        if (optionsOrder == null) {
                            return;
                        }
                        //插入操作log
                        OptionsOrderLog sellOptionsOrderLog = new OptionsOrderLog();
                        sellOptionsOrderLog.setInstrumentId(optionInstId);
                        sellOptionsOrderLog.setCreateTime(new Date());
                        sellOptionsOrderLog.setStrategy("dynamicDeltaHedging");
                        sellOptionsOrderLog.setIsMock(Byte.valueOf("0"));
                        sellOptionsOrderLog.setType(Byte.valueOf(ppUpOrder.getType()));
                        sellOptionsOrderLog.setPrice(new BigDecimal(ppUpOrder.getPx()));
                        sellOptionsOrderLog.setSize(new BigDecimal(ppUpOrder.getSz()));

                        sellOptionsOrderLog.setSymbol(symbol);
                        sellOptionsOrderLog.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                        sellOptionsOrderLog.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                        sellOptionsOrderLog.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                        sellOptionsOrderLog.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                        sellOptionsOrderLog.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                        sellOptionsOrderLog.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
                        sellOptionsOrderLog.setReferId(optionsOrder.getId());

                        optionsOrderLogMapper.insert(optionsOrderLog);

                        messageService.sendStrategyMessage("dynamicDeltaHedging卖出看涨期权", "dynamicDeltaHedging卖出看涨期权:" + currentCallOptionMarketData.getInstId() +
                                ",price:" + currentPrice + ",delta:" + currentCallOptionMarketData.getDelta() + ",gamma:" + currentCallOptionMarketData.getGamma()
                                + ",vega:" + currentCallOptionMarketData.getVega() + ",theta:" + currentCallOptionMarketData.getTheta() + ",vol:" + currentCallOptionMarketData.getVolLv());

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
                        optionsOrderMapper.updateStatus(orderId, 100);
                        optionsOrderMapper.updateStatus(optionsOrder.getOrderId(), 100);


                    }

                    if (currentDelta > lastDelta && currentDelta - lastDelta > increment) {
                        //合约开空
                        BigDecimal swapSize = new BigDecimal(size).multiply(new BigDecimal(currentDelta - lastDelta)).abs().setScale(2, RoundingMode.CEILING);
                        PlaceOrder ppDownOrder = new PlaceOrder();
                        ppDownOrder.setInstId(instrumentId);
                        ppDownOrder.setTdMode("cross");
                        ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                        ppDownOrder.setSz(swapSize.toPlainString());
                        ppDownOrder.setSide("sell");
                        ppDownOrder.setOrdType("market");
                        ppDownOrder.setPosSide("short");
                        ppDownOrder.setType("2");
                        JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "dynamicDeltaHedging");

                        OptionsOrderLog currentOptionsOrderLog = new OptionsOrderLog();
                        currentOptionsOrderLog.setInstrumentId(optionsOrderLog.getInstrumentId());
                        currentOptionsOrderLog.setCreateTime(new Date());
                        currentOptionsOrderLog.setStrategy("dynamicDeltaHedging");
                        currentOptionsOrderLog.setIsMock(Byte.valueOf("0"));
                        currentOptionsOrderLog.setType(Byte.valueOf(optionsOrderLog.getType()));
                        currentOptionsOrderLog.setPrice(new BigDecimal(ppDownOrder.getPx()));
                        currentOptionsOrderLog.setSize(new BigDecimal(ppDownOrder.getSz()));

                        currentOptionsOrderLog.setSymbol(symbol);
                        currentOptionsOrderLog.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                        currentOptionsOrderLog.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                        currentOptionsOrderLog.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                        currentOptionsOrderLog.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                        currentOptionsOrderLog.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                        currentOptionsOrderLog.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
                        currentOptionsOrderLog.setReferId(optionsOrder.getId());

                        optionsOrderLogMapper.insert(currentOptionsOrderLog);

                        messageService.sendStrategyMessage("dynamicDeltaHedging合约开空", "dynamicDeltaHedging合约开空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + ppDownOrder.getSz());
                        log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
                    }

                    if (currentDelta < lastDelta && lastDelta - currentDelta > increment) {
                        //查看合约持仓
                        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, null, instrumentId, null);
                        if (positionsResult == null || !positionsResult.getCode().equals("0")) {
                            continue;
                        }
                        PositionInfo downPosition = null;
                        double shortPosition = 0;
                        for (PositionInfo apiPositionVO : positionsResult.getData()) {
                            if (apiPositionVO.getAvailPos().equals("")) {
                                continue;
                            }
                            if (apiPositionVO.getPosSide().equals("short") && Double.valueOf(apiPositionVO.getPos()) > 0 && Double.valueOf(apiPositionVO.getAvailPos()) > 0) {
                                downPosition = apiPositionVO;
                                shortPosition = Double.valueOf(apiPositionVO.getAvailPos());
                            }

                        }
                        if (shortPosition <= 0) {
                            continue;
                        }
                        //合约平空
                        BigDecimal swapSize = new BigDecimal(size).multiply(new BigDecimal(lastDelta - currentDelta)).abs().setScale(2, RoundingMode.CEILING);
                        swapSize = swapSize.min(new BigDecimal(shortPosition));
                        PlaceOrder ppDownOrder = new PlaceOrder();
                        ppDownOrder.setInstId(instrumentId);
                        ppDownOrder.setTdMode("cross");
                        ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
                        ppDownOrder.setSz(swapSize.toPlainString());
                        ppDownOrder.setSide("buy");
                        ppDownOrder.setOrdType("market");
                        ppDownOrder.setPosSide("short");
                        ppDownOrder.setType("4");
                        JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "dynamicDeltaHedging");
                        messageService.sendStrategyMessage("dynamicDeltaHedging合约平空", "dynamicDeltaHedging合约平空-instId:" + instrumentId + ",price:" + currentPrice + ",size:" + ppDownOrder.getSz());
                        log.info("合约平空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));

                        OptionsOrderLog currentOptionsOrderLog = new OptionsOrderLog();
                        currentOptionsOrderLog.setInstrumentId(optionsOrderLog.getInstrumentId());
                        currentOptionsOrderLog.setCreateTime(new Date());
                        currentOptionsOrderLog.setStrategy("dynamicDeltaHedging");
                        currentOptionsOrderLog.setIsMock(Byte.valueOf("0"));
                        currentOptionsOrderLog.setType(Byte.valueOf(optionsOrderLog.getType()));
                        currentOptionsOrderLog.setPrice(new BigDecimal(ppDownOrder.getPx()));
                        currentOptionsOrderLog.setSize(new BigDecimal(ppDownOrder.getSz()));

                        currentOptionsOrderLog.setSymbol(symbol);
                        currentOptionsOrderLog.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                        currentOptionsOrderLog.setDelta(new BigDecimal(currentCallOptionMarketData.getDelta()));
                        currentOptionsOrderLog.setGamma(new BigDecimal(currentCallOptionMarketData.getGamma()));
                        currentOptionsOrderLog.setVega(new BigDecimal(currentCallOptionMarketData.getVega()));
                        currentOptionsOrderLog.setTheta(new BigDecimal(currentCallOptionMarketData.getTheta()));
                        currentOptionsOrderLog.setVolLv(new BigDecimal(currentCallOptionMarketData.getVolLv()));
                        currentOptionsOrderLog.setReferId(optionsOrder.getId());

                        optionsOrderLogMapper.insert(currentOptionsOrderLog);
                    }
                }
            }
        }

    }


    public void computeOptionBenefit(String site, String instrumentId, String symbol, long startTime, String title) {
        try {

            HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, instrumentId);

            if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
                return;
            }

            Ticker apiTickerVO = swapTicker.getData().get(0);
            Double currentPrice = Double.valueOf(apiTickerVO.getLast());

            Long lastTime = startTime;

            BigDecimal profitSymbol = BigDecimal.ZERO;
            int size = 0;
            while (lastTime != null) {
                HttpResult<List<PositionInfo>> accountBillsResult = accountAPIV5Service.getHistoryPostions(site, "OPTION", null, symbol, null, null, null, null, null, String.valueOf(lastTime), "100");
                if (accountBillsResult == null || !"0".equals(accountBillsResult.getCode())) {
                    continue;
                }
                log.info("account bill result size:{}", accountBillsResult.getData().size());
                if (accountBillsResult.getData().size() <= 0) {
                    lastTime = null;
                    break;
                }
                lastTime = null;
                int index = 0;
                for (PositionInfo positionInfo : accountBillsResult.getData()) {
                    index++;
                    if (index == 100) {
                        lastTime = positionInfo.getuTime();
                    }
                    if (!positionInfo.getCcy().equals(symbol) || positionInfo.getType() != 2) {
                        continue;
                    }
                    log.info("position info: instId:{}, realizedPnl:{}, uTime:{}", positionInfo.getInstId(), positionInfo.getRealizedPnl(), new Date(positionInfo.getuTime()));
                    profitSymbol = profitSymbol.add(new BigDecimal(positionInfo.getRealizedPnl()));
                    size++;
                }
                Thread.sleep(500);
            }
            BigDecimal profitUsdt = profitSymbol.multiply(new BigDecimal(apiTickerVO.getLast())).setScale(4, BigDecimal.ROUND_DOWN);
            log.info("option:{}, profit :{}, profitUsdt:{}, size:{}", symbol, profitSymbol, profitUsdt, size);

            StringBuilder result = new StringBuilder();
            result.append(title).append(":\n")
                    .append(symbol).append(":").append(profitSymbol.setScale(6, BigDecimal.ROUND_DOWN).toPlainString())
                    .append(",USDT:").append(profitUsdt.toPlainString())
                    .append(",size:").append(size);

            messageService.sendMessage(title, result.toString());
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void monitorIV(String site, String instrumentId, String symbol, String strikeDate) {
        try {
            HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, instrumentId);

            if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
                return;
            }

            com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker spotTicker = spotV5Service.getTicker(site, symbol + "-USDT");
            if (spotTicker == null) {
                return;
            }

            Ticker apiTickerVO = swapTicker.getData().get(0);
            Double currentPrice = Double.valueOf(apiTickerVO.getLast());
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentCallOptionMarketData = null;
                Long currentCallStrikePrice = null;
                Double currentCallStrikeDelta = null;
                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    if (optionInstId.equals(instrumentId)) {
                        currentCallOptionMarketData = optionMarketData;
                    }
                }
                if (currentCallOptionMarketData != null) {
                    messageService.sendMonitorMessage("期权IV监控", "期权IV监控:" + currentCallOptionMarketData.getInstId() +
                            ",price:" + currentPrice + ",spotPrice:" + spotTicker.getLast() + ",delta:" + currentCallOptionMarketData.getDelta()  + ",gamma:" + currentCallOptionMarketData.getGamma()
                            + ",vega:" + currentCallOptionMarketData.getVega()  + ",theta:" + currentCallOptionMarketData.getTheta()
                            + ",vol:" + currentCallOptionMarketData.getVolLv() +",markVol:" + currentCallOptionMarketData.getMarkVol()
                            +",bidVol:" + currentCallOptionMarketData.getBidVol()   +",askVol:" + currentCallOptionMarketData.getAskVol()   +",realVol:" + currentCallOptionMarketData.getRealVol());
                }
            }
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
