package com.cjie.cryptocurrency.quant.backtest;


import com.cjie.cryptocurrency.quant.strategy.CCICorrctionStrategy;
import com.cjie.cryptocurrency.quant.strategy.KestnerCrossStrategy;
import com.cjie.cryptocurrency.quant.strategy.SimpleRangeScalperStrategy;
import org.ta4j.core.BaseBarSeries;

import java.math.BigDecimal;
/**
 * Example how you could use and test strategies with example data of this repository
 */
public class Run {


    public static void main(String[] args) {

        // load data as TimeSeries
        SwapLoader loader = new SwapLoader();
        BaseBarSeries series = loader.getMinuteTimeSeries(5, "/Users/hucj/cryptocurrency-quant/quant-strategy/src/main/data/eth_5minutes.csv", "btc");

//        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
//        MMAIndicator mma7Indicator = new MMAIndicator(closePriceIndicator,7);
//        MMAIndicator mma30Indicator = new MMAIndicator(closePriceIndicator,30);
//
//        for (int i = 0;i < series.getBarCount(); i++) {
//            Bar bar = series.getBar(i);
//            System.out.println(Date.from(bar.getBeginTime().toInstant()) + " ma7:" + mma7Indicator.getValue(i) + " ma30:" + mma30Indicator.getValue(i));
//
//        }

        // create and initialize a strategy

        SimpleRangeScalperStrategy simpleRangeScalper = new SimpleRangeScalperStrategy(series, true, false);
        simpleRangeScalper.initStrategy(series);

        // run strategy on time series and analyse results
        StrategyAnalyser analyser = new StrategyAnalyser();
//        analyser.printAllResults(simpleRangeScalper);

        // change parameters of the strategy and run again
        simpleRangeScalper.setParams(20, BigDecimal.valueOf(1));
        analyser.printAllResults(simpleRangeScalper);
    }
}
