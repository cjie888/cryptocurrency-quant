package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.dex.DexApiService;
import com.cjie.cryptocurrency.quant.mapper.DexOrderMapper;
import com.cjie.cryptocurrency.quant.model.DexOrder;
import com.cjie.cryptocurrency.quant.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

@Component
@Slf4j
public class DexService {

    @Autowired
    @Qualifier("telegramMessageServiceImpl")
    private MessageService messageService;

    @Autowired
    private DexApiService dexApiService;

    @Autowired
    private DexOrderMapper dexOrderMapper;

    public void sell(String chainId, String tokenAddress, BigDecimal expectPrice, BigDecimal amount) {

        DexOrder dexOrder = dexOrderMapper.selectByFromAddress(chainId, tokenAddress);

        if (dexOrder != null) {
            return;
        }
        JSONObject tokenDetail = dexApiService.getTokenDetail(chainId, tokenAddress);
        if (tokenDetail == null) {
            return;
        }
        //log.info("Get token detail for {}, result:{}", tokenAddress, JSONObject.toJSONString(tokenDetail));

        Integer tokenDeciamls = tokenDetail.getIntValue("decimals");
        if (tokenDeciamls == null) {
            return;
        }
        log.info("Get token decimals for {}, result:{}", tokenAddress, tokenDeciamls);
        BigDecimal tokenPrice = dexApiService.getPrice(chainId, tokenAddress);
        //System.out.println(tokenPrice);
        log.info("Compute price, tokenPrice: {}, expectPrice:{}", tokenPrice, expectPrice);
        if (tokenPrice.compareTo(expectPrice) > 0) {
            BigDecimal tradeAmount = amount.multiply(new BigDecimal("10").pow(tokenDeciamls));
            log.info("Token price exceed expect price, selling, token:{}, amount:{}", tokenAddress, tradeAmount);
            dexApiService.sell(chainId, tokenAddress, tradeAmount.toPlainString());
            messageService.sendMessage("sell", "Sell " + tokenDetail.getString("symbol") + " , amout:" + amount.toPlainString());
        }

    }


    public void buy(String chainId, String tokenAddress, BigDecimal expectPrice, BigDecimal amount) {

        DexOrder dexOrder = dexOrderMapper.selectByToAddress(chainId, tokenAddress);

        if (dexOrder != null) {
            return;
        }
        JSONObject tokenDetail = dexApiService.getTokenDetail(chainId, tokenAddress);
        if (tokenDetail == null) {
            return;
        }
        //log.info("Get token detail for {}, result:{}", tokenAddress, JSONObject.toJSONString(tokenDetail));

        Integer tokenDeciamls = tokenDetail.getIntValue("decimals");
        if (tokenDeciamls == null) {
            return;
        }
        log.info("Get token decimals for {}, result:{}", tokenAddress, tokenDeciamls);
        BigDecimal tokenPrice = dexApiService.getPrice(chainId, tokenAddress);
        //System.out.println(tokenPrice);
        log.info("Compute price, tokenPrice: {}, expectPrice:{}", tokenPrice, expectPrice);
        if (tokenPrice.compareTo(expectPrice) < 0) {
            BigDecimal tradeAmount = amount.multiply(new BigDecimal("10").pow(tokenDeciamls));
            log.info("Token price exceed expect price, selling, token:{}, amount:{}", tokenAddress, tradeAmount);
            dexApiService.buy(chainId, tokenAddress, tradeAmount.toPlainString());
            messageService.sendMessage("Buy", "Buy " + tokenDetail.getString("symbol") + " , amout:" + amount.toPlainString());

        }

    }
}
