package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.num.PrecisionNum;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j(topic = "strategy")
public abstract class BaseSwapStrategyJob  {

    private Map<String,TimeSeries> timeSeriesMap = new HashMap<>();

    private Map<String, StrategyBuilder> strategyMap = new HashMap<>();

    private Map<String,TradingRecord> longTradingRecordMap = new HashMap<>();

    private Map<String,TradingRecord> shortTradingRecordMap = new HashMap<>();

    @Autowired
    private WeiXinMessageService weiXinMessageService;

    @Autowired
    private SwapMarketAPIService swapMarketAPIService;

    @Autowired
    private SwapOrderMapper swapOrderMapper;

    public abstract StrategyBuilder buildStrategy(TimeSeries timeSeries, boolean isMock);


    public void executeStrategy(String instrumentId, boolean isMock) {

        StrategyBuilder strategy = strategyMap.get(instrumentId);
        try {
            TimeSeries timeSeries =  timeSeriesMap.get(instrumentId);

            TradingRecord longTradingRecord = longTradingRecordMap.get(instrumentId);
            TradingRecord shortTradingRecord = shortTradingRecordMap.get(instrumentId);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String kline = swapMarketAPIService.getCandlesApi(instrumentId, null, null, "60");
            List<String[]> apiKlineVOs = JSON.parseObject(kline, new TypeReference<List<String[]>>(){});
            // Getting the time series
            if (timeSeries == null) {
                timeSeries = new BaseTimeSeries();
                timeSeries.setMaximumBarCount(1000);
                if (CollectionUtils.isNotEmpty(apiKlineVOs)) {
                    for (int i = apiKlineVOs.size() -1; i > 0; i--) {
                        String[] apiKlineVO = apiKlineVOs.get(i);
                        ZonedDateTime beginTime = ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(dateFormat.parse(apiKlineVO[0]).getTime()), ZoneId.systemDefault());
                        double open = Double.valueOf(apiKlineVO[1]);
                        double high = Double.valueOf(apiKlineVO[2]);
                        double close = Double.valueOf(apiKlineVO[4]);
                        double low = Double.valueOf(apiKlineVO[3]);
                        double volume = Double.valueOf(apiKlineVO[5]);

                        timeSeries.addBar(beginTime, open, high,
                                low, close, volume);

                    }
                }
                strategy = buildStrategy(timeSeries, isMock);
                // Initializing the trading history
                longTradingRecord = new BaseTradingRecord();
                shortTradingRecord = new BaseTradingRecord();

                timeSeriesMap.put(instrumentId, timeSeries);
                strategyMap.put(instrumentId, strategy);
                longTradingRecordMap.put(instrumentId, longTradingRecord);
                shortTradingRecordMap.put(instrumentId, shortTradingRecord);


            } else {

                if (CollectionUtils.isNotEmpty(apiKlineVOs)) {
                    ZonedDateTime seriesBeginTime = timeSeries.getLastBar().getBeginTime();
                    String[] apiKlineVO = apiKlineVOs.get(1);
                    ZonedDateTime beginTime = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(dateFormat.parse(apiKlineVO[0]).getTime()), ZoneId.systemDefault());
                    if (!beginTime.isAfter(seriesBeginTime)) {
                        return;
                    }
                    double open = Double.valueOf(apiKlineVO[1]);
                    double high = Double.valueOf(apiKlineVO[2]);
                    double close = Double.valueOf(apiKlineVO[4]);
                    double low = Double.valueOf(apiKlineVO[3]);
                    double volume = Double.valueOf(apiKlineVO[5]);
                    Bar bar = new BaseBar(beginTime, PrecisionNum.valueOf(open), PrecisionNum.valueOf(high),
                            PrecisionNum.valueOf(low), PrecisionNum.valueOf(close), PrecisionNum.valueOf(volume),
                            PrecisionNum.valueOf(0));
                    timeSeries.addBar(bar);

                }

            }
            Strategy longStrategy = strategy.buildStrategy(Order.OrderType.BUY);
            Strategy shortStrategy = strategy.buildStrategy(Order.OrderType.SELL);
            //log.info("Current bar is {}", JSON.toJSONString(timeSeries.getBarData()));
            int endIndex = timeSeries.getEndIndex();
            Bar newBar = timeSeries.getLastBar();

            if (instrumentId.contains("BTC") && strategy instanceof SimpleRangeScalperStrategy) {
                double lowerBollingeBand =  ((SimpleRangeScalperStrategy)strategy).getLowerBollingeBand().getValue(endIndex).doubleValue();
                double middleBollingeBand =  ((SimpleRangeScalperStrategy)strategy).getMiddleBollingerBand().getValue(endIndex).doubleValue();
                double upBollingeBand =  ((SimpleRangeScalperStrategy)strategy).getUpperBollingerBand().getValue(endIndex).doubleValue();
                log.info("kline date:" + JSON.toJSONString(newBar.getBeginTime()) + "price:" + newBar.getClosePrice() +
                        " middleBollingeBand:" + middleBollingeBand + " lowerBollingeBand:" + lowerBollingeBand + " upBollingeBand" + upBollingeBand);
//                StringBuilder sb = new StringBuilder();
//                for (Bar bar :  timeSeries.getBarData()) {
//                    sb.append("time:" +bar.getSimpleDateName() + ",close:"+bar.getClosePrice());
//                }
//                log.info("time series:{}", sb.toString());
            }

