package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.SpotOrder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpotOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SpotOrder record);

    int insertSelective(SpotOrder record);

    SpotOrder selectByPrimaryKey(Long id);

    SpotOrder selectByOrderId(String orderId);


    SpotOrder selectLatest(@Param("symbol") String symbol, @Param("strategy") String strategy);

    List<SpotOrder> selectByStatus(@Param("symbol")String symbol,
                                   @Param("strategy") String strategy,
                                   @Param("statuses") List<Integer> statuses);

    int updateByPrimaryKeySelective(SpotOrder record);

    int updateByPrimaryKey(SpotOrder record);

    void updateStatus(@Param("orderId") String orderId, @Param("status") Integer status);

    List<SpotOrder> groupBySymbol(@Param("startTime") LocalDateTime startTime, @Param("endTime")LocalDateTime endTime);
}