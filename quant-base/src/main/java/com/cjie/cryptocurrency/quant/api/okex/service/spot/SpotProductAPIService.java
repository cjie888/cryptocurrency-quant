package com.cjie.cryptocurrency.quant.api.okex.service.spot;

import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Book;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Product;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Ticker;
import com.cjie.cryptocurrency.quant.api.okex.bean.spot.result.Trade;
import com.cjie.cryptocurrency.quant.model.CurrencyKline;

import java.math.BigDecimal;
import java.util.List;

public interface SpotProductAPIService {

    /**
     * 单个币对行情
     *
     * @param productId
     * @return
     */
    Ticker getTickerByProductId(String site, String productId);

    /**
     * 行情列表
     *
     * @return
     */
    List<Ticker> getTickers(String site);

    /**
     * @param productId
     * @param size
     * @param depth
     * @return
     */
    Book bookProductsByProductId(String site, String productId, Integer size, BigDecimal depth);

    /**
     * 币对列表
     *
     * @return
     */
    List<Product> getProducts(String site);

    /**
     * 交易列表
     *
     * @param productId
     * @param before
     * @param after
     * @param limit
     * @return
     */
    List<Trade> getTrades(String site, String productId, Integer before, Integer after, Integer limit);

    /**
     * @param product_id
     * @param granularity
     * @param start
     * @param end
     * @return
     */
    List<CurrencyKlineDTO> getCandles(String site, String product_id, Integer granularity, String start, String end) throws Exception;

}
