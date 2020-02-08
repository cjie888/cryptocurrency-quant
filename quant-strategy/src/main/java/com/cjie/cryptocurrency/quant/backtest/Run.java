package com.cjie.cryptocurrency.quant.backtest;


import com.cjie.cryptocurrency.quant.indicator.MMAIndicator;
import com.cjie.cryptocurrency.quant.strategy.MmaCrossStrategy;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Example how you could use and test strategies with example data of this repository
 */
public class Run {


    public static void main(String[] args) {

        // load data as TimeSeries
        SwapLoader loader = new SwapLoader();
        TimeSeries series = loader.getMinuteTimeSeries("/Users/hucj/cryptocurrency-quant/quant-strategy/src/main/data/btc_5minutes.csv", "btc");

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        MMAIndicator mma7Indicator = new MMAIndicator(closePriceIndicator,7);
        MMAIndicator mma30Indicator = new MMAIndicator(closePriceIndicator,30);

        for (int i = 0;i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            System.out.println(Date.from(bar.getBeginTime().toInstant()) + " ma7:" + mma7Indicator.getValue(i) + " ma30:" + mma30Indicator.getValue(i));

        }

        // create and initialize a strategy

        MmaCrossStrategy simpleRangeScalper = new MmaCrossStrategy(series, true, false);
        simpleRangeScalper.initStrategy(series);

        // run strategy on time series and analyse results
        StrategyAnalyser analyser = new StrategyAnalyser();
        analyser.printAllResults(simpleRangeScalper);

        // change parameters of the strategy and run again
        simpleRangeScalper.setParams(9, 33, BigDecimal.valueOf(0.5));
        analyser.printAllResults(simpleRangeScalper);
    }
}
