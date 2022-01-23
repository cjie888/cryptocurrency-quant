package com.cjie.cryptocurrency.quant.strategy.okex.v5;


import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransferTask {


    @Autowired
    private TransferService transferService;

    @Scheduled(cron = "29 50 */1 * * ?")
    public void transfer() {

        transferService.transfer("okexsub2", "MANA", "1", 3.0);
    }
}
