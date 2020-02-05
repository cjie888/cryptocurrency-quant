package com.cjie.cryptocurrency.quant.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractEMAIndicator;
import org.ta4j.core.num.Num;

public class MMAIndicator extends AbstractEMAIndicator {

    private static final long serialVersionUID = -7287520945130507544L;

    private Indicator<Num> indicator;

    private final int barCount;

    private final Num multiplier;


    /**
     * Constructor.
     *
     * @param indicator an indicator
     * @param barCount the MMA time frame
     */
    public MMAIndicator(Indicator<Num> indicator, int barCount) {
        super(indicator, barCount, 1.0/barCount);
        this.indicator = indicator;
        this.barCount = barCount;
        this.multiplier = numOf(1.0/barCount);
    }

    @Override
    protected Num calculate(int index) {
        if (index == 0) {
            return indicator.getValue(0);
        }
        Num prevValue = getValue(index - 1);
        Num num = indicator.getValue(index).minus(prevValue).multipliedBy(index < barCount ? numOf(1.0/(index + 1)) : multiplier)
                .plus(prevValue);
        //System.out.println(index  + ":" + num + " pre:" + prevValue + " curr:" + indicator.getValue(index));
        return num;
    }
}

