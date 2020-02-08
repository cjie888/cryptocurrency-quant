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
        executeStrategy("BTC-USD-SWAP");
        executeStrategy("ETH-USD-SWAP");
        executeStrategy("EOS-USD-SWAP");
        executeStrategy("LTC-USD-SWAP");
        executeStrategy("XRP-USD-SWAP");
        executeStrategy("BCH-USD-SWAP");
        executeStrategy("BSV-USD-SWAP");
        executeStrategy("ETC-USD-SWAP");
    }


    @Override
    public StrategyBuilder buildStrategy(TimeSeries timeSeries) {
        MmaCrossStrategy strategy = new MmaCrossStrategy(timeSeries, false, true);
        return strategy;
    }
}
