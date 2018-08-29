package com.cjie.cryptocurrency.quant.strategy.okex;

import com.cxytiandi.elasticjob.annotation.ElasticJobConf;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@ElasticJobConf(name = "collectOkexBalanceJob", cron = "* */2 * * * ?",
        description = "获取okex站点余额", eventTraceRdbDataSource = "logDatasource")
@Slf4j
public class CollectOkexBalanceJob implements SimpleJob {

    @Autowired
    private MineService mineService;

    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("start balance");
        try {
            mineService.collectBalance();
        } catch (Exception e) {
            log.error("collect balance error", e);
        }
        log.info("end balance");
    }
}
