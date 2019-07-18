package com.cjie.cryptocurrency.quant.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class PeriodMaxIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> indicator;

    private final int barCount;

    public PeriodMaxIndicator(Indicator<Num> indicator, int barCount) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
    }

    @Override
    protected Num calculate(int index) {
        Num max = getTimeSeries().numOf(0);
        for (int i = Math.max(0, index - barCount + 1); i <= index; i++) {
            if (indicator.getValue(i).doubleValue() > max.doubleValue()) {
                max = indicator.getValue(i);
            }
        }

        return max;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
