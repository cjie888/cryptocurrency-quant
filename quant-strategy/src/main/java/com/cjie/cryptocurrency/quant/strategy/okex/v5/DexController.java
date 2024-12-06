package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSONObject;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.dex.DexApiService;
import com.cjie.cryptocurrency.quant.mapper.DexOrderMapper;
import com.cjie.cryptocurrency.quant.model.DexOrder;
import com.cjie.cryptocurrency.quant.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dex")
@Slf4j
public class DexController {

    @Autowired
    private DexOrderMapper dexOrderMapper;

    @Autowired
    private DexApiService dexApiService;

    @PostMapping(value = "/sell_order")
    Response<?> sellOrder(@RequestBody JSONObject requestBody) {
        try {
            String tokenAddress = requestBody.getString("tokenAddress");
            String chainId = requestBody.getString("chainId");
            BigDecimal amount = requestBody.getBigDecimal("amount");
            BigDecimal price =  dexApiService.getPrice(chainId, tokenAddress);;
            String txId = requestBody.getString("chainId");
            String fromAddress = tokenAddress;
            String toAddress = "11111111111111111111111111111111";
            DexOrder newDexOrder = new DexOrder();
            newDexOrder.setChainId(chainId);
            newDexOrder.setFromAddress(fromAddress);
            newDexOrder.setToAddress(toAddress);
            newDexOrder.setCreateTime(new Date());
            newDexOrder.setSize(amount);
            newDexOrder.setPrice(price);
            newDexOrder.setIsMock((byte)0);
            newDexOrder.setTxId(txId);
            newDexOrder.setStatus(1);
            dexOrderMapper.insert(newDexOrder);
            return Response.success(null);
        } catch (Exception e) {
            log.error("generate token error", e);
            return Response.error(500, "generate token error");
        }
    }

    @PostMapping(value = "/buy_order")
    Response<?> buyOrder(@RequestBody JSONObject requestBody) {
        try {
            String tokenAddress = requestBody.getString("tokenAddress");
            String chainId = requestBody.getString("chainId");
            BigDecimal amount = requestBody.getBigDecimal("amount");
            BigDecimal price =  dexApiService.getPrice(chainId, tokenAddress);;
            String txId = requestBody.getString("chainId");
            String toAddress = tokenAddress;
            String fromAddress = "11111111111111111111111111111111";
            DexOrder newDexOrder = new DexOrder();
            newDexOrder.setChainId(chainId);
            newDexOrder.setFromAddress(fromAddress);
            newDexOrder.setToAddress(toAddress);
            newDexOrder.setCreateTime(new Date());
            newDexOrder.setSize(amount);
            newDexOrder.setPrice(price);
            newDexOrder.setIsMock((byte)0);
            newDexOrder.setTxId(txId);
            newDexOrder.setStatus(1);
            dexOrderMapper.insert(newDexOrder);
            return Response.success(null);
        } catch (Exception e) {
            log.error("generate token error", e);
            return Response.error(500, "generate token error");
        }
    }
}
