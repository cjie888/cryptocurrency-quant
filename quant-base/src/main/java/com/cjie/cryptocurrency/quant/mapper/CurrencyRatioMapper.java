package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyRatio;

public interface CurrencyRatioMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyRatio record);

    int insertSelective(CurrencyRatio record);

    CurrencyRatio selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyRatio record);

    int updateByPrimaryKey(CurrencyRatio record);

    CurrencyRatio getLatestRatio(String site, String baseCurrency, String quotaCurrency);
}