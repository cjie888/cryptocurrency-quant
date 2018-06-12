package com.cjie.cryptocurrency.quant.api.huobi;


import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSClientOption;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;

public interface HuobiApiWSClient {

    void setOption(HuobiWSClientOption option);

    HuobiWSClientOption getOption();

    void depth(String symbol, String type, HuobiWSEventHandler handler) throws HuobiApiException;

    void kline(String symbol, String period, HuobiWSEventHandler handler) throws HuobiApiException;

    void tradeDetail(String symbol, HuobiWSEventHandler handler) throws HuobiApiException;

    void marketDetail(String symbol, HuobiWSEventHandler handler) throws HuobiApiException;
}
