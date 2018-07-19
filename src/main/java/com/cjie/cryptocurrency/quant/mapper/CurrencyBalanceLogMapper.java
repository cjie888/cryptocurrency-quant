package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyBalanceLog;

public interface CurrencyBalanceLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyBalanceLog record);

    int insertSelective(CurrencyBalanceLog record);

    CurrencyBalanceLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyBalanceLog record);

    int updateByPrimaryKey(CurrencyBalanceLog record);
}