package com.cjie.cryptocurrency.quant.api.okex.bean.futures.result;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * futures contract products <br/>
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/2/26 10:49
 */
@Getter
@Setter
public class Instrument {
    private Long id;

    private String instrument_id;

    private String underlying_index;

    private String quote_currency;

    private BigDecimal contract_val;

    private Date listing;

    private Date delivery;

    private BigDecimal tick_size;

    private BigDecimal trade_increment;

}
