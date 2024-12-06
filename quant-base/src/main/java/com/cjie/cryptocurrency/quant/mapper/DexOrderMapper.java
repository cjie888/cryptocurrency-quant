package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.DexOrder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DexOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DexOrder record);

    int insertSelective(DexOrder record);

    DexOrder selectByPrimaryKey(Long id);

    DexOrder selectByFromAddress(@Param("chainId")String chainId, @Param("fromAddress")String fromAddress);

    DexOrder selectByToAddress(@Param("chainId")String chainId, @Param("toAddress")String toAddress);


    int updateByPrimaryKeySelective(DexOrder record);

    int updateByPrimaryKey(DexOrder record);

}