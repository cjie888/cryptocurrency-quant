package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyOrder;

public interface CurrencyOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CurrencyOrder record);

    int insertSelective(CurrencyOrder record);

    CurrencyOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CurrencyOrder record);

    int updateByPrimaryKey(CurrencyOrder record);
}