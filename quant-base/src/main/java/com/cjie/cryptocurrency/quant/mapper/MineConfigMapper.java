package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.CurrencyRatio;
import com.cjie.cryptocurrency.quant.model.MineConfig;
import org.apache.ibatis.annotations.Param;

public interface MineConfigMapper {
    int deleteByPrimaryKey(Long id);

    int insert(MineConfig record);

    int insertSelective(MineConfig record);

    MineConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MineConfig record);

    int updateByPrimaryKey(MineConfig record);

    MineConfig getLatestConfig(@Param("site") String site,
                                 @Param("baseCurrency")String baseCurrency,
                                 @Param("quotaCurrency")String quotaCurrency);

}