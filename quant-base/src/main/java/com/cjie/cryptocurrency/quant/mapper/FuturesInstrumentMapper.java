package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.FuturesInstrument;

import java.util.List;

public interface FuturesInstrumentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FuturesInstrument record);

    int insertSelective(FuturesInstrument record);

    FuturesInstrument selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FuturesInstrument record);

    int updateByPrimaryKey(FuturesInstrument record);

    FuturesInstrument getFuturesInstrument(String instrumentId);

    List<FuturesInstrument> getAllInstruments();

}