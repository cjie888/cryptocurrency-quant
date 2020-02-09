package com.cjie.cryptocurrency.quant.strategy;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



public class EmaCrossStrategy extends BaseStrategyBuilder {

    EMAIndicator shortEma;
    EMAIndicator longEma;

    // parameters
    private BigDecimal takeProfitValue;

    private int shortEmaCount;

    private int longEmaCount;


    public EmaCrossStrategy(BaseBarSeries series, boolean isBackTest, boolean isMock) {
        super(series, isBackTest, isMock);
        initStrategy(series);
    }

    @Override
    public void initStrategy(BaseBarSeries series) {
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
    public String getName(){
        return "EmaCross";
    }

    @Override
    public List<String> getParamters(){
        ArrayList<String> parameters = new ArrayList<String>();
        String takeProfit = "Take Profit: "+ this.takeProfitValue;
        String emaShort = "EMA Short:"+ this.shortEmaCount;
        String emaLong = "EMA Long:"+ this.longEmaCount;
        parameters.add(takeProfit);
        parameters.add(emaShort);
        parameters.add(emaLong);
        return  parameters;
    }

    /**
     * call this function to change the parameter of the strategy
     * @param shortEmaCount short exponential moving average the bands are based on
     * @param takeProfitValue close a trade if this percentage profit is reached
     */
    public void setParams(int shortEmaCount, int longEmaCount, BigDecimal takeProfitValue){
        this.takeProfitValue = takeProfitValue;
        this.shortEmaCount = shortEmaCount;
        this.longEmaCount = longEmaCount;

        shortEma = new EMAIndicator(closePrice, shortEmaCount);
        longEma = new EMAIndicator(closePrice, longEmaCount);
    }

    private Strategy getLongStrategy() {


        Rule entrySignal = new CrossedUpIndicatorRule(shortEma, longEma);

        Rule exitSignal = new CrossedDownIndicatorRule(shortEma, longEma);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, DoubleNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, isBackTest == false ? exitSignal :  exitSignal.or(exitSignal2), longEmaCount);

    }

    private Strategy getShortStrategy(){

        Rule entrySignal = new CrossedDownIndicatorRule(shortEma, longEma);

        Rule exitSignal = new CrossedUpIndicatorRule(shortEma, longEma);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, DoubleNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, isBackTest == false ? exitSignal : exitSignal.or(exitSignal2), longEmaCount);
    }
}
