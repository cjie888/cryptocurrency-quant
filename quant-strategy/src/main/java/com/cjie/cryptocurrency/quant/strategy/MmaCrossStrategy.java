package com.cjie.cryptocurrency.quant.strategy;

import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import com.cjie.cryptocurrency.quant.indicator.MMAIndicator;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



public class MmaCrossStrategy implements StrategyBuilder {

    private TimeSeries series;

    private ClosePriceIndicator closePrice;

    MMAIndicator shortMma;
    MMAIndicator longMma;

    // parameters
    private BigDecimal takeProfitValue;

    private int shortMmaCount;

    private int longMmaCount;
    /**
     * Constructor
     */
    public MmaCrossStrategy(){}

    public MmaCrossStrategy(TimeSeries series){
        initStrategy(series);
    }

    @Override
    public void initStrategy(TimeSeries series) {
        this.series = series;
        this.closePrice = new ClosePriceIndicator(this.series);
        setParams(7, 30, BigDecimal.valueOf(0.5));
    }

    @Override
    public Strategy buildStrategy(Order.OrderType type){
        if (type.equals(Order.OrderType.SELL)) {
            return getShortStrategy();
        }
        return getLongStrategy();
    }

    @Override
    public TradingRecord getTradingRecord(Order.OrderType type) {
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        return seriesManager.run(buildStrategy(type), type);
    }

    @Override
    public TimeSeries getTimeSeries(){
        return this.series;
    }

    @Override
    public String getName(){
        return "MmaCross";
    }

    @Override
    public List<String> getParamters(){
        ArrayList<String> parameters = new ArrayList<String>();
        String takeProfit = "Take Profit: "+ this.takeProfitValue;
        String mmaShort = "MMA Short:"+ this.shortMmaCount;
        String mmaLong = "MMA Long:"+ this.longMmaCount;
        parameters.add(takeProfit);
        parameters.add(mmaShort);
        parameters.add(mmaLong);
        return  parameters;
    }

    /**
     * call this function to change the parameter of the strategy
     * @param shortMmaCount short moving average the bands are based on
     * @param takeProfitValue close a trade if this percentage profit is reached
     */
    public void setParams(int shortMmaCount, int longMmaCount, BigDecimal takeProfitValue){
        this.takeProfitValue = takeProfitValue;
        this.shortMmaCount = shortMmaCount;
        this.longMmaCount = longMmaCount;

        shortMma = new MMAIndicator(closePrice, shortMmaCount);
        longMma = new MMAIndicator(closePrice, longMmaCount);
    }

    private Strategy getLongStrategy() {


        Rule entrySignal = new CrossedUpIndicatorRule(shortMma, longMma);

        Rule exitSignal = new CrossedDownIndicatorRule(shortMma, longMma);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, DoubleNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, exitSignal.or(exitSignal2), longMmaCount);

    }

    private Strategy getShortStrategy(){

        Rule entrySignal = new CrossedDownIndicatorRule(shortMma, longMma);

        Rule exitSignal = new CrossedUpIndicatorRule(shortMma, longMma);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, DoubleNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, exitSignal.or(exitSignal2), longMmaCount);
    }
}
