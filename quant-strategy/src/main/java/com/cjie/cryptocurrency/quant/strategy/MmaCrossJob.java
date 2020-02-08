package com.cjie.cryptocurrency.quant.strategy;


import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

import org.ta4j.core.*;


import java.math.BigDecimal;

@ElasticJobConf(name = "mmaCrossJob", cron = "10 */1 * * * ?",
        description = "mmaCross", eventTraceRdbDataSource = "logDatasource")
@Slf4j(topic = "strategy")
public class MmaCrossJob extends BaseSwapStrategyJob implements SimpleJob {


    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("start mma cross job");
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
        MmaCrossStrategy strategy = new MmaCrossStrategy(timeSeries, true, isMock);
        return strategy;
    }
}
