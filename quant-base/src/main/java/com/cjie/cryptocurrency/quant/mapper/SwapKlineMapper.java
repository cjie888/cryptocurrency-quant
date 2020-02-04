package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.SwapKline;
import com.cjie.cryptocurrency.quant.model.SwapKline;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface SwapKlineMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SwapKline record);

    int insertSelective(SwapKline record);

    SwapKline selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SwapKline record);

    int updateByPrimaryKey(SwapKline record);

    SwapKline getKLine(@Param("time") Date klineTime, @Param("instrumentId") String instrumentId,
                       @Param("suffix") String suffix);


    SwapKline getMinKLine(@Param("instrumentId") String instrumentId,
                       @Param("suffix") String suffix);
}