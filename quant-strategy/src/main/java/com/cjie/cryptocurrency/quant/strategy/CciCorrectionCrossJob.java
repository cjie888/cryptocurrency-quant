package com.cjie.cryptocurrency.quant.strategy;


import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;


import java.math.BigDecimal;

@Component
@Slf4j(topic = "strategy")
public class CciCorrectionCrossJob extends BaseSwapStrategyJob {


    @Scheduled(cron = "11 */1 * * * ?")
    public void execute() {
        log.info("start cci correction job");
        executeStrategy("BTC-USD-SWAP", true);
        executeStrategy("ETH-USD-SWAP", true);
        executeStrategy("EOS-USD-SWAP", true);
        executeStrategy("LTC-USD-SWAP", true);
        executeStrategy("XRP-USD-SWAP", true);
        executeStrategy("BCH-USD-SWAP", true);
        executeStrategy("BSV-USD-SWAP", true);
        executeStrategy("ETC-USD-SWAP", true);
    }


    @Override
    public StrategyBuilder buildStrategy(TimeSeries timeSeries, boolean isMock) {
        CCICorrctionStrategy strategy = new CCICorrctionStrategy(timeSeries, true, isMock);
        strategy.setParams(5, 100, BigDecimal.valueOf(1));
        return strategy;
    }
}
