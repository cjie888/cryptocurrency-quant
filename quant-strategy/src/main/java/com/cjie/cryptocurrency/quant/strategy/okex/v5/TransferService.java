package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.AssetBalance;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.FundsTransfer;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.PiggyBankPurchaseRedemption;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.FundingAPIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;



@Component
@Slf4j
public class TransferService {


    @Autowired
    private FundingAPIService fundingAPIService;

    @Autowired
    private AccountAPIV5Service accountAPIService;


    public void transfer(String site, String ccy, String size, Double ratio) {
        try {

            HttpResult<List<AccountInfo>> baseAccountResult = accountAPIService.getBalance(site, ccy);
            log.info("Transfer spot base account:{}", JSON.toJSONString(baseAccountResult));
            if (Objects.nonNull(baseAccountResult) && "0".equals(baseAccountResult.getCode())) {

                //余额不足
                if (baseAccountResult.getData().get(0).getDetails().size() == 0 ||
                        Double.parseDouble(baseAccountResult.getData().get(0).getDetails().get(0).getAvailEq()) < Double.parseDouble(size) * 5) {

                    //从资金账号转入
                    String transferAmount = String.valueOf(Double.parseDouble(size) * ratio);

                    FundsTransfer transferIn = new FundsTransfer();
                    transferIn.setCcy(ccy);
                    transferIn.setFrom("6");
                    transferIn.setTo("18");
                    transferIn.setAmt(transferAmount);
                    try {
                        JSONObject transferResult = fundingAPIService.fundsTransfer(site, transferIn);
                        log.info("Transfer from asset to spot,ccy:{},amount:{}, result:{}",
                                ccy, transferAmount, JSON.toJSONString(transferResult));
                    } catch (Exception e) {
                        log.info("Transfer from asset to spot error,ccy:{},amount:{}", ccy, transferAmount, e);
                    }
                }


                if (Double.parseDouble(baseAccountResult.getData().get(0).getDetails().get(0).getAvailEq()) > Double.parseDouble(size) * (5 + ratio)) {
                    //转出资金账号
                    String transferAmount = String.valueOf(Double.parseDouble(size) * ratio);
                    FundsTransfer transferIn = new FundsTransfer();
                    transferIn.setCcy(ccy);
                    transferIn.setFrom("18");
                    transferIn.setTo("6");
                    transferIn.setAmt(transferAmount);
                    try {
                        JSONObject transferResult = fundingAPIService.fundsTransfer(site, transferIn);
                        log.info("Transfer from spot to asset,ccy:{},amount:{}, result:{}",
                                ccy, transferAmount, JSON.toJSONString(transferResult));
                    } catch (Exception e) {
                        log.info("Transfer from spot to asset,ccy:{},amount:{}", ccy, transferAmount);
                    }
                }

            }

            HttpResult<List<AssetBalance>> assetBalanceResult = fundingAPIService.getBalance(site, ccy);
            log.info("Transfer asset base account:{}", JSON.toJSONString(assetBalanceResult));
            if (Objects.nonNull(assetBalanceResult) && "0".equals(assetBalanceResult.getCode())) {

                //余额不足
                if (assetBalanceResult.getData().size() == 0 ||
                        Double.parseDouble(assetBalanceResult.getData().get(0).getAvailBal()) < Double.parseDouble(size) * 5) {
                    //赎回
                    try {
                        String transferAmount = String.valueOf(5 - Double.parseDouble(assetBalanceResult.getData().get(0).getAvailBal()));
                        PiggyBankPurchaseRedemption piggyBankPurchaseRedemption = new PiggyBankPurchaseRedemption();
                        piggyBankPurchaseRedemption.setCcy(ccy);
                        piggyBankPurchaseRedemption.setAmt(transferAmount);
                        piggyBankPurchaseRedemption.setSide("redempt");
                        JSONObject result1 = fundingAPIService.piggyBankPurchaseRedemption(site, piggyBankPurchaseRedemption);
                        log.info("transfer {} {} from financial to asset, result:{}", transferAmount, ccy, JSON.toJSONString(result1));
                    } catch (Exception e) {
                        //ignore
                    }

                }


                if (Double.parseDouble(assetBalanceResult.getData().get(0).getAvailBal()) > Double.parseDouble(size) * (5 + ratio)) {
                    //申购
                    try {
                        String transferAmount = String.valueOf(ratio);
                        PiggyBankPurchaseRedemption piggyBankPurchaseRedemption = new PiggyBankPurchaseRedemption();
                        piggyBankPurchaseRedemption.setCcy(ccy);
                        piggyBankPurchaseRedemption.setAmt(transferAmount);
                        piggyBankPurchaseRedemption.setSide("purchase");
                        JSONObject result1 = fundingAPIService.piggyBankPurchaseRedemption(site, piggyBankPurchaseRedemption);
                        log.info("transfer {} {} from asset  to financial, result:{}", transferAmount, ccy, JSON.toJSONString(result1));
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("Transfer base account:{} error", ccy, e);

        }

    }
}
