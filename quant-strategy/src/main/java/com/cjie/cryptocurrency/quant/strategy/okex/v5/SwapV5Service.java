package com.cjie.cryptocurrency.quant.strategy.okex.v5;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cjie.cryptocurrency.quant.api.okex.bean.account.param.Transfer;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.param.PpOrder;
import com.cjie.cryptocurrency.quant.api.okex.bean.swap.result.*;
import com.cjie.cryptocurrency.quant.api.okex.service.account.AccountAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapMarketAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapTradeAPIService;
import com.cjie.cryptocurrency.quant.api.okex.service.swap.SwapUserAPIServive;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.HttpResult;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountDetail;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.AccountInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.account.result.PositionInfo;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.FundsTransfer;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.funding.param.PiggyBankPurchaseRedemption;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.market.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.CancelOrder;
import com.cjie.cryptocurrency.quant.api.okex.v5.bean.trade.param.PlaceOrder;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.account.AccountAPIV5Service;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.funding.FundingAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.marketData.MarketDataAPIService;
import com.cjie.cryptocurrency.quant.api.okex.v5.service.trade.TradeAPIService;
import com.cjie.cryptocurrency.quant.mapper.SpotOrderMapper;
import com.cjie.cryptocurrency.quant.mapper.SwapOrderMapper;
import com.cjie.cryptocurrency.quant.model.SpotOrder;
import com.cjie.cryptocurrency.quant.model.SwapOrder;
import com.cjie.cryptocurrency.quant.service.MessageService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SwapV5Service {

    @Autowired
    private MarketDataAPIService marketDataAPIService;

    @Autowired
    private AccountAPIV5Service accountAPIV5Service;

    @Autowired
    private TradeAPIService tradeAPIService;

    @Autowired
    private FundingAPIService fundingAPIService;

    @Autowired
    @Qualifier("telegramMessageServiceImpl")
    private MessageService messageService;

    @Autowired
    private SwapOrderMapper swapOrderMapper;

    @Autowired
    private SpotOrderMapper spotOrderMapper;

    @Autowired
    private AccountAPIV5Service accountAPIService;

    private Map<String,Double> ranges = new ConcurrentHashMap<>();

    private Map<String,Double> opens = new ConcurrentHashMap<>();

    private Map<String,LocalDateTime>  lastDates = new ConcurrentHashMap<>();

    private static Map<String, Integer> STATES = Maps.newHashMap();

    private static Map<String, BigDecimal> swapCtVal = Maps.newHashMap();

    static {
//        canceled：撤单成功
//        live：等待成交
//        partially_filled：部分成交
//        filled：完全成交

        STATES.put("live", 0);
        STATES.put("partially_filled", 1);
        STATES.put("filled", 2);
        STATES.put("canceled", -1);

        swapCtVal.put("SOL-USDT-SWAP", new BigDecimal("1"));
        swapCtVal.put("BTC-USDT-SWAP", new BigDecimal("0.01"));
        swapCtVal.put("ETH-USDT-SWAP", new BigDecimal("0.1"));
        swapCtVal.put("XRP-USDT-SWAP", new BigDecimal("100"));
        swapCtVal.put("DOGE-USDT-SWAP", new BigDecimal("1000"));
        swapCtVal.put("ADA-USDT-SWAP", new BigDecimal("100"));
        swapCtVal.put("SUI-USDT-SWAP", new BigDecimal("1"));
        swapCtVal.put("LINK-USDT-SWAP", new BigDecimal("1"));
        swapCtVal.put("AVAX-USDT-SWAP", new BigDecimal("1"));
        swapCtVal.put("XLM-USDT-SWAP", new BigDecimal("100"));

    }




    public  boolean transfer(String site,String instrumentId, Double transferAmount) {
        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, null, instrumentId, null);
        //log.info("获取账户信息{}-{}", instrumentId, JSON.toJSONString(accounts));
        if (positionsResult != null && "0".equals(positionsResult.getCode()) && CollectionUtils.isNotEmpty(positionsResult.getData())) {
            for (PositionInfo positionInfo : positionsResult.getData()) {
                log.info("获取账户信息保证金率{}-{}", instrumentId, positionInfo.getMgnRatio());
                if (StringUtils.isNotBlank(positionInfo.getMgnRatio()) && Double.valueOf(positionInfo.getMgnRatio()) < 100) {


                    FundsTransfer transferIn = new FundsTransfer();

                    String currency = instrumentId.substring(0,instrumentId.indexOf("-"));
                    if (instrumentId.toUpperCase().indexOf("USDT") > 0) {
                        //transferIn.setToInstId(currency + "-" + "USDT");
                        currency = "USDT";
                    }
                    transferIn.setCcy(currency);
                    transferIn.setFrom("6");
                    transferIn.setTo("18");
                    transferIn.setAmt(String.valueOf(transferAmount));
                    try {

                        //赎回
                        try {
                            PiggyBankPurchaseRedemption piggyBankPurchaseRedemption = new PiggyBankPurchaseRedemption();
                            piggyBankPurchaseRedemption.setCcy(currency);
                            piggyBankPurchaseRedemption.setAmt(String.valueOf(transferAmount));
                            piggyBankPurchaseRedemption.setSide("redempt");
                            JSONObject result1 = fundingAPIService.piggyBankPurchaseRedemption(site, piggyBankPurchaseRedemption);
                            log.info("transfer {} {} from financial to asset", transferAmount, JSON.toJSONString(result1));
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            //ignore
                            log.error("transfer {} {} from financial to asset error", instrumentId, transferAmount, e);
                        }
                        //转入
                        JSONObject transferResult = fundingAPIService.fundsTransfer(site, transferIn);
                        log.info("transfer {} {} from asset to swap", transferAmount, JSON.toJSONString(transferResult));
                        //weiXinMessageService.sendMessage("划转" + currency.toUpperCase(), "划转" + instrumentId + ", 数量：" + transferAmount);
                    } catch (Exception e) {
                        log.error("transfer {} {} from financial to swap error", instrumentId, transferAmount, e);

                        try {
                            Thread.sleep(2000);
//                            transferIn.setFrom(5);
//                            transferIn.setInstrument_id(currency + "-usdt");
//                            JSONObject result = accountAPIService.transfer("okexsub1", transferIn);
//                            log.info("transfer {} {} from spot margin to swap", transferAmount, JSON.toJSONString(result));
//                           // weiXinMessageService.sendMessage("划转" + currency.toUpperCase() + "币币杠杆", "划转" + instrumentId + ", 数量：" + transferAmount);
                        } catch (Exception e1) {
//                            log.error("transfer {} {} from spot margin to swap error", instrumentId, transferAmount, e1);
//
                        }
                    }
                }
                if (StringUtils.isNotBlank(positionInfo.getMgnRatio()) && Double.valueOf(positionInfo.getMgnRatio()) < 1) {
                    //停止交易，报警
                    messageService.sendMessage("保证金不足100%", "保证金不足100%，" + instrumentId);
                    return false;
                }
            }
        }
        return true;
    }


    public void swapAndSpotHedging(String site, String instrumentId, String symbol, Double increment, int size) {
        //获取等待提交订单
        List<Integer> unProcessedStatuses = new ArrayList<>();
        unProcessedStatuses.add(99);
        unProcessedStatuses.add(0);
        unProcessedStatuses.add(1);
        try {
            List<SwapOrder> swapOrders = swapOrderMapper.selectByStatus(instrumentId, "swapAndSpotHedging", unProcessedStatuses);
            if (CollectionUtils.isNotEmpty(swapOrders)) {
                log.info("unprocessed orders {}", JSON.toJSONString(swapOrders));
                for (SwapOrder swapOrder : swapOrders) {
                    JSONObject result = tradeAPIService.getOrderDetails(site, instrumentId, swapOrder.getOrderId(), null);

                    log.info("spot order status {}", JSON.toJSONString(result));
                    if (result == null) {
                        return;
                    }
                    String state = ((JSONObject)result.getJSONArray("data").get(0)).getString("state");
                    if ( state == null || STATES.get(state) == null) {
                        return;
                    }
                    Integer status = STATES.get(state);
                    if (!swapOrder.getStatus().equals(status)) {
                        swapOrderMapper.updateStatus(swapOrder.getOrderId(), status);
                    }
                }
            }
        } catch (Exception e) {
            log.info("update status error, instrumentId:{}", instrumentId, e);
            return;
        }

        List<Integer> unSettledStatuses = new ArrayList<>();
        unSettledStatuses.add(1);
        List<SwapOrder> unSettledOrders = swapOrderMapper.selectByStatus(instrumentId, "swapAndSpotHedging", unSettledStatuses);
        if (CollectionUtils.isNotEmpty(unSettledOrders)) {
            for (SwapOrder swapOrder : unSettledOrders) {
                if (System.currentTimeMillis() - 30 * 60 * 1000L > swapOrder.getCreateTime().getTime() ) {
                    CancelOrder cancelOrder = new CancelOrder();
                    cancelOrder.setInstId(instrumentId);
                    cancelOrder.setOrdId(swapOrder.getOrderId());
                    tradeAPIService.cancelOrder(site, cancelOrder);
                    log.info("取消部分成交订单{}-{}", instrumentId, swapOrder.getOrderId());
                }
            }
            return;
        }


        List<Integer> unSelledStatuses = new ArrayList<>();
        unSelledStatuses.add(0);
        List<SwapOrder> unSelledOrders = swapOrderMapper.selectByStatus(instrumentId, "swapAndSpotHedging", unSelledStatuses);
        if (CollectionUtils.isNotEmpty(unSelledOrders)) {
            for (SwapOrder swapOrder : unSelledOrders) {
                CancelOrder cancelOrder = new CancelOrder();
                cancelOrder.setInstId(instrumentId);
                cancelOrder.setOrdId(swapOrder.getOrderId());
                tradeAPIService.cancelOrder(site, cancelOrder);
                log.info("取消未成交订单{}-{}", instrumentId, swapOrder.getOrderId());
            }
        }

        try {
            List<SpotOrder> spotOrders = spotOrderMapper.selectByStatus(symbol, "swapAndSpotHedging", unProcessedStatuses);
            if (CollectionUtils.isNotEmpty(spotOrders)) {
                log.info("unprocessed spot orders {}", JSON.toJSONString(spotOrders));
                for (SpotOrder spotOrder : spotOrders) {
                    JSONObject result = tradeAPIService.getOrderDetails(site, symbol, spotOrder.getOrderId(), null);

                    log.info("spot order status {}", JSON.toJSONString(result));
                    if (result == null) {
                        return;
                    }
                    String state = ((JSONObject) result.getJSONArray("data").get(0)).getString("state");
                    if (state == null || STATES.get(state) == null) {
                        return;
                    }
                    Integer status = STATES.get(state);
                    if (!spotOrder.getStatus().equals(status)) {
                        spotOrderMapper.updateStatus(spotOrder.getOrderId(), status);
                    }
                }
            }
        } catch (Exception e) {
            log.info("update status error, symbol:{}", symbol, e);
            return;
        }

        unSettledStatuses.clear();
        unSettledStatuses.add(1);
        List<SpotOrder> unSettledSpotOrders = spotOrderMapper.selectByStatus(symbol, "swapAndSpotHedging", unSettledStatuses);
        if (CollectionUtils.isNotEmpty(unSettledSpotOrders)) {
            for (SpotOrder spotOrder : unSettledSpotOrders) {
                if (System.currentTimeMillis() - 30 * 60 * 1000L > spotOrder.getCreateTime().getTime()) {
                    CancelOrder cancelOrder = new CancelOrder();
                    cancelOrder.setInstId(symbol);
                    cancelOrder.setOrdId(spotOrder.getOrderId());
                    tradeAPIService.cancelOrder(site, cancelOrder);
                    log.info("取消部分成交订单{}-{}", symbol, spotOrder.getOrderId());
                }
            }
            return;
        }
        unSelledStatuses.clear();
        unSelledStatuses.add(0);
        List<SpotOrder> unSelledSpotOrders = spotOrderMapper.selectByStatus(symbol, "swapAndSpotHedging", unSelledStatuses);
        if (CollectionUtils.isNotEmpty(unSelledSpotOrders)) {
            for (SpotOrder spotOrder : unSelledSpotOrders) {
                CancelOrder cancelOrder = new CancelOrder();
                cancelOrder.setInstId(symbol);
                cancelOrder.setOrdId(spotOrder.getOrderId());
                tradeAPIService.cancelOrder(site, cancelOrder);
                log.info("取消未成交订单{}-{}", symbol, spotOrder.getOrderId());
            }
        }


        HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, instrumentId);

        if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
            return;
        }

        SwapOrder lastOrder = null;
        List<Integer> selledStatuses = new ArrayList<>();
        selledStatuses.add(2);
        List<SwapOrder> selledOrders = swapOrderMapper.selectByStatus(instrumentId, "swapAndSpotHedging", selledStatuses);
        if (CollectionUtils.isNotEmpty(selledOrders)) {
            for (SwapOrder swapOrder : selledOrders) {
                if (swapOrder.getType() == 2) {
                    if (lastOrder == null) {
                        lastOrder = swapOrder;
                    }
                }
            }
        }


        SpotOrder lastSportOrder = null;
        List<SpotOrder> selledSpotOrders = spotOrderMapper.selectByStatus(symbol, "swapAndSpotHedging", selledStatuses);
        if (CollectionUtils.isNotEmpty(selledSpotOrders)) {
            for (SpotOrder spotOrder : selledSpotOrders) {
                if (lastSportOrder == null) {
                    lastSportOrder = spotOrder;
                    break;
                }
            }
        }
        Ticker apiTickerVO = swapTicker.getData().get(0);
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());


        if (lastOrder == null || lastSportOrder == null) {
            //合约开空 现货买入
            PlaceOrder ppDownOrder = new PlaceOrder();
            ppDownOrder.setInstId(instrumentId);
            ppDownOrder.setTdMode("cross");
            ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
            ppDownOrder.setSz(String.valueOf(size));
            ppDownOrder.setSide("sell");
            ppDownOrder.setOrdType("market");
            ppDownOrder.setPosSide("short");
            ppDownOrder.setType("2");
            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "swapAndSpotHedging");
            messageService.sendStrategyMessage("swapAndSpotHedging合约开空", "swapAndSpotHedging合约开空-instId:" + instrumentId+ ",price:" + currentPrice);
            log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
            BigDecimal spotSize = new BigDecimal("1.005").multiply(new BigDecimal(size)).multiply(swapCtVal.get(instrumentId));

            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(symbol);
            placeOrderParam.setTdMode("cash");
            placeOrderParam.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
            placeOrderParam.setSz(spotSize.toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setTgtCcy("base_ccy");
            placeOrderParam.setOrdType("market");
            orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
            log.info("买入{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));
            messageService.sendStrategyMessage("swapAndSpotHedging现货买入", "swapAndSpotHedging现货买入-instId:" + symbol + ",price:" + currentPrice);
            if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("swapAndSpotHedging");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(apiTickerVO.getLast()));
                spotOrder.setSize(spotSize);
                spotOrder.setOrderId(String.valueOf(((JSONObject) orderResult.getJSONArray("data").get(0)).getString("ordId")));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }
            return;
        }
        Double lastPrice = lastOrder.getPrice().doubleValue();
        if (lastSportOrder.getCreateTime().after(lastOrder.getCreateTime())) {
            lastPrice = lastSportOrder.getPrice().doubleValue();
        }
        log.info("当前价格{}:{},上次价格:{}", instrumentId, apiTickerVO.getLast(), lastPrice);
        String baseCurrency = symbol.substring(0, symbol.indexOf("-"));


        //价格上涨
        if (currentPrice > lastPrice && currentPrice - lastPrice > lastPrice * increment * 1.05 ) {
            //合约做空，现货卖出
            BigDecimal spotSize = new BigDecimal(size).multiply(swapCtVal.get(instrumentId));

            HttpResult<List<AccountInfo>> baseAccountResult = accountAPIService.getBalance(site, baseCurrency);
            log.info("base account:{}", JSON.toJSONString(baseAccountResult));
            if (Objects.nonNull(baseAccountResult) && "0".equals(baseAccountResult.getCode()) && baseAccountResult.getData().get(0).getDetails().size() > 0) {
                AccountDetail baseAccountDetail = baseAccountResult.getData().get(0).getDetails().get(0);
                if (Double.parseDouble(baseAccountDetail.getAvailEq()) >= spotSize.doubleValue()) {
                    PlaceOrder placeOrderParam = new PlaceOrder();
                    placeOrderParam.setInstId(symbol);
                    placeOrderParam.setTdMode("cross");
                    //placeOrderParam.setPx(spotTicker.getLast());
                    placeOrderParam.setSz(spotSize.toPlainString());
                    placeOrderParam.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());

                    placeOrderParam.setSide("sell");
                    placeOrderParam.setOrdType("market");
                    placeOrderParam.setTgtCcy("base_ccy");
                    JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
                    log.info("卖出{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
                    messageService.sendStrategyMessage("swapAndSpotHedging现货卖出", "swapAndSpotHedging现货卖出-instId:" + symbol+ ",price:" + currentPrice);
                    if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                        SpotOrder spotOrder = new SpotOrder();
                        spotOrder.setSymbol(symbol);
                        spotOrder.setCreateTime(new Date());
                        spotOrder.setStrategy("swapAndSpotHedging");
                        spotOrder.setIsMock(Byte.valueOf("0"));
                        spotOrder.setType(Byte.valueOf("2"));
                        spotOrder.setPrice(new BigDecimal(apiTickerVO.getLast()));
                        spotOrder.setSize(spotSize);
                        spotOrder.setOrderId(String.valueOf(((JSONObject) orderResult.getJSONArray("data").get(0)).getString("ordId")));
                        spotOrder.setStatus(99);
                        spotOrderMapper.insert(spotOrder);
                    }
                }
            }

            PlaceOrder ppDownOrder = new PlaceOrder();
            ppDownOrder.setInstId(instrumentId);
            ppDownOrder.setTdMode("cross");
            ppDownOrder.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
            ppDownOrder.setSz(String.valueOf(size));
            ppDownOrder.setSide("sell");
            ppDownOrder.setOrdType("market");
            ppDownOrder.setPosSide("short");
            ppDownOrder.setType("2");
            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, ppDownOrder, "swapAndSpotHedging");

            log.info("合约开空 {}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSONObject.toJSONString(orderResult));
            messageService.sendStrategyMessage("swapAndSpotHedging合约开空", "swapAndSpotHedging合约开空-instId:" + instrumentId+ ",price:" + currentPrice);

            return;

        }

        if (currentPrice < lastPrice && lastPrice - currentPrice > lastPrice * increment ) {
            //合约平空，现货买入
            HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, null, instrumentId, null);
            if (positionsResult == null || !positionsResult.getCode().equals("0")) {
                return;
            }
            PositionInfo downPosition = null;
            double shortPosition = 0;
            for (PositionInfo apiPositionVO : positionsResult.getData()) {
                if (apiPositionVO.getAvailPos().equals("")) {
                    continue;
                }
                if (apiPositionVO.getPosSide().equals("short")&& Double.valueOf(apiPositionVO.getPos()) >= Double.valueOf(size) && Double.valueOf(apiPositionVO.getAvailPos()) >= Double.valueOf(size)) {
                    downPosition = apiPositionVO;
                    shortPosition = Double.valueOf(apiPositionVO.getPos());
                }

            }
            log.info("持仓{}-空{}", instrumentId, shortPosition);
            if (downPosition != null && shortPosition >= size) {
                PlaceOrder placeOrderParam = new PlaceOrder();
                placeOrderParam.setInstId(instrumentId);
                placeOrderParam.setTdMode("cross");
                placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                placeOrderParam.setSz(String.valueOf(size));
                placeOrderParam.setSide("buy");
                placeOrderParam.setOrdType("market");
                placeOrderParam.setPosSide("short");
                placeOrderParam.setType("4");
//                placeOrderParam.setTgtCcy("base_ccy");
//                orders.add(placeOrderParam);
                JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "swapAndSpotHedging");

                log.info("平空{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
                messageService.sendStrategyMessage("swapAndSpotHedging合约平空", "swapAndSpotHedging合约平空-instId:" + instrumentId+ ",price:" + currentPrice);

            }

            BigDecimal spotSize = new BigDecimal("1.005").multiply(new BigDecimal(size)).multiply(swapCtVal.get(instrumentId));
            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(symbol);
            placeOrderParam.setTdMode("cross");
            placeOrderParam.setPx(new BigDecimal(apiTickerVO.getLast()).toPlainString());
            placeOrderParam.setSz(spotSize.toPlainString());
            placeOrderParam.setSide("buy");
            placeOrderParam.setTgtCcy("base_ccy");
            placeOrderParam.setOrdType("market");
            JSONObject orderResult = tradeAPIService.placeOrder(site, placeOrderParam);
            log.info("买入{}-{},result:{}", symbol, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));
            messageService.sendStrategyMessage("swapAndSpotHedging现货买入", "swapAndSpotHedging现货买入-instId:" + symbol+ ",price:" + currentPrice);
            if (orderResult.getString("code") != null && orderResult.getString("code").equals("0")) {

                SpotOrder spotOrder = new SpotOrder();
                spotOrder.setSymbol(symbol);
                spotOrder.setCreateTime(new Date());
                spotOrder.setStrategy("swapAndSpotHedging");
                spotOrder.setIsMock(Byte.valueOf("0"));
                spotOrder.setType(Byte.valueOf("1"));
                spotOrder.setPrice(new BigDecimal(apiTickerVO.getLast()));
                spotOrder.setSize(spotSize);
                spotOrder.setOrderId(String.valueOf(((JSONObject) orderResult.getJSONArray("data").get(0)).getString("ordId")));
                spotOrder.setStatus(99);
                spotOrderMapper.insert(spotOrder);
            }
        }
    }


    public void netGrid(String site, String instrumentId, String size, Double increment, Double transferAmount, double origin, double min) {

        //System.out.println(swapMarketAPIService.getContractsApi());
        //[{"instrument_id":"BTC-USD-SWAP","underlying_index":"BTC","quote_currency":"USD","coin":"BTC","contract_val":"100","listing":"2018-08-28T02:43:23.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.1","base_currency":"BTC","underlying":"BTC-USD","settlement_currency":"BTC","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"LTC-USD-SWAP","underlying_index":"LTC","quote_currency":"USD","coin":"LTC","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"LTC","underlying":"LTC-USD","settlement_currency":"LTC","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"ETH-USD-SWAP","underlying_index":"ETH","quote_currency":"USD","coin":"ETH","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"ETH","underlying":"ETH-USD","settlement_currency":"ETH","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"ETC-USD-SWAP","underlying_index":"ETC","quote_currency":"USD","coin":"ETC","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.001","base_currency":"ETC","underlying":"ETC-USD","settlement_currency":"ETC","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"XRP-USD-SWAP","underlying_index":"XRP","quote_currency":"USD","coin":"XRP","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.00010","base_currency":"XRP","underlying":"XRP-USD","settlement_currency":"XRP","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"EOS-USD-SWAP","underlying_index":"EOS","quote_currency":"USD","coin":"EOS","contract_val":"10","listing":"2018-12-10T11:55:31.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.001","base_currency":"EOS","underlying":"EOS-USD","settlement_currency":"EOS","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"BCH-USD-SWAP","underlying_index":"BCH","quote_currency":"USD","coin":"BCH","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"BCH","underlying":"BCH-USD","settlement_currency":"BCH","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"BSV-USD-SWAP","underlying_index":"BSV","quote_currency":"USD","coin":"BSV","contract_val":"10","listing":"2018-12-21T07:53:47.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"BSV","underlying":"BSV-USD","settlement_currency":"BSV","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"TRX-USD-SWAP","underlying_index":"TRX","quote_currency":"USD","coin":"TRX","contract_val":"10","listing":"2019-01-16T04:09:23.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.000010","base_currency":"TRX","underlying":"TRX-USD","settlement_currency":"TRX","is_inverse":true,"contract_val_currency":"USD"},{"instrument_id":"BTC-USDT-SWAP","underlying_index":"BTC","quote_currency":"USDT","coin":"USDT","contract_val":"0.0001","listing":"2019-11-12T11:16:48.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.1","base_currency":"BTC","underlying":"BTC-USDT","settlement_currency":"USDT","is_inverse":false,"contract_val_currency":"BTC"},{"instrument_id":"ETH-USDT-SWAP","underlying_index":"ETH","quote_currency":"USDT","coin":"USDT","contract_val":"0.001","listing":"2019-11-12T11:16:48.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.01","base_currency":"ETH","underlying":"ETH-USDT","settlement_currency":"USDT","is_inverse":false,"contract_val_currency":"ETH"},{"instrument_id":"EOS-USDT-SWAP","underlying_index":"EOS","quote_currency":"USDT","coin":"USDT","contract_val":"0.1","listing":"2019-11-12T11:16:48.000Z","delivery":"2019-12-09T08:00:00.000Z","size_increment":"1","tick_size":"0.001","base_currency":"EOS","underlying":"EOS-USDT","settlement_currency":"USDT","is_inverse":false,"contract_val_currency":"EOS"}]

        //String instrumentId = "ETH-USD-SWAP";
        //String size = "1";
        //Double increment = 1.0;
        //获取账户信息
        if (!transfer(site, instrumentId, transferAmount)) {
            return;
        }

        //获取等待提交订单
        List<Integer> unProcessedStatuses = new ArrayList<>();
        unProcessedStatuses.add(99);
        unProcessedStatuses.add(0);
        unProcessedStatuses.add(1);
        try {
            List<SwapOrder> swapOrders = swapOrderMapper.selectByStatus(instrumentId, "netGrid", unProcessedStatuses);
            if (CollectionUtils.isNotEmpty(swapOrders)) {
                log.info("unprocessed orders {}", JSON.toJSONString(swapOrders));
                for (SwapOrder swapOrder : swapOrders) {
                    JSONObject result = tradeAPIService.getOrderDetails(site, instrumentId, swapOrder.getOrderId(), null);

                    log.info("spot order status {}", JSON.toJSONString(result));
                    if (result == null) {
                        return;
                    }
                    String state = ((JSONObject)result.getJSONArray("data").get(0)).getString("state");
                    if ( state == null || STATES.get(state) == null) {
                        return;
                    }
                    Integer status = STATES.get(state);
                    if (!swapOrder.getStatus().equals(status)) {
                        swapOrderMapper.updateStatus(swapOrder.getOrderId(), status);
                    }
                }
            }
        } catch (Exception e) {
            log.info("update status error, instrumentId:{}", instrumentId, e);
            return;
        }


        List<Integer> unSettledStatuses = new ArrayList<>();
        unSettledStatuses.add(1);
        List<SwapOrder> unSettledOrders = swapOrderMapper.selectByStatus(instrumentId, "netGrid", unSettledStatuses);
        if (CollectionUtils.isNotEmpty(unSettledOrders)) {
            for (SwapOrder swapOrder : unSettledOrders) {
                if (System.currentTimeMillis() - 30 * 60 * 1000L > swapOrder.getCreateTime().getTime() ) {
                    CancelOrder cancelOrder = new CancelOrder();
                    cancelOrder.setInstId(instrumentId);
                    cancelOrder.setOrdId(swapOrder.getOrderId());
                    tradeAPIService.cancelOrder(site, cancelOrder);
                    log.info("取消部分成交订单{}-{}", instrumentId, swapOrder.getOrderId());
                }
            }
            return;
        }


        List<Integer> unSelledStatuses = new ArrayList<>();
        unSelledStatuses.add(0);
        List<SwapOrder> unSelledOrders = swapOrderMapper.selectByStatus(instrumentId, "netGrid", unSelledStatuses);
        if (CollectionUtils.isNotEmpty(unSelledOrders)) {
            for (SwapOrder swapOrder : unSelledOrders) {
                CancelOrder cancelOrder = new CancelOrder();
                cancelOrder.setInstId(instrumentId);
                cancelOrder.setOrdId(swapOrder.getOrderId());
                tradeAPIService.cancelOrder(site, cancelOrder);
                log.info("取消未成交订单{}-{}", instrumentId, swapOrder.getOrderId());
            }
        }

        HttpResult<List<Ticker>> swapTicker = marketDataAPIService.getTicker(site, instrumentId);

        if (!"0".equals(swapTicker.getCode()) || swapTicker.getData().size() == 0) {
            return;
        }

        SwapOrder lastOrder = null;
        SwapOrder lastUpOrder = null;
        SwapOrder lastDownOrder = null;
        List<Integer> selledStatuses = new ArrayList<>();
        selledStatuses.add(2);
        List<SwapOrder> selledOrders = swapOrderMapper.selectByStatus(instrumentId, "netGrid", selledStatuses);
        if (CollectionUtils.isNotEmpty(selledOrders)) {
            for (SwapOrder swapOrder : selledOrders) {
                if (swapOrder.getType() == 1 || swapOrder.getType() == 2) {
                    if (lastOrder == null) {
                        lastOrder = swapOrder;
                    }
                    if (swapOrder.getType() == 1) {
                        lastUpOrder = swapOrder;
                        continue;
                    }
                    if (swapOrder.getType() == 2) {
                        lastDownOrder = swapOrder;
                        continue;
                    }
                }
            }
        }

        HttpResult<List<PositionInfo>> positionsResult = accountAPIV5Service.getPositions(site, null, instrumentId, null);
        Ticker apiTickerVO = swapTicker.getData().get(0);

        log.info("当前价格{}-{},size:{}", instrumentId, apiTickerVO.getLast(), size);
        if (positionsResult == null || !positionsResult.getCode().equals("0")
               // || positionsResult.getData().size() > 0 && !positionsResult.getData().get(0).getMgnMode().equals("cross")
        ) {//不是全仓
            return;
        }
        PositionInfo upPosition = null;
        PositionInfo downPosition = null;
        double longPosition = 0;
        double shortPosition = 0;
        for (PositionInfo apiPositionVO : positionsResult.getData()) {
            if (apiPositionVO.getAvailPos().equals("")) {
                continue;
            }
            if (apiPositionVO.getPosSide().equals("long") && Double.valueOf(apiPositionVO.getPos()) >= Double.valueOf(size) && Double.valueOf(apiPositionVO.getAvailPos()) >= Double.valueOf(size)) {
                upPosition = apiPositionVO;
                longPosition = Double.valueOf(apiPositionVO.getPos());
                continue;
            }
            if (apiPositionVO.getPosSide().equals("short")&& Double.valueOf(apiPositionVO.getPos()) >= Double.valueOf(size) && Double.valueOf(apiPositionVO.getAvailPos()) >= Double.valueOf(size)) {
                downPosition = apiPositionVO;
                shortPosition = Double.valueOf(apiPositionVO.getPos());
            }

        }
        log.info("持仓{}多{}-空{}", instrumentId, longPosition, shortPosition);
        Double currentPrice = Double.valueOf(apiTickerVO.getLast());
        if (currentPrice < min) {
            return;
        }

        if (upPosition == null && downPosition == null || lastOrder == null) {
            //同时开多和空
            List<PlaceOrder> orders = new ArrayList<>();
            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(instrumentId);
            placeOrderParam.setTdMode("cross");
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
            placeOrderParam.setSz(size);
            placeOrderParam.setSide("buy");
            placeOrderParam.setOrdType("limit");
            placeOrderParam.setPosSide("long");
            placeOrderParam.setType("1");
//            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
            orders.add(placeOrderParam);
//            log.info("开多{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSONObject.toJSONString(orderResult));

            PlaceOrder ppDownOrder = new PlaceOrder();
            ppDownOrder.setInstId(instrumentId);
            ppDownOrder.setTdMode("cross");
            ppDownOrder.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
            ppDownOrder.setSz(size);
            ppDownOrder.setSide("sell");
            ppDownOrder.setOrdType("limit");
            ppDownOrder.setPosSide("short");
            ppDownOrder.setType("2");
            orders.add(ppDownOrder);
            JSONObject orderResult = tradeAPIService.placeMultipleOrders(site, orders);

            log.info("开多-开空 {}-{},result:{}", instrumentId, JSON.toJSONString(orders), JSONObject.toJSONString(orderResult));

//            JSONObject orderResult2 = tradeAPIService.placeSwapOrder(site, ppDownOrder, "netGrid");
//            log.info("开空{}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSON.toJSONString(orderResult2));
            return;

        }
        if (longPosition + shortPosition >= 50 && Math.abs(longPosition-shortPosition) <=1) {
            if (longPosition > shortPosition) {
                //平多
                List<PlaceOrder> orders = new ArrayList<>();
                PlaceOrder placeOrderParam = new PlaceOrder();
                placeOrderParam.setInstId(instrumentId);
                placeOrderParam.setTdMode("cross");
                placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                placeOrderParam.setSz(String.valueOf(2* Long.parseLong(size)));
                placeOrderParam.setSide("sell");
                placeOrderParam.setOrdType("limit");
                placeOrderParam.setPosSide("long");
                placeOrderParam.setType("3");
                orders.add(placeOrderParam);
//                JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");

//                log.info("平多{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
                //平空
                PlaceOrder ppDownOrder = new PlaceOrder();
                ppDownOrder.setInstId(instrumentId);
                ppDownOrder.setTdMode("cross");
                ppDownOrder.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                ppDownOrder.setSz(String.valueOf(2* Long.parseLong(size)));
                ppDownOrder.setSide("buy");
                ppDownOrder.setOrdType("limit");
                ppDownOrder.setPosSide("short");
                ppDownOrder.setType("4");
//                JSONObject orderResult2 = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
//                log.info("平空{}-{},result:{}", instrumentId, JSON.toJSONString(ppDownOrder), JSON.toJSONString(orderResult2));
                orders.add(ppDownOrder);

                JSONObject orderResult = tradeAPIService.placeMultipleOrders(site, orders);
                log.info("平多-平空 {}-{},result:{}", instrumentId, JSON.toJSONString(orders), JSONObject.toJSONString(orderResult));

            } else {
                //平空
                List<PlaceOrder> orders = new ArrayList<>();
                PlaceOrder placeOrderParam = new PlaceOrder();
                placeOrderParam.setInstId(instrumentId);
                placeOrderParam.setTdMode("cross");
                placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                placeOrderParam.setSz(String.valueOf(2* Long.parseLong(size)));
                placeOrderParam.setSide("buy");
                placeOrderParam.setOrdType("limit");
                placeOrderParam.setPosSide("short");
                placeOrderParam.setType("4");
                orders.add(placeOrderParam);
//                JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
//                log.info("平空{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));

                //平多
                PlaceOrder ppUpOrder = new PlaceOrder();
                ppUpOrder.setInstId(instrumentId);
                ppUpOrder.setTdMode("cross");
                ppUpOrder.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                ppUpOrder.setSz(String.valueOf(2* Long.parseLong(size)));
                ppUpOrder.setSide("sell");
                ppUpOrder.setOrdType("limit");
                ppUpOrder.setPosSide("long");
                ppUpOrder.setType("3");
                orders.add(ppUpOrder);
//                JSONObject orderResult2 = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
//                log.info("平多{}-{},result:{}", instrumentId, JSON.toJSONString(ppUpOrder), JSON.toJSONString(orderResult2));
                JSONObject orderResult = tradeAPIService.placeMultipleOrders(site, orders);
                log.info("平空-平多 {}-{},result:{}", instrumentId, JSON.toJSONString(orders), JSONObject.toJSONString(orderResult));

            }
        }
        Double lastPrice = lastOrder.getPrice().doubleValue();
        log.info("当前价格：{}, 上次价格:{}", currentPrice, lastPrice);
        if (currentPrice > lastPrice && currentPrice - lastPrice > lastPrice * increment * 1.05 ) {
            //价格上涨
            //获取最新成交多单
            //平多，开空
//            List<PlaceOrder> orders = new ArrayList<>();
            if (upPosition != null && lastUpOrder != null) {
                PlaceOrder placeOrderParam = new PlaceOrder();
                placeOrderParam.setInstId(instrumentId);
                placeOrderParam.setTdMode("cross");
                placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                placeOrderParam.setSz(size);
                placeOrderParam.setSide("sell");
                placeOrderParam.setOrdType("limit");
                placeOrderParam.setPosSide("long");
                placeOrderParam.setType("3");
//                orders.add(placeOrderParam);
                JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
                log.info("平多{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));

            }
            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(instrumentId);
            placeOrderParam.setTdMode("cross");
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
            placeOrderParam.setSz(size);
            placeOrderParam.setSide("sell");
            placeOrderParam.setOrdType("limit");
            placeOrderParam.setPosSide("short");
            placeOrderParam.setType("2");
//            orders.add(placeOrderParam);
            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
            log.info("开空{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
//            JSONObject orderResult = tradeAPIService.placeMultipleOrders(site, orders);
//            log.info("平多-开空 {}-{},result:{}", instrumentId, JSON.toJSONString(orders), JSONObject.toJSONString(orderResult));

            return;

        }
        if (currentPrice < lastPrice && lastPrice - currentPrice > lastPrice * increment ) {
            //价格下跌
            //获取最新成交空单
            //平空，开多
//            List<PlaceOrder> orders = new ArrayList<>();
            if (downPosition != null && lastDownOrder != null) {
                PlaceOrder placeOrderParam = new PlaceOrder();
                placeOrderParam.setInstId(instrumentId);
                placeOrderParam.setTdMode("cross");
                placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
                placeOrderParam.setSz(size);
                placeOrderParam.setSide("buy");
                placeOrderParam.setOrdType("limit");
                placeOrderParam.setPosSide("short");
                placeOrderParam.setType("4");
//                orders.add(placeOrderParam);
                JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");

                log.info("平空{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
            }

            PlaceOrder placeOrderParam = new PlaceOrder();
            placeOrderParam.setInstId(instrumentId);
            placeOrderParam.setTdMode("cross");
            placeOrderParam.setPx(String.valueOf(Double.parseDouble(apiTickerVO.getLast())));
            placeOrderParam.setSz(size);
            placeOrderParam.setSide("buy");
            placeOrderParam.setOrdType("limit");
            placeOrderParam.setPosSide("long");
            placeOrderParam.setType("1");
//            orders.add(placeOrderParam);
            JSONObject orderResult = tradeAPIService.placeSwapOrder(site, placeOrderParam, "netGrid");
            log.info("开多{}-{},result:{}", instrumentId, JSON.toJSONString(placeOrderParam), JSON.toJSONString(orderResult));
//            JSONObject orderResult = tradeAPIService.placeMultipleOrders(site, orders);
//            log.info("平空-开多 {}-{},result:{}", instrumentId, JSON.toJSONString(orders), JSONObject.toJSONString(orderResult));

        }

    }
}
