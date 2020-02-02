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

        try {
            TimeSeries timeSeries =  timeSeriesMap.get(instrumentId);
            StrategyBuilder strategy = strategyMap.get(instrumentId);

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
                    for (int i = apiKlineVOs.size() -1; i >= 0; i--) {
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
                    for (int i = Math.min(apiKlineVOs.size() - 1, 4); i >= 0; i--) {
                        String[] apiKlineVO = apiKlineVOs.get(i);
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

            }
            Strategy longStrategy = strategy.buildStrategy(Order.OrderType.BUY);
            Strategy shortStrategy = strategy.buildStrategy(Order.OrderType.SELL);
            //log.info("Current bar is {}", JSON.toJSONString(timeSeries.getBarData()));
            int endIndex = timeSeries.getEndIndex();
            Bar newBar = timeSeries.getLastBar();


            if ((longTradingRecord.getCurrentTrade().isNew() || longTradingRecord.getCurrentTrade().isClosed() )&&longStrategy.shouldEnter(endIndex, longTradingRecord)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("开多").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("开多-srs-" + instrumentId,  stringBuilder.toString());
                // Our strategy should enter
                log.info("Simple Range Scalper Strategy {} should ENTER on {}, time:{}" , instrumentId, endIndex, newBar.getBeginTime());
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
                SwapOrder swapOrder = SwapOrder.builder()
                        .createTime(new Date())
                        .instrumentId(instrumentId)
                        .isMock(Byte.valueOf("1"))
                        .size(new BigDecimal(100))
                        .price(BigDecimal.valueOf(newBar.getClosePrice().doubleValue()))
                        .strategy(strategy.getName())
                        .type(Byte.valueOf("1"))
                        .build();
                swapOrderMapper.insert(swapOrder);
            } else if (longTradingRecord.getCurrentTrade().isOpened() && longStrategy.shouldExit(endIndex, longTradingRecord)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("平多").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("平多-srs" + instrumentId,  stringBuilder.toString());
                // Our strategy should exit
                log.info("Simple Range Scalper Strategy {} should EXIT on {}, time:{}" , instrumentId, endIndex, newBar.getBeginTime());

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
                SwapOrder swapOrder = SwapOrder.builder()
                        .createTime(new Date())
                        .instrumentId(instrumentId)
                        .isMock(Byte.valueOf("1"))
                        .size(new BigDecimal(100))
                        .price(BigDecimal.valueOf(newBar.getClosePrice().doubleValue()))
                        .strategy("SimpleRangeScalper")
                        .type(Byte.valueOf("3"))
                        .build();
                swapOrderMapper.insert(swapOrder);
            }

            if ((shortTradingRecord.getCurrentTrade().isNew() || shortTradingRecord.getCurrentTrade().isClosed()) && shortStrategy.shouldEnter(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("开空").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("开空-srs" + instrumentId,  stringBuilder.toString());
                // Our strategy should enter
                log.info("Simple Range Scalper Strategy {} should ENTER on {}, time:{}" , instrumentId, endIndex, newBar.getBeginTime());
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
                SwapOrder swapOrder = SwapOrder.builder()
                        .createTime(new Date())
                        .instrumentId(instrumentId)
                        .isMock(Byte.valueOf("1"))
                        .size(new BigDecimal(100))
                        .price(BigDecimal.valueOf(newBar.getClosePrice().doubleValue()))
                        .strategy("SimpleRangeScalper")
                        .type(Byte.valueOf("2"))
                        .build();
                swapOrderMapper.insert(swapOrder);
            } else if (shortTradingRecord.getCurrentTrade().isOpened() && shortStrategy.shouldExit(endIndex, shortTradingRecord)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("平空").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("平空-srs" + instrumentId,  stringBuilder.toString());
                // Our strategy should exit
                log.info("Simple Range Scalper Strategy {} should EXIT on {}, time:{}" , instrumentId, endIndex, newBar.getBeginTime());

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
                SwapOrder swapOrder = SwapOrder.builder()
                        .createTime(new Date())
                        .instrumentId(instrumentId)
                        .isMock(Byte.valueOf("1"))
                        .size(new BigDecimal(100))
                        .price(BigDecimal.valueOf(newBar.getClosePrice().doubleValue()))
                        .strategy("SimpleRangeScalper")
                        .type(Byte.valueOf("4"))
                        .build();
                swapOrderMapper.insert(swapOrder);
            }
        } catch (Exception e) {
            log.error("Simple range scalper strategy error", e);
        }
    }
}
