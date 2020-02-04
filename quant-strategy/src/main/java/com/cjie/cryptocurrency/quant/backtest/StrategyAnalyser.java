package com.cjie.cryptocurrency.quant.backtest;


import org.ta4j.core.Bar;
import org.ta4j.core.Order;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.*;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StrategyAnalyser {

    private AverageProfitableTradesCriterion avgProfitTrades = new AverageProfitableTradesCriterion();
    private AverageProfitCriterion avgProfit = new AverageProfitCriterion();
    private TotalProfitCriterion totalProfit = new TotalProfitCriterion();
    private BuyAndHoldCriterion buyAndHold = new BuyAndHoldCriterion();
    private NumberOfTradesCriterion numTrades = new NumberOfTradesCriterion();
    //private NumberOfTicksCriterion numTicks = new NumberOfTicksCriterion();
    private VersusBuyAndHoldCriterion vsByAndHold = new VersusBuyAndHoldCriterion(avgProfit);
    private RewardRiskRatioCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
    private MaximumDrawdownCriterion maxDrawDown = new MaximumDrawdownCriterion();


    public void printAllResults(StrategyBuilder strategyBuilder){
        printResults(strategyBuilder, Order.OrderType.BUY, true);
        print("");
        printResults(strategyBuilder, Order.OrderType.SELL, true);
    }

    public void printResults(StrategyBuilder sb, Order.OrderType type, boolean showTrades){
        if (type.equals(Order.OrderType.SELL)) {
            print("RUN STRATEGY SHORT:");
        } else {
            print("RUN STRATEGY LONG:");
        }
        TradingRecord record = sb.getTradingRecord(type);
        if (record == null) {
            print("   -no long record found");
        } else {
            TimeSeries series = sb.getTimeSeries();

            print(" -General:");
            print("     -Series Name:                       " + series.getName());
            print("     -Period:                            " + series.getSeriesPeriodDescription());
            print("     -Strategy Name:                     " + sb.getName());
            print("     -Strategy Parameters:               " + sb.getParamters());
            print(" -Performance Criterions: ");
            print("     -Average Profit:                    " + avgProfit.calculate(series, record));
            print("     -Total Profit:                      " + totalProfit.calculate(series, record));
            print("     -Buy and Hold:                      " + buyAndHold.calculate(series, record));
            //print("     -numTicks:                          " + numTicks.calculate(series, record));
            print("     -numTrades:                         " + numTrades.calculate(series, record));
            print("     -Average Profit vs. By and Hold:    " + vsByAndHold.calculate(series, record));
            print("     -Average Profitable Trades:         " + avgProfitTrades.calculate(series, record));
            print("     -Reward Risk Ratio:                 " + rewardRiskRatio.calculate(series, record));
            print("     -Maximum Drawdown:                  " + maxDrawDown.calculate(series, record));

            print(" -Trades:");
            if (showTrades) {
                for (int i = 0; i < record.getTrades().size(); i++) {
                    Order entry = record.getTrades().get(i).getEntry();
                    Order exit = record.getTrades().get(i).getExit();
                    Bar entryTick = series.getBar(entry.getIndex());
                    Bar exitTick = series.getBar(exit.getIndex());

                    print("     -Entry: "+entry.getIndex()+" "+entryTick.getSimpleDateName()+" "+round(entry.getPrice(),4)
                            +" Exit: "+exit.getIndex()+" "+exitTick.getSimpleDateName()+" "+round(exit.getPrice(),4) + " "
                            + "isProfit:" + (entry.isBuy() ? entryTick.getClosePrice().doubleValue() < exitTick.getClosePrice().doubleValue() : entryTick.getClosePrice().doubleValue() > exitTick.getClosePrice().doubleValue()));
                }
            }
        }
    }

    public void printResults(StrategyBuilder sb, Order.OrderType type){
        printResults(sb, type, false);
    }

    public void print(String msg){
        System.out.println(msg);
    }

    public static Double round(Double value, int n){
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(n, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Double round(Num value, int n){
        return round(value.doubleValue(), n);
    }

    public static Double round(BigDecimal value, int n){
        return round(value.doubleValue(), n);
    }

}
