package com.cjie.cryptocurrency.quant.strategy.okex;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Account;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrder;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.*;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapTradeAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapUserAPIServive;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.WeiXinMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SwapService {

    @Autowired
    private SwapMarketAPIService swapMarketAPIService;

    @Autowired
    private SwapUserAPIServive swapUserAPIServive;

    @Autowired
    private SwapTradeAPIService swapTradeAPIService;

    @Autowired
    private AccountAPIService accountAPIService;

    @Autowired
    private WeiXinMessageService weiXinMessageService;

    @Autowired
    private SwapOrderMapper swapOrderMapper;

    private Map<String,Double> ranges = new ConcurrentHashMap<>();

    private Map<String,Double> opens = new ConcurrentHashMap<>();

    private Map<String,LocalDateTime>  lastDates = new ConcurrentHashMap<>();

    public void computeBenefit() {
        String[] instrumentIds = new String[]{"BTC-USD-SWAP","ETH-USD-SWAP","BCH-USD-SWAP",
                "EOS-USD-SWAP","XRP-USD-SWAP","LTC-USD-SWAP"};
        Map<String,BigDecimal> costs = new HashMap<>();
        costs.put("BTC-USD-SWAP", new BigDecimal("0.05"));
        costs.put("ETH-USD-SWAP", new BigDecimal("1.4"));
        costs.put("BCH-USD-SWAP",new BigDecimal("0.9"));
        costs.put("EOS-USD-SWAP",new BigDecimal("60"));
        costs.put("XRP-USD-SWAP",new BigDecimal("900"));
        costs.put("LTC-USD-SWAP",new BigDecimal("3"));
        costs.put("ETH-USDT-SWAP", new BigDecimal("100"));
        String accounts = swapUserAPIServive.getAccounts();
        log.info("获取所有账户信息{}", JSON.toJSONString(accounts));
        ApiAccountsVO apiAccountsVO = JSON.parseObject(accounts, ApiAccountsVO.class);
        BigDecimal benefit = BigDecimal.ZERO;
        BigDecimal allAsset = BigDecimal.ZERO;
        StringBuilder sb = new StringBuilder();
        if (apiAccountsVO != null && CollectionUtils.isNotEmpty(apiAccountsVO.getInfo())) {
            for (ApiAccountVO apiAccountVO : apiAccountsVO.getInfo()) {
                if (apiAccountVO.getInstrument_id().toUpperCase().indexOf("USDT") >=0) {
                    BigDecimal asset = new BigDecimal(apiAccountVO.getEquity());
                    allAsset = allAsset.add(asset);
                    BigDecimal currentCurrencyBenefit = asset.subtract(costs.get(apiAccountVO.getInstrument_id()));
                    BigDecimal currentBenefit = currentCurrencyBenefit;
                    log.info("当前收益{}-{}-{}", apiAccountVO.getInstrument_id(), currentCurrencyBenefit, currentBenefit);
                    sb.append(apiAccountVO.getInstrument_id() + ":" + currentCurrencyBenefit + ":" + currentBenefit);
                    sb.append("\r\n\n");
                    benefit = benefit.add(currentBenefit);
                } else {
                    String swapTicker = swapMarketAPIService.getTickerApi(apiAccountVO.getInstrument_id());
                    ApiTickerVO apiTickerVO = JSON.parseObject(swapTicker, ApiTickerVO.class);
                    log.info("当前价格{}-{}", apiAccountVO.getInstrument_id(), apiTickerVO.getLast());
                    BigDecimal asset = new BigDecimal(apiAccountVO.getEquity());
                    allAsset = allAsset.add(asset.multiply(new BigDecimal(apiTickerVO.getLast())));
                    BigDecimal currentCurrencyBenefit = asset.subtract(costs.get(apiAccountVO.getInstrument_id()));
                    BigDecimal currentBenefit = currentCurrencyBenefit.multiply(new BigDecimal(apiTickerVO.getLast()));
                    log.info("当前收益{}-{}-{}", apiAccountVO.getInstrument_id(), currentCurrencyBenefit, currentBenefit);
                    sb.append(apiAccountVO.getInstrument_id() + ":" + currentCurrencyBenefit + ":" + currentBenefit);
                    sb.append("\r\n\n");
                    benefit = benefit.add(currentBenefit);
                }
            }
        }
        sb.append("总资产" + allAsset + ", 总收益" + benefit);
        weiXinMessageService.sendMessage("收益", sb.toString());
    }


    public void dualTrust(String instrumentId, double ratio) {

        LocalDateTime currentHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);

        LocalDateTime lastDate = lastDates.get(instrumentId);
        if (lastDate == null || lastDate.isBefore(currentHour) ) {
            String kline = swapMarketAPIService.getCandlesApi(instrumentId, null, null, "3600");
            //log.info("获取  {} k线 {} ", instrumentId, kline);
            double maxHigh = 0.0;
            double maxClose = 0.0 ;
            double minClose = 0.0;
            double minLow = 0.0;
            List<String[]> apiKlineVOs = JSON.parseObject(kline, new TypeReference<List<String[]>>(){});
            if (CollectionUtils.isNotEmpty(apiKlineVOs)) {
                apiKlineVOs = apiKlineVOs.subList(0, Math.min(apiKlineVOs.size(), 4));

                int count = 0;
                double open = 0;
                for (String[]  apiKlineVO: apiKlineVOs) {
                    double high = Double.valueOf(apiKlineVO[2]);
                    double close = Double.valueOf(apiKlineVO[4]);
                    double low = Double.valueOf(apiKlineVO[3]);
                    if (count == 0) {
                        maxHigh = high;
                        maxClose = close;
                        minClose = close;
                        minLow = low;
                        open = close;
                    } else {
                        if (high > maxHigh) {
                            maxHigh = high;
                        }
                        if (close > maxClose) {
                            maxClose = close;
                        }
                        if (low < minLow) {
                            minLow = low;
                        }
                        if (close < minClose) {
                            minClose = close;
                        }
                    }
                    count++;
                }
                double range = Math.max(maxHigh - minClose, maxClose - minLow);
                log.info("{} maxHigh:{},minCLose:{},maxClose:{},minLow:{},range:{}", instrumentId,  maxHigh, minClose, maxClose, minLow, range);
                ranges.put(instrumentId, range);
                opens.put(instrumentId, open);
                lastDates.put(instrumentId, currentHour);
            }

        }
        Double range = ranges.get(instrumentId);
        Double open = opens.get(instrumentId);
        String swapTicker = swapMarketAPIService.getTickerApi(instrumentId);
        ApiTickerVO apiTickerVO = JSON.parseObject(swapTicker, ApiTickerVO.class);
        log.info("当前价格{}-{}-open:{}, range:{}", instrumentId, apiTickerVO.getLast(), open, range);
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());
        if (currentPrice > open +  range * ratio) {//突破上轨，开多
            SwapOrder lastOrder = swapOrderMapper.selectLatest(instrumentId);
            if (lastOrder != null && lastOrder.getType() == 1) {
                return;
            }
            weiXinMessageService.sendMessage("平空开多", "平空开多" + instrumentId  + ",价格：" + currentPrice);
            SwapOrder swapOrder = SwapOrder.builder()
                    .createTime(new Date())
                    .instrumentId(instrumentId)
                    .isMock(Byte.valueOf("1"))
                    .size(new BigDecimal(100))
                    .price(BigDecimal.valueOf(currentPrice))
                    .type(Byte.valueOf("1"))
                    .build();
            swapOrderMapper.insert(swapOrder);
        } else if (currentPrice < open - range * ratio) {
            SwapOrder lastOrder = swapOrderMapper.selectLatest(instrumentId);
            if (lastOrder != null && lastOrder.getType() == 2) {
                return;
            }
            weiXinMessageService.sendMessage("平多开空", "平多开空" + instrumentId  + ",价格：" + currentPrice);
            SwapOrder swapOrder = SwapOrder.builder()
                    .createTime(new Date())
                    .instrumentId(instrumentId)
                    .isMock(Byte.valueOf("1"))
                    .size(new BigDecimal(100))
                    .price(BigDecimal.valueOf(currentPrice))
                    .type(Byte.valueOf("2"))
                    .build();
            swapOrderMapper.insert(swapOrder);
        }

    }



    public void netGrid(String instrumentId, String size, Double increment, Double transferAmount) {

        //System.out.println(swapMarketAPIService.getContractsApi());
        //[{"instrument_id":"BTC-USD-SWAP","underlying_index":"BTC","quote_currency":"USD","coin":"BTC","contract_val":"100","listing":"2018-08-28T02:43:23.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.1","base_currency":"BTC","underlying":"BTC-USD","settlement_currency":"BTC","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"LTC-USD-SWAP","underlying_index":"LTC","quote_currency":"USD","coin":"LTC","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"LTC","underlying":"LTC-USD","settlement_currency":"LTC","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"ETH-USD-SWAP","underlying_index":"ETH","quote_currency":"USD","coin":"ETH","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"ETH","underlying":"ETH-USD","settlement_currency":"ETH","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"ETC-USD-SWAP","underlying_index":"ETC","quote_currency":"USD","coin":"ETC","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.001","base_currency":"ETC","underlying":"ETC-USD","settlement_currency":"ETC","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"XRP-USD-SWAP","underlying_index":"XRP","quote_currency":"USD","coin":"XRP","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.00010","base_currency":"XRP","underlying":"XRP-USD","settlement_currency":"XRP","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"EOS-USD-SWAP","underlying_index":"EOS","quote_currency":"USD","coin":"EOS","contract_val":"10","listing":"2018-12-10T11:55:31.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.001","base_currency":"EOS","underlying":"EOS-USD","settlement_currency":"EOS","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"BCH-USD-SWAP","underlying_index":"BCH","quote_currency":"USD","coin":"BCH","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"BCH","underlying":"BCH-USD","settlement_currency":"BCH","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"BSV-USD-SWAP","underlying_index":"BSV","quote_currency":"USD","coin":"BSV","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"BSV","underlying":"BSV-USD","settlement_currency":"BSV","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"TRX-USD-SWAP","underlying_index":"TRX","quote_currency":"USD","coin":"TRX","contract_val":"10","listing":"2019-01-16T04:09:23.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.000010","base_currency":"TRX","underlying":"TRX-USD","settlement_currency":"TRX","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"BTC-USDT-SWAP","underlying_index":"BTC","quote_currency":"USDT","coin":"USDT","contract_val":"0.0001","listing":"2019-11-12T11:16:48.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.1","base_currency":"BTC","underlying":"BTC-USDT","settlement_currency":"USDT","is_inverse":false,"contract_val_currency":"BTC"},{"instrument_id":"ETH-USDT-SWAP","underlying_index":"ETH","quote_currency":"USDT","coin":"USDT","contract_val":"0.001","listing":"2019-11-12T11:16:48.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"ETH","underlying":"ETH-USDT","settlement_currency":"USDT","is_inverse":false,"contract_val_currency":"ETH"},{"instrument_id":"EOS-USDT-SWAP","underlying_index":"EOS","quote_currency":"USDT","coin":"USDT","contract_val":"0.1","listing":"2019-11-12T11:16:48.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.001","base_currency":"EOS","underlying":"EOS-USDT","settlement_currency":"USDT","is_inverse":false,"contract_val_currency":"EOS"}]

        //String instrumentId = "ETH-USD-SWAP";
        //String size = "1";
        //Double increment = 1.0;
        //获取账户信息
        String accounts = swapUserAPIServive.selectAccount(instrumentId);
        //log.info("获取账户信息{}-{}", instrumentId, JSON.toJSONString(accounts));
        ApiAccountsVO apiAccountsVO = JSON.parseObject(accounts, ApiAccountsVO.class);
        if (apiAccountsVO != null && CollectionUtils.isNotEmpty(apiAccountsVO.getInfo())) {
            for (ApiAccountVO apiAccountVO : apiAccountsVO.getInfo()) {
                log.info("获取账户信息保证金率{}-{}", instrumentId, apiAccountVO.getMargin_ratio());
                if (Double.valueOf(apiAccountVO.getMargin_ratio()) < 0.20) {
                    //转入
                    Transfer transferIn = new Transfer();
                    String currency = instrumentId.substring(0,3).toLowerCase();
                    if (instrumentId.toUpperCase().indexOf("USDT") > 0) {
                        currency = "usdt";

                    }
                    transferIn.setCurrency(currency);
                    transferIn.setFrom(8);
                    transferIn.setTo(9);
                    transferIn.setAmount(BigDecimal.valueOf(transferAmount));
                    JSONObject result =  accountAPIService.transfer("okex", transferIn);
                    log.info("transfer {} {} from financial to swap", transferAmount, JSON.toJSONString(result));
                    weiXinMessageService.sendMessage("划转" + currency.toUpperCase(), "划转" + instrumentId + ", 数量：" + transferAmount);
                }
                if (Double.valueOf(apiAccountVO.getMargin_ratio()) < 0.10) {
                    //停止交易，报警
                    weiXinMessageService.sendMessage("保证金不足10%", "保证金不足10%，" + instrumentId);
                    return;
                }
            }
        }

        //获取部分成交订单
        String waitsell = swapUserAPIServive.selectOrders(instrumentId, "1", null, null, "10");
        //{"order_info":[{"client_oid":"","contract_val":"10","fee":"0.000000","filled_qty":"0","instrument_id":"ETH-USD-SWAP","order_id":"384556031446822912","order_type":"0","price":"100.00","price_avg":"0.00","size":"1","state":"0","status":"0","timestamp":"2019-12-08T10:23:11.315Z","trigger_price":"","type":"1"}]}
        //log.info("获取部分成交订单{}-{}", instrumentId, JSON.toJSONString(waitsell));
        //{"order_info":[]}
        ApiOrderResultVO apiOrderWaitResultVO = JSON.parseObject(waitsell, ApiOrderResultVO.class);
        //取消未成交订单
        if (apiOrderWaitResultVO != null && CollectionUtils.isNotEmpty(apiOrderWaitResultVO.getOrder_info())) {
            log.info("当前持有部分成交订单{}-{}", instrumentId, JSON.toJSONString(waitsell));
            for (ApiOrderResultVO.PerOrderResult perOrderResult : apiOrderWaitResultVO.getOrder_info()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date orderDate = dateFormat.parse(perOrderResult.getTimestamp());
                    if (System.currentTimeMillis() - 30 * 60 * 1000L > orderDate.getTime() ) {
                        swapTradeAPIService.cancelOrder(instrumentId, perOrderResult.getOrder_id());
                        log.info("取消部分成交订单{}-{}", instrumentId, perOrderResult.getOrder_id());
                    }
                } catch (Exception e) {
                    log.error("取消部分成交订单失败{}-{}", instrumentId, perOrderResult.getClient_oid(), e);

                }


            }
            return;
        }
        //获取未成交订单
        String unsell = swapUserAPIServive.selectOrders(instrumentId, "0", null, null, "10");
        //{"order_info":[{"client_oid":"","contract_val":"10","fee":"0.000000","filled_qty":"0","instrument_id":"ETH-USD-SWAP","order_id":"384556031446822912","order_type":"0","price":"100.00","price_avg":"0.00","size":"1","state":"0","status":"0","timestamp":"2019-12-08T10:23:11.315Z","trigger_price":"","type":"1"}]}
        //log.info("获取未成交订单{}-{}", instrumentId, JSON.toJSONString(unsell));
        //{"order_info":[]}
        ApiOrderResultVO apiOrderResultVO = JSON.parseObject(unsell, ApiOrderResultVO.class);
        //取消未成交订单
        if (apiOrderResultVO != null && CollectionUtils.isNotEmpty(apiOrderResultVO.getOrder_info())) {
            for (ApiOrderResultVO.PerOrderResult perOrderResult : apiOrderResultVO.getOrder_info()) {
                swapTradeAPIService.cancelOrder(instrumentId, perOrderResult.getOrder_id());
                log.info("取消未成交订单{}-{}", instrumentId, perOrderResult.getClient_oid());

            }
        }

        String swapTicker = swapMarketAPIService.getTickerApi(instrumentId);
        ApiTickerVO apiTickerVO = JSON.parseObject(swapTicker, ApiTickerVO.class);
        log.info("当前价格{}-{}", instrumentId, apiTickerVO.getLast());

        //获取已成交订单
        String sell = swapUserAPIServive.selectOrders(instrumentId, "2", null, null, "100");
        //log.info("获取已成交订单{}-{}", instrumentId, JSON.toJSONString(sell));

        ApiOrderResultVO sellOrderResultVO = JSON.parseObject(sell, ApiOrderResultVO.class);
        //过滤开空或开多的订单
        ApiOrderResultVO.PerOrderResult lastOrder = null;
        ApiOrderResultVO.PerOrderResult lastUpOrder = null;
        ApiOrderResultVO.PerOrderResult lastDownOrder = null;
        if (sellOrderResultVO != null && CollectionUtils.isNotEmpty(sellOrderResultVO.getOrder_info())) {
            for (ApiOrderResultVO.PerOrderResult perOrderResult : sellOrderResultVO.getOrder_info()) {
                if (perOrderResult.getType().equals("1") || perOrderResult.getType().equals("2")) {
                    if (lastOrder == null) {
                        lastOrder = perOrderResult;
                    }
                    if (perOrderResult.getType().equals("1")) {
                        lastUpOrder = perOrderResult;
                        continue;
                    }
                    if (perOrderResult.getType().equals("2")) {
                        lastDownOrder = perOrderResult;
                        continue;
                    }
                }
            }

        }
        String position =  swapUserAPIServive.getPosition(instrumentId);
        //log.info("获取持仓{}-{}", instrumentId, JSON.toJSONString(position));
        ApiPositionsVO apiPositionsVO = JSON.parseObject(position, ApiPositionsVO.class);
        if (apiPositionsVO != null && !apiPositionsVO.getMargin_mode().equals("crossed")) {//不是全仓
            return;
        }
        ApiPositionVO upPosition = null;
        ApiPositionVO downPosition = null;
        for (ApiPositionVO apiPositionVO : apiPositionsVO.getHolding()) {
            if (apiPositionVO.getSide().equals("long") && Double.valueOf(apiPositionVO.getAvail_position()) > 0) {
                upPosition = apiPositionVO;
                continue;
            }
            if (apiPositionVO.getSide().equals("short") && Double.valueOf(apiPositionVO.getAvail_position()) > 0) {
                downPosition = apiPositionVO;
                continue;
            }

        }
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());
        if (upPosition == null && downPosition == null) {
            //同时开多和空
            PpOrder ppUpOrder = new PpOrder();
            ppUpOrder.setType("1");
            ppUpOrder.setPrice(String.valueOf(currentPrice));
            //ppUpOrder.setSize(size);
            ppUpOrder.setInstrument_id(instrumentId);
            ppUpOrder.setMatch_price("1");
            swapTradeAPIService.order(ppUpOrder);
            log.info("开多{}-{}", instrumentId, JSON.toJSONString(ppUpOrder));

            PpOrder ppDownOrder = new PpOrder();
            ppDownOrder.setType("2");
            ppDownOrder.setPrice(String.valueOf(currentPrice));
            //ppDownOrder.setSize(size);
            ppDownOrder.setInstrument_id(instrumentId);
            ppDownOrder.setMatch_price("1");
            swapTradeAPIService.order(ppDownOrder);
            log.info("开空{}-{}", instrumentId, JSON.toJSONString(ppDownOrder));
            return;

        }
        Double lastPrice = Double.valueOf(lastOrder.getPrice());
        log.info("当前价格：{}, 上次价格:{}", currentPrice, lastPrice);
        if (currentPrice > lastPrice && currentPrice - lastPrice > increment ) {
            //价格上涨
            //获取最新成交多单
            //平多，开空
            if (upPosition != null && lastUpOrder != null) {
                PpOrder ppUpOrder = new PpOrder();
                ppUpOrder.setType("3");
                ppUpOrder.setPrice(String.valueOf(currentPrice));
                ppUpOrder.setSize(size);
                ppUpOrder.setInstrument_id(instrumentId);
                swapTradeAPIService.order(ppUpOrder);
                log.info("平多{}-{}", instrumentId, JSON.toJSONString(ppUpOrder));

            }
            PpOrder ppDownOrder = new PpOrder();
            ppDownOrder.setType("2");
            ppDownOrder.setPrice(String.valueOf(currentPrice));
            ppDownOrder.setSize(size);
            ppDownOrder.setInstrument_id(instrumentId);
            swapTradeAPIService.order(ppDownOrder);
            log.info("开空{}-{}", instrumentId, JSON.toJSONString(ppDownOrder));
            return;

        }
        if (currentPrice < lastPrice && lastPrice - currentPrice > increment ) {
            //价格下跌
            //获取最新成交空单
            //平空，开多
            if (downPosition != null && lastDownOrder != null) {
                PpOrder ppDownOrder = new PpOrder();
                ppDownOrder.setType("4");
                ppDownOrder.setPrice(String.valueOf(currentPrice));
                ppDownOrder.setSize(size);
                ppDownOrder.setInstrument_id(instrumentId);
                swapTradeAPIService.order(ppDownOrder);
                log.info("平空{}-{}", instrumentId, JSON.toJSONString(ppDownOrder));
            }

            PpOrder ppUpOrder = new PpOrder();
            ppUpOrder.setType("1");
            ppUpOrder.setPrice(String.valueOf(currentPrice));
            ppUpOrder.setSize(size);
            ppUpOrder.setInstrument_id(instrumentId);
            swapTradeAPIService.order(ppUpOrder);
            log.info("开多{}-{}", instrumentId, JSON.toJSONString(ppUpOrder));
        }

    }
}
