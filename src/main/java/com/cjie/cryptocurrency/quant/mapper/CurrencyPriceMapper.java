package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyPrice;

public interface CurrencyPriceMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyPrice record);

    int insertSelective(CurrencyPrice record);

    CurrencyPrice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyPrice record);

    int updateByPrimaryKey(CurrencyPrice record);
}