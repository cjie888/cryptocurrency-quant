package com.cjie.cryptocurrency.quant.api.okex.bean.futures.result;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class Kline {

    private Date klineTime;

    private String instrument_id;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal low;

    private BigDecimal high;

    private BigDecimal volume;

    private BigDecimal currencyVolume;

    private Date createTime;

}