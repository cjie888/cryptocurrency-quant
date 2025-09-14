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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
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
                        ppUpOrder.setSz(String.valueOf(size * 2));
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
                        if (status == null || !status.equals(2)) {
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
                            if (apiPositionVO.getPosSide().equals("net") && Double.valueOf(apiPositionVO.getPos()) >= Double.valueOf(size * 2) && Double.valueOf(apiPositionVO.getAvailPos()) >= Double.valueOf(size * 2)) {
                                if (!apiPositionVO.getInstId().contains(symbol)) {
                                    continue;
                                }
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
                            ppUpOrder.setSz(String.valueOf(size * 2));
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
                            if (status == null || !status.equals(2)) {
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
                        ppUpOrder.setSz(String.valueOf(size * 2));
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
                        if (!status.equals(2)) {
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

    void netGrid2(String site, String instrumentId, String symbol, int size, double baseIncrement, double callIncrement, double putDecrement) {

        HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, symbol + "-USDT");
        if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
            messageService.sendStrategyMessage("netGrid获取不到价格", "netGrid获取不到价格,请手动检查");
            return;
        }
        Ticker apiTickerVO = swapTicker.getData().get(0);
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());

        log.info("当前价格{}-{}-{}", site, currentPrice, symbol);
        for (int i = 1; i <= 4; i++) {
            String expireTime = getNextNDay(i);
            for (int j = i; j <= 4; j++) {
                double callStrikePrice = currentPrice * (1 + baseIncrement + j * callIncrement);
                double putStrikePrice = currentPrice * (1 - baseIncrement - j * putDecrement);
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
                    if ("C".equals(optionInstArr[4]) && delta > 0.3) {
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
                    if (!status.equals(2)) {
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
            String currentStrikeDate = optionsOrder.getInstrumentId().split("-")[2];
            double lastDelta = optionsOrderLog.getDelta().doubleValue();
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", currentStrikeDate);
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
                    if (currentDelta > 0.6) {

                        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, "OPTION", optionsOrder.getInstrumentId(), null);
                        log.info("期权持仓{}-看涨{}, result:{}", symbol, JSON.toJSONString(positionsResult));
                        if (positionsResult == null || !positionsResult.getCode().equals("0")) {
                            messageService.sendStrategyMessage("dynamicDeltaHedging无持仓", "dynamicDeltaHedging无持仓:" + optionsOrder.getInstrumentId());
                            continue;
                        }

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
                        String orderId = tradeAPIService.placeOptionsOrder(site, ppUpOrder, sellOptionsOrder);
                        log.info("卖出看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                        if (orderId == null) {
                            continue;
                        }
                        optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                        if (optionsOrder == null) {
                            continue;
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
                            continue;
                        }
                        String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                        if (state == null || STATES.get(state) == null) {
                            continue;
                        }
                        Integer status = STATES.get(state);
                        if (!optionsOrder.getStatus().equals(status)) {
                            optionsOrderMapper.updateStatus(orderId, status);
                        }
                        if (!status.equals(2)) {
                            continue;
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
            BigDecimal isolatedProfit = BigDecimal.ZERO;
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
                    log.info("position info: instId:{}, realizedPnl:{}, uTime:{}, pos:{}", positionInfo.getInstId(), positionInfo.getRealizedPnl(), new Date(positionInfo.getuTime()), JSON.toJSONString(positionInfo));
                    profitSymbol = profitSymbol.add(new BigDecimal(positionInfo.getRealizedPnl()));
                    if ("isolated".equals(positionInfo.getMgnMode())) {
                        isolatedProfit = isolatedProfit.add(new BigDecimal(positionInfo.getRealizedPnl()));
                    }
                    size++;
                }
                Thread.sleep(500);
            }
            BigDecimal profitUsdt = profitSymbol.multiply(new BigDecimal(apiTickerVO.getLast())).setScale(4, BigDecimal.ROUND_DOWN);
            BigDecimal isolatedProfitUsdt = isolatedProfit.multiply(new BigDecimal(apiTickerVO.getLast())).setScale(4, BigDecimal.ROUND_DOWN);

            log.info("option:{}, profit :{}, profitUsdt:{}, size:{}", symbol, profitSymbol, profitUsdt, size);

            StringBuilder result = new StringBuilder();
            result.append(title).append(":\n")
                    .append(symbol).append(":").append(profitSymbol.setScale(6, BigDecimal.ROUND_DOWN).toPlainString()).append("\n")
                    .append("逐仓").append(symbol).append(":").append(isolatedProfit.setScale(6, BigDecimal.ROUND_DOWN).toPlainString()).append("\n")
                    .append("USDT:").append(profitUsdt.toPlainString()).append("\n")
                    .append("逐仓USDT:").append(isolatedProfitUsdt.toPlainString()).append("\n")
                    .append("size:").append(size);

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

    public void monitorIvSkew(String site, String symbol) {
        try {
            String strikeDate = getOptionExpireTime(75);
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                String chartPath = "/data/option/iv_skew.png";
                createVolatilitySmileChart(optionsMarketDatas.getData(), strikeDate, chartPath);
                messageService.sendPhoto(chartPath, "Skew曲线");

                new File(chartPath).delete();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void createVolatilitySmileChart(List<OptionMarketData> options, String expiry, String outputPath) throws IOException {
        XYSeries callSeries = new XYSeries("Call IV");
        XYSeries putSeries = new XYSeries("Put IV");

        // Separate calls and puts
        for (OptionMarketData option : options) {
            String optionInstId = option.getInstId();
            String[] optionInstArr = optionInstId.split("-");
            System.out.println( optionInstId + ",strikePrice:" + optionInstArr[3] + ",vol:" + option.getMarkVol());
            if (optionInstArr[4].equals("P")) {
                callSeries.add(Long.parseLong(optionInstArr[3]), new BigDecimal(option.getVega()).setScale(6, BigDecimal.ROUND_DOWN));
            } else {
//                putSeries.add(Long.parseLong(optionInstArr[3]), 0.1);
                putSeries.add(Long.parseLong(optionInstArr[3]), new BigDecimal(option.getVega()).setScale(6, BigDecimal.ROUND_DOWN));
            }
        }
        System.out.println("Call points: " + callSeries.getItemCount());
        System.out.println("Put points: " + putSeries.getItemCount());

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(callSeries);
        dataset.addSeries(putSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "BTC Volatility Smile (" + expiry + ")", // Title
                "Strike Price", // X-axis label
                "Implied Volatility", // Y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        // Customize plot
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Set line and shape properties
        renderer.setSeriesPaint(0, Color.RED);    // Call IV
        renderer.setSeriesPaint(1, Color.BLUE);   // Put IV
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));           // Thicker Call line
        renderer.setSeriesStroke(1, new BasicStroke(1.5f));           // Solid Put line
        renderer.setSeriesShapesVisible(0, true);  // Show points for Call
        renderer.setSeriesShapesVisible(1, true);  // Show points for Put
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6)); // Circle for Call
        renderer.setSeriesShape(1, new Ellipse2D.Double(-3, -3, 6, 6)); // Circle for Put
        plot.setRenderer(renderer);

        for (int series = 0; series < dataset.getSeriesCount(); series++) {
            XYSeries xySeries = dataset.getSeries(series);
            for (int item = 0; item < xySeries.getItemCount(); item++) {
                double x = xySeries.getX(item).doubleValue(); // Strike Price
                double y = xySeries.getY(item).doubleValue(); // Implied Volatility
//                String label = String.format("(%d, %.2f)", (int) x, y);
                String label = String.format("(%d, %.4f)", (int) x, y);
                XYTextAnnotation annotation = new XYTextAnnotation(label, x, y);
                annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT); // Position text to the right
                annotation.setFont(new Font("SansSerif", Font.PLAIN, 10));
                annotation.setPaint(series == 0 ? Color.RED : Color.BLUE); // Match series color
                // Adjust position to avoid overlap
                annotation.setX(x + 200); // Move right by 200 units (adjust as needed)
                annotation.setY(y);
                plot.addAnnotation(annotation);
            }
        }

        // Save chart as PNG
        File outputFile = new File(outputPath);
        ChartUtils.saveChartAsPNG(outputFile, chart, 1920, 1080);
    }


    public void coveredCall(String site, String symbol, double size, double increment) {
        try {
            HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, symbol + "-USDT");
            if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
                messageService.sendStrategyMessage("netGrid获取不到价格", "netGrid获取不到价格,请手动检查");
                return;
            }
            Ticker apiTickerVO = swapTicker.getData().get(0);
            Double currentPrice = Double.valueOf(apiTickerVO.getLast());

            log.info("当前价格{}-{}-{}", site, currentPrice, symbol);


            String strikeDate = getNextNDay(1);

            //卖出看涨期权
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
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
                    if (strikePrice > currentPrice) {
                        if ("C".equals(optionInstArr[4])) {
                            if (currentCallOptionMarketData == null || strikePrice < currentCallStrikePrice) {
                                currentCallStrikePrice = strikePrice;
                                currentCallOptionMarketData = optionMarketData;
                            }
                        }
                    }
                    if (strikePrice < currentPrice) {
                        if ("P".equals(optionInstArr[4])) {
                            if (currentPutOptionMarketData == null || strikePrice > currentPutStrikePrice) {
                                currentPutStrikePrice = strikePrice;
                                currentPutOptionMarketData = optionMarketData;
                            }
                        }
                    }

                }


                if (currentCallOptionMarketData != null) {
                    log.info("期权市场数据{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(currentCallOptionMarketData));
                    //获取期权的价格数据
                    HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentCallOptionMarketData.getInstId(), null);
                    log.info("期权深度数据{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(optionOrderBookDatas));
                    if (!"0".equals(optionOrderBookDatas.getCode()) || optionOrderBookDatas.getData().size() <= 0
                            || optionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                        return;
                    }
                    String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                    log.info("期权买一价{}:{}", currentCallOptionMarketData.getInstId(), JSON.toJSONString(optionBidPrice));

                    String optionInstId = currentCallOptionMarketData.getInstId();
                    PlaceOrder ppUpOrder = new PlaceOrder();
                    ppUpOrder.setInstId(optionInstId);
                    ppUpOrder.setTdMode("cross");
                    ppUpOrder.setPx(new BigDecimal(optionBidPrice).toPlainString());
                    ppUpOrder.setSz(String.valueOf(size));
                    ppUpOrder.setSide("sell");
                    ppUpOrder.setOrdType("fok");
                    ppUpOrder.setType("2");
                    OptionsOrder optionsOrder = new OptionsOrder();
                    optionsOrder.setInstrumentId(optionInstId);
                    optionsOrder.setCreateTime(new Date());
                    optionsOrder.setStrategy("coveredCall");
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
                    if (orderId == null) {
                        return;
                    }
                    optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                    if (optionsOrder == null) {
                        return;
                    }


                    messageService.sendStrategyMessage("coveredCall卖出看涨期权", "coveredCall卖出看涨期权:" + currentCallOptionMarketData.getInstId() +
                            ",price:" + optionBidPrice + ",delta:" + currentCallOptionMarketData.getDelta() + ",gamma:" + currentCallOptionMarketData.getGamma()
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
                    if (status == null || !status.equals(2)) {
                        return;
                    }
                    optionsOrderMapper.updateStatus(orderId, 100);
                    optionsOrderMapper.updateStatus(optionsOrder.getOrderId(), 100);
                }

                if (currentPutOptionMarketData != null) {
                    log.info("期权市场数据{}:{}", currentPutOptionMarketData.getInstId(), JSON.toJSONString(currentPutOptionMarketData));
                    //获取期权的价格数据
                    HttpResult<List<OrderBook>> optionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentPutOptionMarketData.getInstId(), null);
                    log.info("期权深度数据{}:{}", currentPutOptionMarketData.getInstId(), JSON.toJSONString(optionOrderBookDatas));
                    if (!"0".equals(optionOrderBookDatas.getCode()) || optionOrderBookDatas.getData().size() <= 0
                            || optionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                        return;
                    }
                    String optionBidPrice = optionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                    log.info("期权买一价{}:{}", currentPutOptionMarketData.getInstId(), JSON.toJSONString(optionBidPrice));

                    String optionInstId = currentPutOptionMarketData.getInstId();
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
                    optionsOrder.setStrategy("coveredCall");
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
                    log.info("卖出看涨期权 {}-{},orderId:{}", optionInstId, JSON.toJSONString(ppUpOrder), orderId);
                    if (orderId == null) {
                        return;
                    }
                    optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
                    if (optionsOrder == null) {
                        return;
                    }


                    messageService.sendStrategyMessage("coveredCall卖出看跌期权", "coveredCall卖出看跌期权:" + currentPutOptionMarketData.getInstId() +
                            ",price:" + optionBidPrice + ",delta:" + currentPutOptionMarketData.getDelta() + ",gamma:" + currentPutOptionMarketData.getGamma()
                            + ",vega:" + currentPutOptionMarketData.getVega() + ",theta:" + currentPutOptionMarketData.getTheta() + ",vol:" + currentPutOptionMarketData.getVolLv());

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
                    if (status == null || !status.equals(2)) {
                        return;
                    }
                    optionsOrderMapper.updateStatus(orderId, 100);
                    optionsOrderMapper.updateStatus(optionsOrder.getOrderId(), 100);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String placeOrderAndUpdateStatus(String site, PlaceOrder placeOrder, OptionsOrder optionsOrder) {
        //下单
        String orderId = tradeAPIService.placeOptionsOrder(site, placeOrder, optionsOrder);
        if (orderId == null) {
            return null;
        }
        optionsOrder = optionsOrderMapper.selectByOrderId(orderId);
        if (optionsOrder == null) {
            return null;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JSONObject result = tradeAPIService.getOrderDetails(site, placeOrder.getInstId(), orderId, null);

        log.info("options order status {}", JSON.toJSONString(result));
        if (result == null) {
            return null;
        }
        String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
        if (state == null || STATES.get(state) == null) {
            return null;
        }
        Integer status = STATES.get(state);
        if (!optionsOrder.getStatus().equals(status)) {
            optionsOrderMapper.updateStatus(orderId, status);
        }
        if (status == null || !status.equals(2)) {
            return null;
        }
        return orderId;
    }
    public void butterfly(String site, String symbol, double size, int days, double increment) {
        try {
            HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, symbol + "-USDT");
            if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
                messageService.sendStrategyMessage("netGrid获取不到价格", "netGrid获取不到价格,请手动检查");
                return;
            }
            Ticker apiTickerVO = swapTicker.getData().get(0);
            Double currentPrice = Double.valueOf(apiTickerVO.getLast());

            Double lowPrice = currentPrice * (1-increment);
            Double highPrice = currentPrice * (1+increment);

            log.info("当前价格{}-{}-{}", site, currentPrice, symbol);


            String strikeDate = getNextNDay(days);

            //卖出看涨期权 2份
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentSellCallOptionMarketData = null;
                Long currentSellCallStrikePrice = null;
                OptionMarketData currentLowOptionMarketData = null;
                Long currentLowStrikePrice = null;
                OptionMarketData currentHighOptionMarketData = null;
                Long currentHighStrikePrice = null;
                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    if (strikePrice > currentPrice) {
                        if ("C".equals(optionInstArr[4])) {
                            if (currentSellCallOptionMarketData == null || strikePrice < currentSellCallStrikePrice) {
                                currentSellCallStrikePrice = strikePrice;
                                currentSellCallOptionMarketData = optionMarketData;
                            }
                        }
                    }
                    if (strikePrice < lowPrice) {
                        if ("C".equals(optionInstArr[4])) {
                            if (currentLowOptionMarketData == null || strikePrice > currentLowStrikePrice) {
                                currentLowStrikePrice = strikePrice;
                                currentLowOptionMarketData = optionMarketData;
                            }
                        }
                    }
                    if (strikePrice > highPrice) {
                        if ("C".equals(optionInstArr[4])) {
                            if (currentHighOptionMarketData == null || strikePrice < currentHighStrikePrice) {
                                currentHighStrikePrice = strikePrice;
                                currentHighOptionMarketData = optionMarketData;
                            }
                        }
                    }

                }


                if (currentSellCallOptionMarketData != null && currentLowOptionMarketData != null && currentHighOptionMarketData != null) {
                    log.info("期权市场数据2倍卖{}:{}", currentSellCallOptionMarketData.getInstId(), JSON.toJSONString(currentSellCallOptionMarketData));
                    log.info("期权市场数据低买{}:{}", currentLowOptionMarketData.getInstId(), JSON.toJSONString(currentLowOptionMarketData));
                    log.info("期权市场数据高买{}:{}", currentHighOptionMarketData.getInstId(), JSON.toJSONString(currentHighOptionMarketData));

                    //获取期权的价格数据
                    HttpResult<List<OrderBook>> sellOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentSellCallOptionMarketData.getInstId(), null);
                    log.info("期权深度数据2倍卖{}:{}", currentSellCallOptionMarketData.getInstId(), JSON.toJSONString(sellOptionOrderBookDatas));
                    if (!"0".equals(sellOptionOrderBookDatas.getCode()) || sellOptionOrderBookDatas.getData().size() <= 0
                            || sellOptionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                        return;
                    }
                    String sellOptionBidPrice = sellOptionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                    log.info("2倍卖期权买一价{}:{}", currentSellCallOptionMarketData.getInstId(), sellOptionBidPrice);

                    HttpResult<List<OrderBook>> lowOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentLowOptionMarketData.getInstId(), null);
                    log.info("期权深度数据低买{}:{}", currentLowOptionMarketData.getInstId(), JSON.toJSONString(lowOptionOrderBookDatas));
                    if (!"0".equals(lowOptionOrderBookDatas.getCode()) || lowOptionOrderBookDatas.getData().size() <= 0
                            || lowOptionOrderBookDatas.getData().get(0).getAsks().size() <= 0) {
                        return;
                    }
                    String lowOptionAskPrice = lowOptionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
                    log.info("低买期权卖一价{}:{}", currentLowOptionMarketData.getInstId(), lowOptionAskPrice);


                    HttpResult<List<OrderBook>> highOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentHighOptionMarketData.getInstId(), null);
                    log.info("期权深度数据高买{}:{}", currentHighOptionMarketData.getInstId(), JSON.toJSONString(highOptionOrderBookDatas));
                    if (!"0".equals(highOptionOrderBookDatas.getCode()) || highOptionOrderBookDatas.getData().size() <= 0
                            || highOptionOrderBookDatas.getData().get(0).getAsks().size() <= 0) {
                        return;
                    }
                    String highOptionAskPrice = highOptionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
                    log.info("低买期权卖一价{}:{}", currentHighOptionMarketData.getInstId(), highOptionAskPrice);


                    String lowOptionInstId = currentLowOptionMarketData.getInstId();
                    PlaceOrder lowPlaceOrder = new PlaceOrder();
                    lowPlaceOrder.setInstId(lowOptionInstId);
                    lowPlaceOrder.setTdMode("isolated");
                    lowPlaceOrder.setPx(new BigDecimal(lowOptionAskPrice).toPlainString());
                    lowPlaceOrder.setSz(String.valueOf(size));
                    lowPlaceOrder.setSide("buy");
                    lowPlaceOrder.setOrdType("fok");
                    lowPlaceOrder.setType("1");
                    OptionsOrder lowOptionsOrder = new OptionsOrder();
                    lowOptionsOrder.setInstrumentId(lowOptionInstId);
                    lowOptionsOrder.setCreateTime(new Date());
                    lowOptionsOrder.setStrategy("butterfly");
                    lowOptionsOrder.setIsMock(Byte.valueOf("0"));
                    lowOptionsOrder.setType(Byte.valueOf(lowPlaceOrder.getType()));
                    lowOptionsOrder.setPrice(new BigDecimal(lowPlaceOrder.getPx()));
                    lowOptionsOrder.setSize(new BigDecimal(lowPlaceOrder.getSz()));

                    lowOptionsOrder.setSymbol(symbol);
                    lowOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    lowOptionsOrder.setDelta(new BigDecimal(currentLowOptionMarketData.getDelta()));
                    lowOptionsOrder.setGamma(new BigDecimal(currentLowOptionMarketData.getGamma()));
                    lowOptionsOrder.setVega(new BigDecimal(currentLowOptionMarketData.getVega()));
                    lowOptionsOrder.setTheta(new BigDecimal(currentLowOptionMarketData.getTheta()));
                    lowOptionsOrder.setVolLv(new BigDecimal(currentLowOptionMarketData.getVolLv()));

                    String lowOrderId = placeOrderAndUpdateStatus(site, lowPlaceOrder, lowOptionsOrder);
                    if (lowOrderId == null) {
                        return;
                    }
                    log.info("买入看涨期权低 {}-{},orderId:{}", lowOptionInstId, JSON.toJSONString(lowPlaceOrder), lowOrderId);

                    messageService.sendStrategyMessage("butterfly买入看涨期权", "butterfly买入看涨期权:" + currentLowOptionMarketData.getInstId() +
                            ",price:" + lowOptionAskPrice + ",delta:" + currentLowOptionMarketData.getDelta() + ",gamma:" + currentLowOptionMarketData.getGamma()
                            + ",vega:" + currentLowOptionMarketData.getVega() + ",theta:" + currentLowOptionMarketData.getTheta() + ",vol:" + currentLowOptionMarketData.getVolLv());


                    String sellOptionInstId = currentSellCallOptionMarketData.getInstId();
                    PlaceOrder sellPlaceOrder = new PlaceOrder();
                    sellPlaceOrder.setInstId(sellOptionInstId);
                    sellPlaceOrder.setTdMode("cross");
                    sellPlaceOrder.setPx(new BigDecimal(sellOptionBidPrice).toPlainString());
                    sellPlaceOrder.setSz(String.valueOf(size * 2));
                    sellPlaceOrder.setSide("sell");
                    sellPlaceOrder.setOrdType("fok");
                    sellPlaceOrder.setType("2");
                    OptionsOrder optionsOrder = new OptionsOrder();
                    optionsOrder.setInstrumentId(sellOptionInstId);
                    optionsOrder.setCreateTime(new Date());
                    optionsOrder.setStrategy("butterfly");
                    optionsOrder.setIsMock(Byte.valueOf("0"));
                    optionsOrder.setType(Byte.valueOf(sellPlaceOrder.getType()));
                    optionsOrder.setPrice(new BigDecimal(sellPlaceOrder.getPx()));
                    optionsOrder.setSize(new BigDecimal(sellPlaceOrder.getSz()));

                    optionsOrder.setSymbol(symbol);
                    optionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    optionsOrder.setDelta(new BigDecimal(currentSellCallOptionMarketData.getDelta()));
                    optionsOrder.setGamma(new BigDecimal(currentSellCallOptionMarketData.getGamma()));
                    optionsOrder.setVega(new BigDecimal(currentSellCallOptionMarketData.getVega()));
                    optionsOrder.setTheta(new BigDecimal(currentSellCallOptionMarketData.getTheta()));
                    optionsOrder.setVolLv(new BigDecimal(currentSellCallOptionMarketData.getVolLv()));

                    String sellOrderId = placeOrderAndUpdateStatus(site, sellPlaceOrder, optionsOrder);
                    if (sellOrderId == null) {
                        return;
                    }
                    log.info("卖出看涨期权 {}-{},orderId:{}", sellOptionInstId, JSON.toJSONString(sellPlaceOrder), sellOrderId);

                    messageService.sendStrategyMessage("butterfly卖出看涨期权", "butterfly卖出看涨期权:" + currentSellCallOptionMarketData.getInstId() +
                            ",price:" + sellOptionBidPrice + ",delta:" + currentSellCallOptionMarketData.getDelta() + ",gamma:" + currentSellCallOptionMarketData.getGamma()
                            + ",vega:" + currentSellCallOptionMarketData.getVega() + ",theta:" + currentSellCallOptionMarketData.getTheta() + ",vol:" + currentSellCallOptionMarketData.getVolLv());

                    String highOptionInstId = currentHighOptionMarketData.getInstId();
                    PlaceOrder highPlaceOrder = new PlaceOrder();
                    highPlaceOrder.setInstId(highOptionInstId);
                    highPlaceOrder.setTdMode("isolated");
                    highPlaceOrder.setPx(new BigDecimal(highOptionAskPrice).toPlainString());
                    highPlaceOrder.setSz(String.valueOf(size));
                    highPlaceOrder.setSide("buy");
                    highPlaceOrder.setOrdType("fok");
                    highPlaceOrder.setType("1");
                    OptionsOrder highOptionsOrder = new OptionsOrder();
                    highOptionsOrder.setInstrumentId(highOptionInstId);
                    highOptionsOrder.setCreateTime(new Date());
                    highOptionsOrder.setStrategy("butterfly");
                    highOptionsOrder.setIsMock(Byte.valueOf("0"));
                    highOptionsOrder.setType(Byte.valueOf(highPlaceOrder.getType()));
                    highOptionsOrder.setPrice(new BigDecimal(highPlaceOrder.getPx()));
                    highOptionsOrder.setSize(new BigDecimal(highPlaceOrder.getSz()));

                    highOptionsOrder.setSymbol(symbol);
                    highOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    highOptionsOrder.setDelta(new BigDecimal(currentHighOptionMarketData.getDelta()));
                    highOptionsOrder.setGamma(new BigDecimal(currentHighOptionMarketData.getGamma()));
                    highOptionsOrder.setVega(new BigDecimal(currentHighOptionMarketData.getVega()));
                    highOptionsOrder.setTheta(new BigDecimal(currentHighOptionMarketData.getTheta()));
                    highOptionsOrder.setVolLv(new BigDecimal(currentHighOptionMarketData.getVolLv()));

                    String highOrderId = placeOrderAndUpdateStatus(site, highPlaceOrder, highOptionsOrder);
                    if (highOrderId == null) {
                        return;
                    }
                    log.info("买入看涨期权高 {}-{},orderId:{}", highOptionInstId, JSON.toJSONString(highPlaceOrder), highOrderId);

                    messageService.sendStrategyMessage("butterfly买入看涨期权", "butterfly买入看涨期权:" + currentHighOptionMarketData.getInstId() +
                            ",price:" + highOptionAskPrice + ",delta:" + currentHighOptionMarketData.getDelta() + ",gamma:" + currentHighOptionMarketData.getGamma()
                            + ",vega:" + currentHighOptionMarketData.getVega() + ",theta:" + currentHighOptionMarketData.getTheta() + ",vol:" + currentHighOptionMarketData.getVolLv());


                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void ironCondor(String site, String symbol, double size, int days, double increment, double moreIncrement) {
        try {
            HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, symbol + "-USDT");
            if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
                messageService.sendStrategyMessage("netGrid获取不到价格", "netGrid获取不到价格,请手动检查");
                return;
            }
            Ticker apiTickerVO = swapTicker.getData().get(0);
            Double currentPrice = Double.valueOf(apiTickerVO.getLast());

            Double lowPrice = currentPrice * (1-increment);
            Double highPrice = currentPrice * (1+increment);

            Double moreLowPrice = currentPrice * (1-increment-moreIncrement);
            Double moreHighPrice = currentPrice * (1+increment+moreIncrement);

            log.info("当前价格{}-{}-{}", site, currentPrice, symbol);


            String strikeDate = getNextNDay(days);

            //卖出看涨期权 2份
            HttpResult<List<OptionMarketData>> optionsMarketDatas = publicDataAPIService.getOptionMarketData(site, symbol + "-USD", strikeDate);
            if ("0".equals(optionsMarketDatas.getCode()) && optionsMarketDatas.getData().size() > 0) {
                OptionMarketData currentLowOptionMarketData = null;
                Long currentLowStrikePrice = null;
                OptionMarketData currentHighOptionMarketData = null;
                Long currentHighStrikePrice = null;

                OptionMarketData currentMoreLowOptionMarketData = null;
                Long currentMoreLowStrikePrice = null;
                OptionMarketData currentMoreHighOptionMarketData = null;
                Long currentMoreHighStrikePrice = null;

                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);
                    if (strikePrice < lowPrice) {
                        if ("P".equals(optionInstArr[4])) {
                            if (currentLowOptionMarketData == null || strikePrice > currentLowStrikePrice) {
                                currentLowStrikePrice = strikePrice;
                                currentLowOptionMarketData = optionMarketData;
                            }
                        }
                    }
                    if (strikePrice > highPrice) {
                        if ("C".equals(optionInstArr[4])) {
                            if (currentHighOptionMarketData == null || strikePrice < currentHighStrikePrice) {
                                currentHighStrikePrice = strikePrice;
                                currentHighOptionMarketData = optionMarketData;
                            }
                        }
                    }

                }
                if (currentLowOptionMarketData == null || currentHighOptionMarketData == null) {
                    return;
                }

                for (OptionMarketData optionMarketData : optionsMarketDatas.getData()) {
                    String optionInstId = optionMarketData.getInstId();
                    String[] optionInstArr = optionInstId.split("-");
                    if (optionInstArr.length != 5 || !NumberUtils.isNumber(optionInstArr[3])) {
                        continue;
                    }
                    Long strikePrice = Long.parseLong(optionInstArr[3]);

                    if (strikePrice < moreLowPrice) {
                        if ("P".equals(optionInstArr[4]) && !optionInstId.equals(currentLowOptionMarketData.getInstId())) {
                            if (currentMoreLowOptionMarketData == null || strikePrice > currentMoreLowStrikePrice) {
                                currentMoreLowStrikePrice = strikePrice;
                                currentMoreLowOptionMarketData = optionMarketData;
                            }
                        }
                    }
                    if (strikePrice > highPrice) {
                        if ("C".equals(optionInstArr[4]) && !optionInstId.equals(currentHighOptionMarketData.getInstId())) {
                            if (currentMoreHighOptionMarketData == null || strikePrice < currentMoreHighStrikePrice) {
                                currentMoreHighStrikePrice = strikePrice;
                                currentMoreHighOptionMarketData = optionMarketData;
                            }
                        }
                    }
                }


                if (currentMoreLowStrikePrice != null && currentLowOptionMarketData != null
                        && currentHighOptionMarketData != null && currentMoreHighOptionMarketData != null) {
                    log.info("期权市场数据低卖{}:{}", currentLowOptionMarketData.getInstId(), JSON.toJSONString(currentLowOptionMarketData));
                    log.info("期权市场数据低买{}:{}", currentMoreLowOptionMarketData.getInstId(), JSON.toJSONString(currentMoreLowOptionMarketData));
                    log.info("期权市场数据高卖{}:{}", currentHighOptionMarketData.getInstId(), JSON.toJSONString(currentHighOptionMarketData));
                    log.info("期权市场数据高买{}:{}", currentMoreHighOptionMarketData.getInstId(), JSON.toJSONString(currentMoreHighOptionMarketData));

                    //获取期权的价格数据
                    HttpResult<List<OrderBook>> lowSellOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentLowOptionMarketData.getInstId(), null);
                    log.info("期权深度数据低卖{}:{}", currentLowOptionMarketData.getInstId(), JSON.toJSONString(lowSellOptionOrderBookDatas));
                    if (!"0".equals(lowSellOptionOrderBookDatas.getCode()) || lowSellOptionOrderBookDatas.getData().size() <= 0
                            || lowSellOptionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                        return;
                    }
                    String lowSellOptionBidPrice = lowSellOptionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                    log.info("低卖期权买一价{}:{}", currentLowOptionMarketData.getInstId(), lowSellOptionBidPrice);

                    HttpResult<List<OrderBook>> lowBuyOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentMoreLowOptionMarketData.getInstId(), null);
                    log.info("期权深度数据低买{}:{}", currentMoreLowOptionMarketData.getInstId(), JSON.toJSONString(lowBuyOptionOrderBookDatas));
                    if (!"0".equals(lowBuyOptionOrderBookDatas.getCode()) || lowBuyOptionOrderBookDatas.getData().size() <= 0
                            || lowBuyOptionOrderBookDatas.getData().get(0).getAsks().size() <= 0) {
                        return;
                    }
                    String lowBuyOptionAskPrice = lowBuyOptionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
                    log.info("低买期权卖一价{}:{}", currentMoreLowOptionMarketData.getInstId(), lowBuyOptionAskPrice);


                    HttpResult<List<OrderBook>> highSellOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentHighOptionMarketData.getInstId(), null);
                    log.info("期权深度数据低卖{}:{}", currentHighOptionMarketData.getInstId(), JSON.toJSONString(lowSellOptionOrderBookDatas));
                    if (!"0".equals(highSellOptionOrderBookDatas.getCode()) || highSellOptionOrderBookDatas.getData().size() <= 0
                            || highSellOptionOrderBookDatas.getData().get(0).getBids().size() <= 0) {
                        return;
                    }
                    String highSellOptionBidPrice = highSellOptionOrderBookDatas.getData().get(0).getBids().get(0)[0];
                    log.info("高卖期权买一价{}:{}", currentHighOptionMarketData.getInstId(), highSellOptionBidPrice);


                    HttpResult<List<OrderBook>> highBuyOptionOrderBookDatas = marketDataAPIService.getOrderBook(site, currentMoreHighOptionMarketData.getInstId(), null);
                    log.info("期权深度数据高买{}:{}", currentMoreHighOptionMarketData.getInstId(), JSON.toJSONString(highBuyOptionOrderBookDatas));
                    if (!"0".equals(highBuyOptionOrderBookDatas.getCode()) || highBuyOptionOrderBookDatas.getData().size() <= 0
                            || highBuyOptionOrderBookDatas.getData().get(0).getAsks().size() <= 0) {
                        return;
                    }
                    String highBuyOptionAskPrice = highBuyOptionOrderBookDatas.getData().get(0).getAsks().get(0)[0];
                    log.info("高买期权卖一价{}:{}", currentMoreHighOptionMarketData.getInstId(), highBuyOptionAskPrice);


                    String lowSellOptionInstId = currentLowOptionMarketData.getInstId();
                    PlaceOrder lowSellPlaceOrder = new PlaceOrder();
                    lowSellPlaceOrder.setInstId(lowSellOptionInstId);
                    lowSellPlaceOrder.setTdMode("cross");
                    lowSellPlaceOrder.setPx(new BigDecimal(lowSellOptionBidPrice).toPlainString());
                    lowSellPlaceOrder.setSz(String.valueOf(size));
                    lowSellPlaceOrder.setSide("sell");
                    lowSellPlaceOrder.setOrdType("fok");
                    lowSellPlaceOrder.setType("4");
                    OptionsOrder lowSellOptionsOrder = new OptionsOrder();
                    lowSellOptionsOrder.setInstrumentId(lowSellOptionInstId);
                    lowSellOptionsOrder.setCreateTime(new Date());
                    lowSellOptionsOrder.setStrategy("ironCondor");
                    lowSellOptionsOrder.setIsMock(Byte.valueOf("0"));
                    lowSellOptionsOrder.setType(Byte.valueOf(lowSellPlaceOrder.getType()));
                    lowSellOptionsOrder.setPrice(new BigDecimal(lowSellPlaceOrder.getPx()));
                    lowSellOptionsOrder.setSize(new BigDecimal(lowSellPlaceOrder.getSz()));

                    lowSellOptionsOrder.setSymbol(symbol);
                    lowSellOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    lowSellOptionsOrder.setDelta(new BigDecimal(currentLowOptionMarketData.getDelta()));
                    lowSellOptionsOrder.setGamma(new BigDecimal(currentLowOptionMarketData.getGamma()));
                    lowSellOptionsOrder.setVega(new BigDecimal(currentLowOptionMarketData.getVega()));
                    lowSellOptionsOrder.setTheta(new BigDecimal(currentLowOptionMarketData.getTheta()));
                    lowSellOptionsOrder.setVolLv(new BigDecimal(currentLowOptionMarketData.getVolLv()));

                    String lowSellOrderId = placeOrderAndUpdateStatus(site, lowSellPlaceOrder, lowSellOptionsOrder);
                    if (lowSellOrderId == null) {
                        return;
                    }
                    log.info("卖出看跌期权 {}-{},orderId:{}", lowSellOptionsOrder, JSON.toJSONString(lowSellPlaceOrder), lowSellOrderId);

                    messageService.sendStrategyMessage("ironCondor卖出看跌期权", "ironCondor卖出看跌期权:" + currentLowOptionMarketData.getInstId() +
                            ",price:" + lowSellOptionBidPrice + ",delta:" + currentLowOptionMarketData.getDelta() + ",gamma:" + currentLowOptionMarketData.getGamma()
                            + ",vega:" + currentLowOptionMarketData.getVega() + ",theta:" + currentLowOptionMarketData.getTheta() + ",vol:" + currentLowOptionMarketData.getVolLv());


                    String lowBuyOptionInstId = currentMoreLowOptionMarketData.getInstId();
                    PlaceOrder lowBuyPlaceOrder = new PlaceOrder();
                    lowBuyPlaceOrder.setInstId(lowBuyOptionInstId);
                    lowBuyPlaceOrder.setTdMode("isolated");
                    lowBuyPlaceOrder.setPx(new BigDecimal(lowBuyOptionAskPrice).toPlainString());
                    lowBuyPlaceOrder.setSz(String.valueOf(size));
                    lowBuyPlaceOrder.setSide("buy");
                    lowBuyPlaceOrder.setOrdType("fok");
                    lowBuyPlaceOrder.setType("2");
                    OptionsOrder lowBuyOptionsOrder = new OptionsOrder();
                    lowBuyOptionsOrder.setInstrumentId(lowBuyOptionInstId);
                    lowBuyOptionsOrder.setCreateTime(new Date());
                    lowBuyOptionsOrder.setStrategy("ironCondor");
                    lowBuyOptionsOrder.setIsMock(Byte.valueOf("0"));
                    lowBuyOptionsOrder.setType(Byte.valueOf(lowBuyPlaceOrder.getType()));
                    lowBuyOptionsOrder.setPrice(new BigDecimal(lowBuyPlaceOrder.getPx()));
                    lowBuyOptionsOrder.setSize(new BigDecimal(lowBuyPlaceOrder.getSz()));

                    lowBuyOptionsOrder.setSymbol(symbol);
                    lowBuyOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    lowBuyOptionsOrder.setDelta(new BigDecimal(currentMoreLowOptionMarketData.getDelta()));
                    lowBuyOptionsOrder.setGamma(new BigDecimal(currentMoreLowOptionMarketData.getGamma()));
                    lowBuyOptionsOrder.setVega(new BigDecimal(currentMoreLowOptionMarketData.getVega()));
                    lowBuyOptionsOrder.setTheta(new BigDecimal(currentMoreLowOptionMarketData.getTheta()));
                    lowBuyOptionsOrder.setVolLv(new BigDecimal(currentMoreLowOptionMarketData.getVolLv()));

                    String lowBuyOrderId = placeOrderAndUpdateStatus(site, lowBuyPlaceOrder, lowBuyOptionsOrder);
                    if (lowBuyOrderId == null) {
                        return;
                    }
                    log.info("买入看跌期权 {}-{},orderId:{}", lowBuyOptionInstId, JSON.toJSONString(lowBuyPlaceOrder), lowBuyOrderId);

                    messageService.sendStrategyMessage("ironCondor买入看跌期权", "ironCondor买入看跌期权:" + currentMoreLowOptionMarketData.getInstId() +
                            ",price:" + lowBuyOptionAskPrice + ",delta:" + currentMoreLowOptionMarketData.getDelta() + ",gamma:" + currentMoreLowOptionMarketData.getGamma()
                            + ",vega:" + currentMoreLowOptionMarketData.getVega() + ",theta:" + currentMoreLowOptionMarketData.getTheta() + ",vol:" + currentMoreLowOptionMarketData.getVolLv());


                    String highSellOptionInstId = currentHighOptionMarketData.getInstId();
                    PlaceOrder highSellPlaceOrder = new PlaceOrder();
                    highSellPlaceOrder.setInstId(highSellOptionInstId);
                    highSellPlaceOrder.setTdMode("cross");
                    highSellPlaceOrder.setPx(new BigDecimal(highSellOptionBidPrice).toPlainString());
                    highSellPlaceOrder.setSz(String.valueOf(size ));
                    highSellPlaceOrder.setSide("sell");
                    highSellPlaceOrder.setOrdType("fok");
                    highSellPlaceOrder.setType("2");
                    OptionsOrder highSelloptionsOrder = new OptionsOrder();
                    highSelloptionsOrder.setInstrumentId(highSellOptionInstId);
                    highSelloptionsOrder.setCreateTime(new Date());
                    highSelloptionsOrder.setStrategy("ironCondor");
                    highSelloptionsOrder.setIsMock(Byte.valueOf("0"));
                    highSelloptionsOrder.setType(Byte.valueOf(highSellPlaceOrder.getType()));
                    highSelloptionsOrder.setPrice(new BigDecimal(highSellPlaceOrder.getPx()));
                    highSelloptionsOrder.setSize(new BigDecimal(highSellPlaceOrder.getSz()));

                    highSelloptionsOrder.setSymbol(symbol);
                    highSelloptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    highSelloptionsOrder.setDelta(new BigDecimal(currentHighOptionMarketData.getDelta()));
                    highSelloptionsOrder.setGamma(new BigDecimal(currentHighOptionMarketData.getGamma()));
                    highSelloptionsOrder.setVega(new BigDecimal(currentHighOptionMarketData.getVega()));
                    highSelloptionsOrder.setTheta(new BigDecimal(currentHighOptionMarketData.getTheta()));
                    highSelloptionsOrder.setVolLv(new BigDecimal(currentHighOptionMarketData.getVolLv()));

                    String sellHighOrderId = placeOrderAndUpdateStatus(site, highSellPlaceOrder, highSelloptionsOrder);
                    if (sellHighOrderId == null) {
                        return;
                    }
                    log.info("卖出看涨期权 {}-{},orderId:{}", sellHighOrderId, JSON.toJSONString(highSellPlaceOrder), sellHighOrderId);

                    messageService.sendStrategyMessage("ironCondor卖出看涨期权", "ironCondor卖出看涨期权:" + currentHighOptionMarketData.getInstId() +
                            ",price:" + highSellOptionBidPrice + ",delta:" + currentHighOptionMarketData.getDelta() + ",gamma:" + currentHighOptionMarketData.getGamma()
                            + ",vega:" + currentHighOptionMarketData.getVega() + ",theta:" + currentHighOptionMarketData.getTheta() + ",vol:" + currentHighOptionMarketData.getVolLv());


                    String highOptionInstId = currentMoreHighOptionMarketData.getInstId();
                    PlaceOrder highPlaceOrder = new PlaceOrder();
                    highPlaceOrder.setInstId(highOptionInstId);
                    highPlaceOrder.setTdMode("isolated");
                    highPlaceOrder.setPx(new BigDecimal(highBuyOptionAskPrice).toPlainString());
                    highPlaceOrder.setSz(String.valueOf(size));
                    highPlaceOrder.setSide("buy");
                    highPlaceOrder.setOrdType("fok");
                    highPlaceOrder.setType("1");
                    OptionsOrder highOptionsOrder = new OptionsOrder();
                    highOptionsOrder.setInstrumentId(highOptionInstId);
                    highOptionsOrder.setCreateTime(new Date());
                    highOptionsOrder.setStrategy("ironCondor");
                    highOptionsOrder.setIsMock(Byte.valueOf("0"));
                    highOptionsOrder.setType(Byte.valueOf(highPlaceOrder.getType()));
                    highOptionsOrder.setPrice(new BigDecimal(highPlaceOrder.getPx()));
                    highOptionsOrder.setSize(new BigDecimal(highPlaceOrder.getSz()));

                    highOptionsOrder.setSymbol(symbol);
                    highOptionsOrder.setSwapPrice(new BigDecimal(apiTickerVO.getLast()));
                    highOptionsOrder.setDelta(new BigDecimal(currentMoreHighOptionMarketData.getDelta()));
                    highOptionsOrder.setGamma(new BigDecimal(currentMoreHighOptionMarketData.getGamma()));
                    highOptionsOrder.setVega(new BigDecimal(currentMoreHighOptionMarketData.getVega()));
                    highOptionsOrder.setTheta(new BigDecimal(currentMoreHighOptionMarketData.getTheta()));
                    highOptionsOrder.setVolLv(new BigDecimal(currentMoreHighOptionMarketData.getVolLv()));

                    String highOrderId = placeOrderAndUpdateStatus(site, highPlaceOrder, highOptionsOrder);
                    if (highOrderId == null) {
                        return;
                    }
                    log.info("买入看涨期权 {}-{},orderId:{}", highOptionInstId, JSON.toJSONString(highPlaceOrder), highOrderId);

                    messageService.sendStrategyMessage("ironCondor买入看涨期权", "ironCondor买入看涨期权:" + currentMoreHighOptionMarketData.getInstId() +
                            ",price:" + highBuyOptionAskPrice + ",delta:" + currentMoreHighOptionMarketData.getDelta() + ",gamma:" + currentMoreHighOptionMarketData.getGamma()
                            + ",vega:" + currentMoreHighOptionMarketData.getVega() + ",theta:" + currentMoreHighOptionMarketData.getTheta() + ",vol:" + currentMoreHighOptionMarketData.getVolLv());


                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
