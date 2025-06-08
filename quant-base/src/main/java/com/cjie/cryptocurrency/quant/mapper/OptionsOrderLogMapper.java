package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.OptionsOrder;
import com.cjie.cryptocurrency.quant.model.OptionsOrderLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OptionsOrderLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OptionsOrderLog record);

    int insertSelective(OptionsOrderLog record);

    OptionsOrderLog selectByPrimaryKey(Long id);

    OptionsOrderLog selectByOrderId(String orderId);


    OptionsOrderLog selectByReferId(Long referId);


    OptionsOrderLog selectLatest(@Param("symbol") String symbol, @Param("strategy") String strategy);

    List<OptionsOrderLog> selectByStatus(@Param("symbol")String symbol,
                                   @Param("strategy") String strategy,
                                   @Param("statuses") List<Integer> statuses);

    int updateByPrimaryKeySelective(OptionsOrderLog record);

    int updateByPrimaryKey(OptionsOrderLog record);

    void updateStatus(@Param("orderId") String orderId, @Param("status") Integer status);
}