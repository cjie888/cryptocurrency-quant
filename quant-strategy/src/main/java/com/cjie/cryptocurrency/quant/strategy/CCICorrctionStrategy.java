
package com.cjie.cryptocurrency.quant.strategy;


import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CCIIndicator;;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;;
import java.util.ArrayList;
import java.util.List;


//@ElasticJobConf(name = "cciJob", cron = "40 */1 * * * ?",
//        description = "cci", eventTraceRdbDataSource = "logDatasource")
@Slf4j(topic = "strategy")
public class CCICorrctionStrategy extends BaseStrategyBuilder {

    CCIIndicator shortCci;

    CCIIndicator longCci;

    // parameters
    private BigDecimal takeProfitValue;

    private int shortMmaCount;

    private int longMmaCount;

    public CCICorrctionStrategy(BaseBarSeries series, boolean isBackTest, boolean isMock){
        super(series, isBackTest, isMock);
        initStrategy(series);
    }


    @Override
    public void initStrategy(BaseBarSeries series) {
        setParams(5, 200, BigDecimal.valueOf(0.5));
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
        return "CciCross";
    }

    @Override
    public List<String> getParamters(){
        ArrayList<String> parameters = new ArrayList<String>();
        String takeProfit = "Take Profit: "+ this.takeProfitValue;
        String mmaShort = "CCI Short:"+ this.shortMmaCount;
        String mmaLong = "CCI Long:"+ this.longMmaCount;
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

        longCci = new CCIIndicator(series, longMmaCount);
        shortCci = new CCIIndicator(series, shortMmaCount);

    }

    private Strategy getLongStrategy() {

        Num plus100 = series.numOf(100);
        Num minus100 = series.numOf(-100);


        Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
                .and(new UnderIndicatorRule(shortCci, minus100)); // Signal

        Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
                .and(new OverIndicatorRule(shortCci, plus100)); // Signal
        Rule exitSignal2 = new TrailingStopLossRule(closePrice, PrecisionNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entryRule, isBackTest == false ? exitRule :exitRule.or(exitSignal2), longMmaCount);

    }

    private Strategy getShortStrategy(){

        Num plus100 = series.numOf(100);
        Num minus100 = series.numOf(-100);


        Rule exitRule  = new OverIndicatorRule(longCci, plus100) // Bull trend
                .and(new UnderIndicatorRule(shortCci, minus100)); // Signal

        Rule  entryRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
                .and(new OverIndicatorRule(shortCci, plus100)); // Signal

        Rule exitSignal2 = new TrailingStopLossRule(closePrice, PrecisionNum.valueOf(this.takeProfitValue));

        return new BaseStrategy(entryRule, isBackTest == false ? exitRule : exitRule.or(exitSignal2), longMmaCount);
    }
}
