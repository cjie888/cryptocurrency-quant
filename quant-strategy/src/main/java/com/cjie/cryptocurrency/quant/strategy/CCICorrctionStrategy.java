
package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.CurrencyKlineDTO;
import com.cjie.cryptocurrency.quant.api.okex.service.spot.SpotProductAPIService;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
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
@ElasticJobConf(name = "cciJob", cron = "40 */1 * * * ?",
        description = "cci", eventTraceRdbDataSource = "logDatasource")
@Slf4j
public class CCICorrctionStrategy implements SimpleJob {

    private static final org.slf4j.Logger strategyLog = org.slf4j.LoggerFactory.getLogger("strategy");


    private TimeSeries timeSeries;

    private Strategy strategy;

    private TradingRecord tradingRecord;

    private CCIIndicator longCci;

    private CCIIndicator shortCci;

    @Autowired
    private SpotProductAPIService spotProductAPIService;

    /**
     * @param series a time series
     * @return a CCI correction strategy
     */
    public Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        longCci = new CCIIndicator(series, 200);
        shortCci = new CCIIndicator(series, 5);
        Num plus100 = series.numOf(100);
        Num minus100 = series.numOf(-100);

        Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
                .and(new UnderIndicatorRule(shortCci, minus100)); // Signal

        Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
                .and(new OverIndicatorRule(shortCci, plus100)); // Signal

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        strategy.setUnstablePeriod(5);
        return strategy;
    }


    @Override
    public void execute(ShardingContext shardingContext) {
        try {
            List<CurrencyKlineDTO> currencyKlineDTOS = spotProductAPIService.getCandles("okex", "btc-usdt", 60, null, null);
            //log.info(JSON.toJSONString(currencyKlineDTOS));
            // Getting the time series
            if (timeSeries == null) {
                timeSeries = new BaseTimeSeries();

                for (int i = currencyKlineDTOS.size() -1; i >= 0; i--) {
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
                timeSeries.addBar(beginTime, currencyKlineDTO.getOpen(), currencyKlineDTO.getHigh(), currencyKlineDTO.getLow(), currencyKlineDTO.getClose());

            }
            log.info("Current bar is {}", JSON.toJSONString(timeSeries.getBarData()));
            int endIndex = timeSeries.getEndIndex();
            Bar newBar = timeSeries.getLastBar();

            log.info("cci:{}", JSON.toJSONString(shortCci));
            strategyLog.info("Current cci time:{}, short:{}, long:{}", newBar.getBeginTime(),
                    shortCci.getValue(endIndex-1).doubleValue(),
                    longCci.getValue(endIndex -1).doubleValue());

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
            log.error("Cci correction strategy error", e);
        }
    }
}
