package com.cjie.cryptocurrency.quant.strategy;

import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public abstract class BaseStrategyBuilder implements StrategyBuilder {

    protected boolean isMock = true;

    protected boolean isBackTest = false;

    protected BaseBarSeries series;

    protected ClosePriceIndicator closePrice;

    BaseStrategyBuilder(BaseBarSeries series, boolean isBackTest, boolean isMock) {
        this.series = series;
        this.isBackTest = isBackTest;
        this.isMock = isMock;

        this.closePrice = new ClosePriceIndicator(this.series);
    }

    @Override
    public TradingRecord getTradingRecord(Order.OrderType type) {
        BarSeriesManager seriesManager = new BarSeriesManager(series);
        return seriesManager.run(buildStrategy(type), type);
    }

    @Override
    public BaseBarSeries getTimeSeries(){
        return this.series;
    }

    @Override
    public boolean isMock() {
        return this.isMock;
    }

    @Override
    public boolean isBackTest() {
        return this.isBackTest;
    }
}