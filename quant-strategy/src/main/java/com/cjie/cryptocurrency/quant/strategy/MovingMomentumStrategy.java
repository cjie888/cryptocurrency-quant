
package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.CurrencyKlineDTO;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Moving momentum strategy.
 * </p>
 * @see <a href="http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum">
 *     http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum</a>
 */
@ElasticJobConf(name = "movingMomentumJob", cron = "30 */1 * * * ?",
        description = "移动动量策略", eventTraceRdbDataSource = "logDatasource")
@Slf4j
public class MovingMomentumStrategy implements SimpleJob {

    private static final org.slf4j.Logger strategyLog = org.slf4j.LoggerFactory.getLogger("strategy");


    private TimeSeries timeSeries;

    private Strategy strategy;

    private TradingRecord tradingRecord;

    @Autowired
    private SpotProductAPIService spotProductAPIService;

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
        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
        EMAIndicator longEma = new EMAIndicator(closePrice, 26);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, 14);

        MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 18);
        
        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, 20)) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2
        
        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK,20)) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2
        
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

            log.info("Current bar is {}", JSON.toJSONString(timeSeries.getBarData()));


            int endIndex = timeSeries.getEndIndex();
            Bar newBar = timeSeries.getLastBar();
            if (strategy.shouldEnter(endIndex)) {
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
