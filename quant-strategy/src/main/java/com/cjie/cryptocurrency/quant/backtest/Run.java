package com.cjie.cryptocurrency.quant.backtest;


import com.cjie.cryptocurrency.quant.strategy.EmaCrossStrategy;
import com.cjie.cryptocurrency.quant.strategy.SimpleRangeScalperStrategy;
import org.ta4j.core.TimeSeries;

import java.math.BigDecimal;

/**
 * Example how you could use and test strategies with example data of this repository
 */
public class Run {


    public static void main(String[] args) {

        // load data as TimeSeries
        SwapLoader loader = new SwapLoader();
        TimeSeries series = loader.getMinuteTimeSeries("/Users/hucj/cryptocurrency-quant/quant-strategy/src/main/data/btc_minutes.csv", "btc");

        // create and initialize a strategy
        EmaCrossStrategy simpleRangeScalper = new EmaCrossStrategy();
        simpleRangeScalper.initStrategy(series);

        // run strategy on time series and analyse results
        StrategyAnalyser analyser = new StrategyAnalyser();
        analyser.printAllResults(simpleRangeScalper);

        // change parameters of the strategy and run again
        simpleRangeScalper.setParams(13, 50, BigDecimal.valueOf(0.5));
        analyser.printAllResults(simpleRangeScalper);
    }
}
