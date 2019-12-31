package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.SwapOrder;

public interface SwapOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SwapOrder record);

    int insertSelective(SwapOrder record);

    SwapOrder selectByPrimaryKey(Long id);


    SwapOrder selectLatest(String instrumentId);

    int updateByPrimaryKeySelective(SwapOrder record);

    int updateByPrimaryKey(SwapOrder record);
}