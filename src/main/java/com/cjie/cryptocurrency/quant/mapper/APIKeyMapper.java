package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.APIKey;

public interface APIKeyMapper {
    int deleteByPrimaryKey(Long id);

    int insert(APIKey record);

    int insertSelective(APIKey record);

    APIKey selectByPrimaryKey(Long id);

    APIKey selectBySite(String site);

    int updateByPrimaryKeySelective(APIKey record);

    int updateByPrimaryKey(APIKey record);
}