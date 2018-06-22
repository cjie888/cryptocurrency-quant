package com.cjie.cryptocurrency.quant.api.huobi.impl.ws;

import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.huobi.constant.HuobiConst;
import com.cjie.cryptocurrency.quant.api.huobi.domain.resp.HuobiWSResp;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSError;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSSub;
import com.cjie.cryptocurrency.quant.api.huobi.impl.HuobiApiWSClientImpl;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;
import com.cjie.cryptocurrency.quant.api.huobi.util.HuobiUtil;
import okhttp3.*;
import okio.ByteString;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;

public abstract class AbsHuobiApiWSClient<T extends HuobiWSResp> extends WebSocketListener implements Closeable {

    protected final HuobiApiWSClientImpl client;

    protected final HuobiWSEventHandler handler;

    protected final Class<T> clazz;

    protected WebSocket webSocket;


    public AbsHuobiApiWSClient(final HuobiApiWSClientImpl client, final HuobiWSEventHandler handler, final Class<T> clazz) {
        this.client = client;
        this.handler = handler;
        this.clazz = clazz;
    }

    public void start() {
        Request.Builder builder = new Request.Builder().url(HuobiConst.WS_URL);
        this.webSocket = client.getClient().newWebSocket(builder.build(), this);
    }

    public void shutdown() {
        this.webSocket.close(0, "manual");
    }

    protected abstract HuobiWSSub calcSub();

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        System.out.println(String.format("%s onOpen", getClass().getSimpleName()));
        // String id = UUID.randomUUID().toString();
        // HuobiWSSub sub = new HuobiWSSub(String.format("market.%s.depth.%s", symbol, type), id);
        HuobiWSSub sub = calcSub();
        this.webSocket.send(HuobiUtil.toJson(sub));
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        // logger.info("onMessage {},{}", symbol,text);
    }

    protected abstract void doHandler(T resp);

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        String json = null;
        try {
            json = HuobiUtil.uncompress(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (StringUtils.isEmpty(json)) {
            return;
        }

        if (json.contains("ping")) {
            String pong = json.replace("ping", "pong");
            webSocket.send(pong);
            return;
        }
        // System.out.println(json);
        try {
            T resp =  JSON.parseObject(json, clazz);
            if (resp.status != null && !resp.status.equals(HuobiWSResp.STATUES_OK)) {
                HuobiWSError err = new HuobiWSError(resp.errCode, resp.errMsg);
                if (handler != null) {
                    this.handler.onError(err);
                }
            } else {
                this.doHandler(resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        System.out.println(String.format("%s onClosing %d,%s", getClass().getSimpleName(), code, reason));
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        System.out.println(String.format("%s onClosed %d,%s", getClass().getSimpleName(), code, reason));
        if (this.handler != null) {
            this.handler.onClosed(code, reason);
        }
        if(this.client.getOption().isReconWhenClosed()){
            this.start();
        }

    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.out.println(String.format("%s onFailure,%s", getClass().getSimpleName(), t.getMessage()));
        if (this.handler != null) {
            handler.onFailure(t.getMessage());
        }
        if(this.client.getOption().isReconWhenFailure()){
            this.start();
        }
    }

    @Override
    public void close() throws IOException {
        this.shutdown();
    }
}
