package com.cjie.cryptocurrency.quant.mapper;

import com.cjie.cryptocurrency.quant.model.UpbitNotice;

public interface UpbitNoticeMapper {

    int insert(UpbitNotice notice);

    int updateById(UpbitNotice notice);

    UpbitNotice selectById(Long id);

    UpbitNotice selectByUpbitId(Long upbitId);

    int deleteById(Long id);
}
