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

    public abstract StrategyBuilder buildStrategy(TimeSeries timeSeries);


    public void executeStrategy(String instrumentId) {

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
                strategy = buildStrategy(timeSeries);
                // Initializing the trading history
                longTradingRecord = new BaseTradingRecord();
                shortTradingRecord = new BaseTradingRecord();

                timeSeriesMap.put(instrumentId, timeSeries);
                strategyMap.put(instrumentId, strategy);
                longTradingRecordMap.put(instrumentId, longTradingRecord);
                shortTradingRecordMap.put(instrumentId, shortTradingRecord);


            } else {

                if (CollectionUtils.isNotEmpty(apiKlineVOs)) {
                    String[] apiKlineVO = apiKlineVOs.get(1);
                    ZonedDateTime beginTime = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(dateFormat.parse(apiKlineVO[0]).getTime()), ZoneId.systemDefault());
                    double open = Double.valueOf(apiKlineVO[1]);
                    double high = Double.valueOf(apiKlineVO[2]);
                    double close = Double.valueOf(apiKlineVO[4]);
                    double low = Double.valueOf(apiKlineVO[3]);
                    double volume = Double.valueOf(apiKlineVO[5]);
                    Bar bar = new BaseBar(beginTime, PrecisionNum.valueOf(open), PrecisionNum.valueOf(high),
                            PrecisionNum.valueOf(low), PrecisionNum.valueOf(close), PrecisionNum.valueOf(volume),
                            PrecisionNum.valueOf(0));
                    timeSeries.addBar(bar, true);

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
            }

            if (instrumentId.contains("BTC") && strategy instanceof MmaCrossStrategy) {
                double shortMMa =  ((MmaCrossStrategy)strategy).getShortMma().getValue(endIndex).doubleValue();
                double longMMa =  ((MmaCrossStrategy)strategy).getLongMma().getValue(endIndex).doubleValue();
                log.info("kline date:" + JSON.toJSONString(newBar.getBeginTime()) + "price:" + newBar.getClosePrice() +
                        " shortMMa:" + shortMMa + " longMMa:" + longMMa);
            }

            if ((longTradingRecord.getCurrentTrade().isNew() || longTradingRecord.getCurrentTrade().isClosed() )&&longStrategy.shouldEnter(endIndex, longTradingRecord)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("开多").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("开多-" + strategy.getName()  + instrumentId,  stringBuilder.toString());
                // Our strategy should enter
                log.info("Strategy {} {} should ENTER on {}, time:{}" , strategy, instrumentId, endIndex, newBar.getBeginTime());
                boolean entered = longTradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (entered) {
                    Order entry = longTradingRecord.getLastEntry();
                    log.info("Entered on " + entry.getIndex()
                            + "(type = " + entry.getType().name()
                            + ", instrumentId=" +  instrumentId
                            + ", time=" + timeSeries.getBar(entry.getIndex()).getBeginTime()
                            + ", price=" + entry.getPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
                createOrder(instrumentId, "1", BigDecimal.valueOf(newBar.getClosePrice().doubleValue()), new BigDecimal(100));

            } else if (longTradingRecord.getCurrentTrade().isOpened() && longStrategy.shouldExit(endIndex, longTradingRecord)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("平多").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("平多" + strategy.getName() + instrumentId,  stringBuilder.toString());
                // Our strategy should exit
                log.info( "Strategy {} {} should EXIT on {}, time:{}" , strategy.getName(), instrumentId, endIndex, newBar.getBeginTime());

                boolean exited = longTradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (exited) {
                    Order exit = longTradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex()
                            + "(type = " + exit.getType().name()
                            + ", instrumentId=" +  instrumentId
                            + ", time=" + timeSeries.getBar(exit.getIndex()).getBeginTime()
                            + ", price=" + exit.getPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
                createOrder(instrumentId, "3", BigDecimal.valueOf(newBar.getClosePrice().doubleValue()), new BigDecimal(100));

            }

            if ((shortTradingRecord.getCurrentTrade().isNew() || shortTradingRecord.getCurrentTrade().isClosed()) && shortStrategy.shouldEnter(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("开空").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("开空" + strategy.getName() + instrumentId,  stringBuilder.toString());
                // Our strategy should enter
                log.info("Strategy {} {} should ENTER on {}, time:{}" , shortStrategy.getName(), instrumentId, endIndex, newBar.getBeginTime());
                boolean entered = shortTradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (entered) {
                    Order entry = shortTradingRecord.getLastEntry();
                    log.info("Entered on " + entry.getIndex()
                            + "(type = " + entry.getType().name()
                            + ", instrumentId=" +  instrumentId
                            + ", time=" + timeSeries.getBar(entry.getIndex()).getBeginTime()
                            + ", price=" + entry.getPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
                createOrder(instrumentId, "2", BigDecimal.valueOf(newBar.getClosePrice().doubleValue()), new BigDecimal(100));
            } else if (shortTradingRecord.getCurrentTrade().isOpened() && shortStrategy.shouldExit(endIndex, shortTradingRecord)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("平空").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("平空" + strategy.getName() + instrumentId,  stringBuilder.toString());
                // Our strategy should exit
                log.info("Strategy {} {} should EXIT on {}, time:{}" , strategy.getName(), instrumentId, endIndex, newBar.getBeginTime());

                boolean exited = shortTradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (exited) {
                    Order exit = shortTradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex()
                            + "(type = " + exit.getType().name()
                            + ", instrumentId=" +  instrumentId
                            + ", time=" + timeSeries.getBar(exit.getIndex()).getBeginTime()
                            + ", price=" + exit.getPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
                createOrder(instrumentId, "4", BigDecimal.valueOf(newBar.getClosePrice().doubleValue()), new BigDecimal(100));
            }
        } catch (Exception e) {
            log.error("Strategy {} error", strategy.getName(), e);
        }
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
