package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyKline;

public interface CurrencyKlineMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyKline record);

    int insertSelective(CurrencyKline record);

    CurrencyKline selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyKline record);

    int updateByPrimaryKey(CurrencyKline record);
}