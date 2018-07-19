package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyBalance;

public interface CurrencyBalanceMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyBalance record);

    int insertSelective(CurrencyBalance record);

    CurrencyBalance selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyBalance record);

    int updateByPrimaryKey(CurrencyBalance record);
}