package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.FuturesInstrument;
import com.cjie.cryptocurrency.quant.model.PerpetualFuturesInstrument;

import java.util.List;

public interface PerpetualFuturesInstrumentMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PerpetualFuturesInstrument record);

    int insertSelective(PerpetualFuturesInstrument record);

    PerpetualFuturesInstrument selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PerpetualFuturesInstrument record);

    int updateByPrimaryKey(PerpetualFuturesInstrument record);

    PerpetualFuturesInstrument getFuturesInstrument(String instrumentId);

    List<PerpetualFuturesInstrument> getAllInstruments();

}