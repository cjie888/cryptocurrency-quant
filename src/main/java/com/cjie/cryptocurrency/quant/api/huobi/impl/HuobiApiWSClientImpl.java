package com.cjie.cryptocurrency.quant.api.huobi.impl;

import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiException;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiWSClient;
import com.cjie.cryptocurrency.quant.api.huobi.impl.ws.*;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSClientOption;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;
import okhttp3.OkHttpClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HuobiApiWSClientImpl implements HuobiApiWSClient, Closeable {

    private final OkHttpClient client;

    private HuobiWSClientOption option;

    public HuobiApiWSClientImpl() {
        this(null);
    }

    public HuobiApiWSClientImpl(HuobiWSClientOption option) {
        this.option = option == null ? new HuobiWSClientOption() : option;
        final OkHttpClient.Builder build = new OkHttpClient.Builder();
        build.connectTimeout(this.option.getConnectTimeout(), TimeUnit.SECONDS);
        build.readTimeout(this.option.getReadTimeout(), TimeUnit.SECONDS);
        build.writeTimeout(this.option.getWriteTimeout(), TimeUnit.SECONDS);

        this.client = build.build();
        this.client.dispatcher().setMaxRequests(this.option.getMaxRequests());
        this.client.dispatcher().setMaxRequestsPerHost(this.option.getMaxRequestsPerHost());
    }

    public OkHttpClient getClient() {
        return client;
    }

    @Override
    public void setOption(HuobiWSClientOption option) {
        if (option == null) {
            throw new IllegalArgumentException();
        }
        this.option = option;
        this.client.dispatcher().setMaxRequests(this.option.getMaxRequests());
        this.client.dispatcher().setMaxRequestsPerHost(this.option.getMaxRequestsPerHost());
    }

    @Override
    public HuobiWSClientOption getOption() {
        return this.option;
    }

    @Override
    public void depth(String symbol, String type, HuobiWSEventHandler handler) throws HuobiApiException {
        try {
            AbsHuobiApiWSClient client = new HuobiApiWSDepthClient(this, handler, symbol, type);
            client.start();
        } catch (Exception e) {
            throw new HuobiApiException(e);
        }
    }

    @Override
    public void kline(String symbol, String period, HuobiWSEventHandler handler) throws HuobiApiException {

        try {
            AbsHuobiApiWSClient client = new HuobiApiWSKLineClient(this, handler, symbol, period);
            client.start();
        } catch (Exception e) {
            throw new HuobiApiException(e);
        }

    }

    @Override
    public void tradeDetail(String symbol, HuobiWSEventHandler handler) throws HuobiApiException {
        try {
            AbsHuobiApiWSClient client = new HuobiApiWSTradeDetailClient(this, handler, symbol);
            client.start();
        } catch (Exception e) {
            throw new HuobiApiException(e);
        }
    }

    @Override
    public void marketDetail(String symbol, HuobiWSEventHandler handler) throws HuobiApiException {
        try {
            new HuobiApiWSMarketDetailClient(this, handler, symbol).start();
        } catch (Exception e) {
            throw new HuobiApiException(e);
        }
    }

    @Override
    public void close() throws IOException {
        this.client.dispatcher().executorService().shutdown();
    }
}
