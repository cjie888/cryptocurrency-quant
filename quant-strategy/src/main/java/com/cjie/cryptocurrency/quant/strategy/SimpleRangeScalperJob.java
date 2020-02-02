package com.cjie.cryptocurrency.quant.strategy;


import com.cjie.cryptocurrency.quant.backtest.StrategyBuilder;
import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import lombok.extern.slf4j.Slf4j;

import org.ta4j.core.*;


import java.math.BigDecimal;

@ElasticJobConf(name = "srsJob", cron = "20 */1 * * * ?",
        description = "srs", eventTraceRdbDataSource = "logDatasource")
@Slf4j(topic = "strategy")
public class SimpleRangeScalperJob extends BaseSwapStrategyJob {


    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("start simple range scalper job");
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
        SimpleRangeScalperStrategy strategy = new SimpleRangeScalperStrategy(timeSeries);
        strategy.setParams(20, BigDecimal.valueOf(1));
        return strategy;
    }
}
