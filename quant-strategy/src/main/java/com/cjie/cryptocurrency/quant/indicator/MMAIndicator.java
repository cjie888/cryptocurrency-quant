package com.cjie.cryptocurrency.quant.indicator;

import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractEMAIndicator;
import org.ta4j.core.num.Num;

@Slf4j(topic = "strategy")
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
//        if (index == 0) {
//            return indicator.getValue(0);
//        }
//        Num prevValue = getValue(index - 1);
//        Num num = indicator.getValue(index).minus(prevValue).multipliedBy(index < barCount ? numOf(1.0/(index + 1)) : multiplier)
//                .plus(prevValue);
        Num sum = indicator.getValue(index);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("index:"+index +",close:" +indicator.getValue(index));
        for (int i = 1; i < barCount; i++)  {
            if (index - i >= 0) {
                stringBuilder.append("index:"+(index-i) +",close:" +indicator.getValue(index-i));
                sum = sum.plus(indicator.getValue(index-i));
            }
        }
        Num num = (index + 1) < barCount ? sum.dividedBy(numOf(index+1)) : sum.dividedBy(numOf(barCount));
        //log.info("mma {} closes:{}", num, stringBuilder.toString());
        //System.out.println(index  + ":" + num + " pre:" + prevValue + " curr:" + indicator.getValue(index));
        return num;
    }
}

