package com.cjie.cryptocurrency.quant.strategy;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
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
public class SimpleRangeScalperStrategy extends BaseStrategyBuilder {

    private Indicator<Num> maxPrice;
    private Indicator<Num> minPrice;

    // parameters
    private BigDecimal takeProfitValue;
    private int emaForBollingerBandValue;

    private BollingerBandsUpperIndicator upperBollingerBand;
    private BollingerBandsMiddleIndicator middleBollingerBand;
    private BollingerBandsLowerIndicator lowerBollingeBand;

    public BollingerBandsUpperIndicator getUpperBollingerBand() {
        return upperBollingerBand;
    }

    public BollingerBandsMiddleIndicator getMiddleBollingerBand() {
        return middleBollingerBand;
    }

    public BollingerBandsLowerIndicator getLowerBollingeBand() {
        return lowerBollingeBand;
    }


    public SimpleRangeScalperStrategy(TimeSeries series, boolean isBackTest, boolean isMock){
        super(series, isBackTest, isMock);
        initStrategy(series);
    }

    @Override
    public void initStrategy(TimeSeries series) {
        this.minPrice = new MinPriceIndicator(this.series);
        this.maxPrice = new MaxPriceIndicator(this.series);
        setParams(14, BigDecimal.valueOf(0.5));
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
        return "SimpleRangeScalper";
    }

    @Override
    public List<String> getParamters(){
        ArrayList<String> parameters = new ArrayList<String>();
        String takeProfit = "Take Profit: "+ this.takeProfitValue;
        String ema = "EMA :"+ this.emaForBollingerBandValue;
        parameters.add(takeProfit);
        parameters.add(ema);
        return  parameters;
    }

    /**
     * call this function to change the parameter of the strategy
     * @param emaForBollingerBandValue exponential moving average the bollinger bands are based on
     * @param takeProfitValue close a trade if this percentage profit is reached
     */
    public void setParams(int emaForBollingerBandValue, BigDecimal takeProfitValue){
        this.takeProfitValue = takeProfitValue;
        this.emaForBollingerBandValue = emaForBollingerBandValue;

        EMAIndicator ema = new EMAIndicator(this.closePrice, emaForBollingerBandValue);
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(this.closePrice, emaForBollingerBandValue);
        this.middleBollingerBand = new BollingerBandsMiddleIndicator(ema);
        this.lowerBollingeBand = new BollingerBandsLowerIndicator(this.middleBollingerBand, standardDeviation);
        this.upperBollingerBand = new BollingerBandsUpperIndicator(this.middleBollingerBand, standardDeviation);
    }

    private Strategy getLongStrategy() {

        Indicator<Num> d_upper_middle = new DifferenceIndicator(this.upperBollingerBand, this.middleBollingerBand);
        // exit if half way up to middle reached
        Indicator<Num> threshold = new MultiplierIndicator(d_upper_middle, Double.valueOf(0.5));

        Rule entrySignal = new CrossedUpIndicatorRule(this.maxPrice, this.upperBollingerBand);
        Rule entrySignal2 = new UnderIndicatorRule(this.minPrice, this.upperBollingerBand);

        Rule exitSignal = new CrossedDownIndicatorRule(this.closePrice, threshold);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, PrecisionNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal.and(entrySignal2), isBackTest == false ? exitSignal : exitSignal.or(exitSignal2), 5);

    }

    private Strategy getShortStrategy(){

        Indicator<Num> d_middle_lower = new DifferenceIndicator(this.middleBollingerBand, this.lowerBollingeBand);
        // exit if half way down to middle reached
        Indicator<Num> threshold = new MultiplierIndicator(d_middle_lower, Double.valueOf(0.5));

        Rule entrySignal = new CrossedDownIndicatorRule(this.minPrice, this.lowerBollingeBand);
        Rule entrySignal2 = new OverIndicatorRule(this.maxPrice, this.lowerBollingeBand);

        Rule exitSignal = new CrossedUpIndicatorRule(this.closePrice, threshold);
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, PrecisionNum.valueOf(this.takeProfitValue)); // stop loss long = stop gain short?

        return new BaseStrategy(entrySignal.and(entrySignal2), isBackTest == false ? exitSignal :  exitSignal.or(exitSignal2), 5);
    }
}
