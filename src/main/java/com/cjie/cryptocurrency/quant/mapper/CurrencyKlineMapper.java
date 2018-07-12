package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyKline;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface CurrencyKlineMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CurrencyKline record);

    int insertSelective(CurrencyKline record);

    CurrencyKline selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CurrencyKline record);

    int updateByPrimaryKey(CurrencyKline record);

    CurrencyKline getCurrencyLine(@Param("klineTime") Date klineTime,
                                  @Param("baseCurrency") String baseCurrency,
                                  @Param("quotaCurrency") String quotaCurrency,@Param("site") String site);
}