package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.OptionsOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OptionsOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OptionsOrder record);

    int insertSelective(OptionsOrder record);

    OptionsOrder selectByPrimaryKey(Long id);

    OptionsOrder selectByOrderId(String orderId);


    OptionsOrder selectLatest(@Param("symbol") String symbol, @Param("strategy") String strategy);

    List<OptionsOrder> selectByStatus(@Param("symbol")String symbol,
                                   @Param("strategy") String strategy,
                                   @Param("statuses") List<Integer> statuses);

    int updateByPrimaryKeySelective(OptionsOrder record);

    int updateByPrimaryKey(OptionsOrder record);

    void updateStatus(@Param("orderId") String orderId, @Param("status") Integer status);
}