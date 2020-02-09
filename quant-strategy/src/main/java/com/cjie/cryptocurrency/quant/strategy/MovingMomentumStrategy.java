
package com.cjie.cryptocurrency.quant.strategy;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


//@ElasticJobConf(name = "movingMomentumJob", cron = "30 */1 * * * ?",
//        description = "移动动量策略", eventTraceRdbDataSource = "logDatasource")
@Slf4j(topic = "strategy")
public class MovingMomentumStrategy extends BaseStrategyBuilder {


    EMAIndicator shortMma;
    EMAIndicator longMma;

    MACDIndicator macd;

    EMAIndicator emaMacd;

    StochasticOscillatorKIndicator stochasticOscillK;

    // parameters
    private BigDecimal takeProfitValue;

    private int shortMmaCount;

    private int longMmaCount;

    public MovingMomentumStrategy(BaseBarSeries series, boolean isBackTest, boolean isMock){
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
        return "MovingMomentum";
    }

    @Override
    public List<String> getParamters(){
        ArrayList<String> parameters = new ArrayList<String>();
        String takeProfit = "Take Profit: "+ this.takeProfitValue;
        String mmaShort = "Moving Momentum Short:"+ this.shortMmaCount;
        String mmaLong = "Moving Momentum Long:"+ this.longMmaCount;
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

        shortMma = new EMAIndicator(closePrice, shortMmaCount);
        longMma = new EMAIndicator(closePrice, longMmaCount);

        stochasticOscillK = new StochasticOscillatorKIndicator(series, 14);

        macd = new MACDIndicator(closePrice, shortMmaCount, longMmaCount);
        emaMacd = new EMAIndicator(macd, 18);
    }

    private Strategy getLongStrategy() {


        Rule entrySignal = new OverIndicatorRule(shortMma, longMma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, 20)) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

        // Exit rule
        Rule exitSignal = new UnderIndicatorRule(shortMma, longMma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK,20)) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, PrecisionNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, isBackTest == false ? exitSignal :exitSignal.or(exitSignal2), longMmaCount);

    }

    private Strategy getShortStrategy(){

        // Entry rule
        Rule entrySignal = new UnderIndicatorRule(shortMma, longMma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK,20)) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2

        Rule  exitSignal = new OverIndicatorRule(shortMma, longMma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, 20)) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

        Rule exitSignal2 = new TrailingStopLossRule(closePrice, PrecisionNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entrySignal, isBackTest == false ? exitSignal : exitSignal.or(exitSignal2), longMmaCount);
    }
}
