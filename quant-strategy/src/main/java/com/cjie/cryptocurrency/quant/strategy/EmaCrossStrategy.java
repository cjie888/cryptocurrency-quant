
package com.cjie.cryptocurrency.quant.strategy;

import com.cjie.cryptocurrency.quant.api.okex.service.spot.CurrencyKlineDTO;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;


//@ElasticJobConf(name = "emaCrossJob", cron = "30 */1 * * * ?",
 //      description = "ema上穿下穿策略", eventTraceRdbDataSource = "logDatasource")
@Slf4j
public class EmaCrossStrategy implements SimpleJob {

    private static final org.slf4j.Logger strategyLog = org.slf4j.LoggerFactory.getLogger("strategy");


    private TimeSeries timeSeries;

    private Strategy strategy;

    private TradingRecord tradingRecord;

    @Autowired
    private SpotProductAPIService spotProductAPIService;

    @Autowired
    private WeiXinMessageService weiXinMessageService;

    /**
     * @param series a time series
     * @return a moving momentum strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, 7);
        EMAIndicator longEma = new EMAIndicator(closePrice, 30);


        // Entry rule
        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma);
        
        // Exit rule
        Rule exitRule = new CrossedDownIndicatorRule(shortEma, longEma); // Trend

        return new BaseStrategy(entryRule, exitRule);
    }


    @Override
    public void execute(ShardingContext shardingContext) {
        try {
            List<CurrencyKlineDTO> currencyKlineDTOS = spotProductAPIService.getCandles("okex", "btc-usdt", 60, null, null);
            //log.info(JSON.toJSONString(currencyKlineDTOS));
            // Getting the time series
            if (timeSeries == null) {
                timeSeries = new BaseTimeSeries();
                timeSeries.setMaximumBarCount(1000);

                for (int i = currencyKlineDTOS.size() - 1; i >= 0; i--) {
                    CurrencyKlineDTO currencyKlineDTO = currencyKlineDTOS.get(i);
                    ZonedDateTime beginTime = ZonedDateTime.ofInstant(
                            Instant.ofEpochMilli(Long.parseLong(currencyKlineDTO.getTime())), ZoneId.systemDefault());

                    timeSeries.addBar(beginTime, currencyKlineDTO.getOpen(), currencyKlineDTO.getHigh(), currencyKlineDTO.getLow(), currencyKlineDTO.getClose());
                }
                // Building the trading strategy
                strategy = buildStrategy(timeSeries);

                // Initializing the trading history
                tradingRecord = new BaseTradingRecord();

            } else {
                CurrencyKlineDTO currencyKlineDTO = currencyKlineDTOS.get(0);

                ZonedDateTime beginTime = ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(Long.parseLong(currencyKlineDTO.getTime())), ZoneId.systemDefault());
                timeSeries.addBar(beginTime, currencyKlineDTO.getOpen(), currencyKlineDTO.getHigh(), currencyKlineDTO.getLow(),
                        currencyKlineDTO.getClose(), currencyKlineDTO.getVolume());

            }

            //log.info("Current bar is {}", JSON.toJSONString(timeSeries.getBarData()));


            int endIndex = timeSeries.getEndIndex();
            Bar newBar = timeSeries.getLastBar();
            if (strategy.shouldEnter(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Buy").append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("buy-ema",  stringBuilder.toString());
                // Our strategy should enter
                strategyLog.info("Strategy should ENTER on {}, time:{}" , endIndex, newBar.getBeginTime());
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    log.info("Entered on " + entry.getIndex()
                            + " (price=" + entry.getPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
            } else if (strategy.shouldExit(endIndex)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Sell").append(" ").append(newBar.getBeginTime())
                        .append(" ").append(newBar.getClosePrice()).append("\r\n\n");
                weiXinMessageService.sendMessage("sell-ema",  stringBuilder.toString());
                // Our strategy should exit
                strategyLog.info("Strategy should EXIT on {}, time:{}" , endIndex, newBar.getBeginTime());

                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(10));
                if (exited) {
                    Order exit = tradingRecord.getLastExit();
                    log.info("Exited on " + exit.getIndex()
                            + " (price=" + exit.getPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
            }
        }catch (Exception e) {
            log.error("Moving momentum strategy error", e);
        }
    }
}
