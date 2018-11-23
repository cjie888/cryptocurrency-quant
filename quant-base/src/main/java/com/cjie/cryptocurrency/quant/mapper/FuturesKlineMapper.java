package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.FuturesKline;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface FuturesKlineMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuturesKline record);

    int insertSelective(FuturesKline record);

    FuturesKline selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuturesKline record);

    int updateByPrimaryKey(FuturesKline record);

    FuturesKline getKLine(@Param("time") Date klineTime, @Param("instrumentId") String instrumentId,
                          @Param("suffix") String suffix);
}