            if (instrumentId.contains("BTC") && strategy instanceof MmaCrossStrategy) {
                double shortMMa =  ((MmaCrossStrategy)strategy).getShortMma().getValue(endIndex).doubleValue();
                double longMMa =  ((MmaCrossStrategy)strategy).getLongMma().getValue(endIndex).doubleValue();
                log.info("kline date:" + JSON.toJSONString(newBar.getBeginTime()) + "price:" + newBar.getClosePrice() +
                        " shortMMa:" + shortMMa + " longMMa:" + longMMa);
            }

            if (longStrategy.shouldEnter(endIndex, longTradingRecord)) {
                enter(instrumentId, longTradingRecord, Order.OrderType.BUY);
            } else if (longStrategy.shouldExit(endIndex, longTradingRecord)) {
                exit(instrumentId, longTradingRecord, Order.OrderType.BUY);
            }

            if (shortStrategy.shouldEnter(endIndex)) {
                enter(instrumentId, shortTradingRecord, Order.OrderType.SELL);
            } else if (shortStrategy.shouldExit(endIndex, shortTradingRecord)) {
                exit(instrumentId, shortTradingRecord, Order.OrderType.SELL);
            }
        } catch (Exception e) {
            log.error("Strategy {} error", strategy.getName(), e);
        }
    }

    private void enter(String instrumentId, TradingRecord tradingRecord, Order.OrderType orderType) {
        TimeSeries timeSeries = timeSeriesMap.get(instrumentId);
        int endIndex = timeSeries.getEndIndex();
        Bar newBar = timeSeries.getLastBar();
        boolean shouldEnter = tradingRecord.getCurrentTrade().isNew() || tradingRecord.getCurrentTrade().isClosed();
        StrategyBuilder strategyBuilder =  strategyMap.get(instrumentId);
        String operation;
        String type;
        if (orderType == Order.OrderType.BUY) {
            operation = "开多";
            type = "1";
        } else {
            operation = "开空";
            type = "2";
        }
        // Our strategy should enter
        log.info("Strategy {} {} should ENTER on {}, time:{}, price:{}" , strategyBuilder.getName(), instrumentId, endIndex, newBar.getBeginTime(), newBar.getClosePrice());
        if (shouldEnter) {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(operation).append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
//                    .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
//            weiXinMessageService.sendMessage(operation + strategyBuilder.getName() + instrumentId,  stringBuilder.toString());
            boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
            if (entered) {
                Order entry = tradingRecord.getLastEntry();
                log.info("Entered on " + entry.getIndex()
                        + "(type = " + entry.getType().name()
                        + ", instrumentId=" + instrumentId
                        + ", time=" + timeSeries.getBar(entry.getIndex()).getBeginTime()
                        + ", price=" + entry.getPrice().doubleValue()
                        + ", amount=" + entry.getAmount().doubleValue() + ")");
            }
        }

        createOrder(instrumentId, type, BigDecimal.valueOf(newBar.getClosePrice().doubleValue()), new BigDecimal(100));
    }

    private  void exit(String instrumentId, TradingRecord tradingRecord, Order.OrderType orderType) {
        TimeSeries timeSeries = timeSeriesMap.get(instrumentId);
        int endIndex = timeSeries.getEndIndex();
        Bar newBar = timeSeries.getLastBar();
        boolean shouldExit = tradingRecord.getCurrentTrade().isOpened();
        StrategyBuilder strategyBuilder =  strategyMap.get(instrumentId);
        String operation;
        String type;
        if (orderType == Order.OrderType.BUY) {
            operation = "平多";
            type = "3";
        } else {
            operation = "开空";
            type = "4";
        }

        // Our strategy should exit
        log.info("Strategy {} {} should EXIT on {}, time:{}, price:{}" , strategyBuilder.getName(), instrumentId, endIndex, newBar.getBeginTime(), newBar.getClosePrice());
        if (shouldExit) {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(operation).append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
//                    .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
//            weiXinMessageService.sendMessage(operation + strategyBuilder.getName() + instrumentId,  stringBuilder.toString());
            boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
            if (exited) {
                Order exit = tradingRecord.getLastExit();
                log.info("Exited on " + exit.getIndex()
                        + "(type = " + exit.getType().name()
                        + ", instrumentId=" + instrumentId
                        + ", time=" + timeSeries.getBar(exit.getIndex()).getBeginTime()
                        + ", price=" + exit.getPrice().doubleValue()
                        + ", amount=" + exit.getAmount().doubleValue() + ")");
            }
        }
        createOrder(instrumentId, type, BigDecimal.valueOf(newBar.getClosePrice().doubleValue()), new BigDecimal(100));
    }

    private void createOrder(String instrumentId, String type, BigDecimal price, BigDecimal size) {
        SwapOrder swapOrder = SwapOrder.builder()
                .createTime(new Date())
                .instrumentId(instrumentId)
                .isMock(strategyMap.get(instrumentId).isMock() ? Byte.valueOf("1") : Byte.valueOf("0"))
                .size(size)
                .price(price)
                .strategy(strategyMap.get(instrumentId).getName())
                .type(Byte.valueOf(type))
                .build();
        swapOrderMapper.insert(swapOrder);
    }
}
