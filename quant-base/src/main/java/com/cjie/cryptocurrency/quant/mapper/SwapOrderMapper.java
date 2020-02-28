package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.SwapOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SwapOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SwapOrder record);

    int insertSelective(SwapOrder record);

    SwapOrder selectByPrimaryKey(Long id);

    SwapOrder selectByOrderId(String orderId);


    SwapOrder selectLatest(@Param("instrumentId") String instrumentId, @Param("strategy") String strategy);

    List<SwapOrder> selectByStatus(@Param("instrumentId")String instrumentId,
                                   @Param("strategy") String strategy,
                                   @Param("statuses") List<Integer> statuses);

    int updateByPrimaryKeySelective(SwapOrder record);

    int updateByPrimaryKey(SwapOrder record);

    void updateStatus(@Param("orderId") String orderId, @Param("status") Integer status);
}