package com.cjie.cryptocurrency.quant.strategy;

import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/*
    http://www.investopedia.com/terms/s/scalping.asp
    http://forexop.com/strategy/simple-range-scalper/
 */
public class EmaCrossStrategy implements StrategyBuilder {

    private TimeSeries series;

    private ClosePriceIndicator closePrice;
    private Indicator<Num> maxPrice;
    private Indicator<Num> minPrice;

    EMAIndicator shortEma;
    EMAIndicator longEma;

    // parameters
    private BigDecimal takeProfitValue;

    private int shortEmaCount;

    private int longEmaCount;
    /**
     * Constructor
     */
    public EmaCrossStrategy(){}

    public EmaCrossStrategy(TimeSeries series){
        initStrategy(series);
    }

    @Override
    public void initStrategy(TimeSeries series) {
        this.series = series;
        this.minPrice = new MinPriceIndicator(this.series);
        this.closePrice = new ClosePriceIndicator(this.series);
        this.maxPrice = new MaxPriceIndicator(this.series);
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

        return new BaseStrategy(entrySignal, exitSignal.or(exitSignal2), 5);

    }

    private Strategy getShortStrategy(){

        Rule entrySignal = new CrossedDownIndicatorRule(shortEma, longEma);

        Rule exitSignal = new CrossedUpIndicatorRule(shortEma, longEma);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, DoubleNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, exitSignal.or(exitSignal2), 5);
    }
}
