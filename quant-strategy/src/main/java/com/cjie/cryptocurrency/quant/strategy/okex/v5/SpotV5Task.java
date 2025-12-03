package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountDetail;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SpotV5Task {

    @Autowired
    private SpotV5Service spotService;

    @Autowired
    private AccountAPIV5Service accountAPIService;

    @Scheduled(cron = "3/17 * * * * ?")
    public  void netGrid() {

        //获取账户余额
        String site = "okexsub2";
        String baseCurrency = "ETH,TRX,SHIB,HBAR,SOL";
        Map<String, AccountDetail> balances = new HashMap<>();
        try {
            HttpResult<List<AccountInfo>> baseAccountResult = accountAPIService.getBalance(site, baseCurrency);
            log.info("base account:{}", JSON.toJSONString(baseAccountResult));
            if (baseAccountResult.getData().get(0).getDetails().size() > 0) {
                for (AccountDetail accountDetail : baseAccountResult.getData().get(0).getDetails()) {
                    balances.put(accountDetail.getCcy(), accountDetail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("base account balance map :{}", JSON.toJSONString(balances));

//        spotService.netGrid("okexsub2", "MANA-USDT", "1", 0.03);
        spotService.netGrid("okexsub2", "ETH-USDT", "0.01000000", 0.03, balances.get("ETH"));
        spotService.netGrid("okexsub2", "TRX-USDT", "40", 0.03, balances.get("TRX"));
        spotService.netGrid("okexsub2", "SHIB-USDT", "400000", 0.03, balances.get("SHIB"));
        spotService.netGrid("okexsub2", "SOL-USDT", "0.1", 0.03, balances.get("SOL"));
        spotService.netGrid("okexsub2", "HBAR-USDT", "50", 0.03, balances.get("HBAR"));

        try {
            String mainSite = "okexsub1";
            String mainBaseCurrency = "XRP,DOGE,BNB,SUI,LINK,BTC,ADA";
            HttpResult<List<AccountInfo>> baseAccountResult = accountAPIService.getBalance(mainSite, mainBaseCurrency);
            if (baseAccountResult.getData().get(0).getDetails().size() > 0) {
                for (AccountDetail accountDetail : baseAccountResult.getData().get(0).getDetails()) {
                    balances.put(accountDetail.getCcy(), accountDetail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        log.info("base account balance map :{}", JSON.toJSONString(balances));


        spotService.netGrid("okexsub1", "XRP-USDT", "20", 0.03, balances.get("XRP"));
        spotService.netGrid("okexsub1", "DOGE-USDT", "100", 0.03, balances.get("DOGE"));
        spotService.netGrid("okexsub1", "BNB-USDT", "0.05", 0.03, balances.get("BNB"));
        spotService.netGrid("okexsub1", "SUI-USDT", "10", 0.03, balances.get("SUI"));
        spotService.netGrid("okexsub1", "LINK-USDT", "1", 0.03, balances.get("LINK"));
        spotService.netGrid("okexsub1", "BTC-USDT", "0.00050000", 0.02, balances.get("BTC"));
        spotService.netGrid("okexsub1", "ADA-USDT", "20", 0.03, balances.get("ADA"));

//ltc 6  xrp 3 etc 5
    }


    @Scheduled(cron = "11/13 * * * * ?")
    public  void netGrid2() {
        String site = "okex";
        String baseCurrency = "DOT,BCH,LTC,OKB,XLM,AVAX,AAVE,NEAR,ONDO,UNI,TON,APT,PEPE";
        HttpResult<List<AccountInfo>> baseAccountResult = accountAPIService.getBalance(site, baseCurrency);
        log.info("base account:{}", JSON.toJSONString(baseAccountResult));
        Map<String, AccountDetail> balances = new HashMap<>();
        try {
            if (baseAccountResult.getData().get(0).getDetails().size() > 0) {
                for (AccountDetail accountDetail : baseAccountResult.getData().get(0).getDetails()) {
                    balances.put(accountDetail.getCcy(), accountDetail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("base account balance map :{}", JSON.toJSONString(balances));
        spotService.netGrid("okex", "DOT-USDT", "2", 0.03, balances.get("DOT"));
        spotService.netGrid("okex", "BCH-USDT", "0.05", 0.03, balances.get("BCH"));
        spotService.netGrid("okex", "LTC-USDT", "0.2", 0.03, balances.get("LTC"));
        spotService.netGrid("okex", "OKB-USDT", "0.5", 0.03, balances.get("OKB"));
        spotService.netGrid("okex", "XLM-USDT", "30", 0.03, balances.get("XLM"));
        spotService.netGrid("okex", "AAVE-USDT", "0.1", 0.03, balances.get("AAVE"));
        spotService.netGrid("okex", "AVAX-USDT", "0.5", 0.03, balances.get("AVAX"));
        spotService.netGrid("okex", "NEAR-USDT", "5", 0.03, balances.get("NEAR"));
        spotService.netGrid("okex", "ONDO-USDT", "10", 0.03, balances.get("ONDO"));
        spotService.netGrid("okex", "UNI-USDT", "2", 0.03, balances.get("UNI"));
        spotService.netGrid("okex", "TON-USDT", "2", 0.03, balances.get("TON"));
        spotService.netGrid("okex", "APT-USDT", "3", 0.03, balances.get("APT"));
        spotService.netGrid("okex", "PEPE-USDT", "1000000", 0.03, balances.get("PEPE"));

    }
}
