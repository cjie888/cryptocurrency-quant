package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyKline;
import com.cjie.cryptocurrency.quant.model.CurrencyPair;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface CurrencyPairMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyPair record);

    int insertSelective(CurrencyPair record);

    CurrencyPair selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyPair record);

    int updateByPrimaryKey(CurrencyPair record);

    List<CurrencyPair> getAllCurrency(String site);

    CurrencyPair getCurrencyPair(@Param("baseCurrency") String baseCurrency,
                                  @Param("quotaCurrency") String quotaCurrency,
                                  @Param("site") String site);
}