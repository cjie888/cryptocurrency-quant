package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.CurrencyKlineDTO;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ElasticJobConf(name = "srsJob", cron = "20 */1 * * * ?",
        description = "srs", eventTraceRdbDataSource = "logDatasource")
@Slf4j(topic = "strategy")
public class SimpleRangeScalperJob implements SimpleJob {

    @Autowired
    private WeiXinMessageService weiXinMessageService;

    @Autowired
    private SwapMarketAPIService swapMarketAPIService;

    @Autowired
    private SwapOrderMapper swapOrderMapper;

    private Map<String,TimeSeries> timeSeriesMap = new HashMap<>();

    private Map<String,StrategyBuilder> strategyMap = new HashMap<>();

    private Map<String,TradingRecord> tradingRecordMap = new HashMap<>();


    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("start simple range scalper job");
        executeSrs("BTC-USD-SWAP");
        executeSrs("ETH-USD-SWAP");
        executeSrs("EOS-USD-SWAP");
        executeSrs("LTC-USD-SWAP");
        executeSrs("XRP-USD-SWAP");
        executeSrs("BCH-USD-SWAP");
        executeSrs("BSV-USD-SWAP");
    }

    public void executeSrs(String instrumentId) {

        try {
            TimeSeries timeSeries =  timeSeriesMap.get(instrumentId);
            StrategyBuilder strategy = strategyMap.get(instrumentId);
            TradingRecord tradingRecord = tradingRecordMap.get(instrumentId);
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
                strategy = new SimpleRangeScalperStrategy(timeSeries);
                // Initializing the trading history
                tradingRecord = new BaseTradingRecord();

                timeSeriesMap.put(instrumentId, timeSeries);
                strategyMap.put(instrumentId, strategy);
                tradingRecordMap.put(instrumentId, tradingRecord);

            } else {
                String[] apiKlineVO = apiKlineVOs.get(0);

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
            Strategy longStrategy = strategy.buildStrategy(Order.OrderType.BUY);
            Strategy shortStrategy = strategy.buildStrategy(Order.OrderType.SELL);
            //log.info("Current bar is {}", JSON.toJSONString(timeSeries.getBarData()));
            int endIndex = timeSeries.getEndIndex();
            Bar newBar = timeSeries.getLastBar();


            if (longStrategy.shouldEnter(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("开多").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("开多-srs",  stringBuilder.toString());
                // Our strategy should enter
                log.info("Simple Range Scalper Strategy should ENTER on {}, time:{}" , endIndex, newBar.getBeginTime());
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    log.info("Entered on " + entry.getIndex()
                            + " (price=" + entry.getPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
                SwapOrder swapOrder = SwapOrder.builder()
                        .createTime(new Date())
                        .instrumentId(instrumentId)
                        .isMock(Byte.valueOf("1"))
                        .size(new BigDecimal(100))
                        .price(BigDecimal.valueOf(newBar.getClosePrice().doubleValue()))
                        .strategy("SimpleRangeScalper")
                        .type(Byte.valueOf("1"))
                        .build();
                swapOrderMapper.insert(swapOrder);
            } else if (longStrategy.shouldExit(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("平多").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("平多-srs",  stringBuilder.toString());
                // Our strategy should exit
                log.info("Strategy should EXIT on {}, time:{}" , endIndex, newBar.getBeginTime());

                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (exited) {
                    Order exit = tradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex()
                            + " (price=" + exit.getPrice().doubleValue()
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

            if (shortStrategy.shouldEnter(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("开空").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("开空-srs",  stringBuilder.toString());
                // Our strategy should enter
                log.info("Simple Range Scalper Strategy should ENTER on {}, time:{}" , endIndex, newBar.getBeginTime());
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    log.info("Entered on " + entry.getIndex()
                            + " (price=" + entry.getPrice().doubleValue()
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
            } else if (shortStrategy.shouldExit(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("平空").append(" ").append(instrumentId).append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("平空-srs",  stringBuilder.toString());
                // Our strategy should exit
                log.info("Strategy should EXIT on {}, time:{}" , endIndex, newBar.getBeginTime());

                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (exited) {
                    Order exit = tradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex()
                            + " (price=" + exit.getPrice().doubleValue()
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
