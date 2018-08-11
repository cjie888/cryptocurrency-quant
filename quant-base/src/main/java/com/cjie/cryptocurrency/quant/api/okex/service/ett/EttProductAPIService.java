package com.cjie.cryptocurrency.quant.api.okex.service.ett;

import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttSettlementDefinePrice;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttConstituentsResult;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttSettlementDefinePrice;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttConstituentsResult;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttSettlementDefinePrice;
import com.cjie.cryptocurrency.quant.api.okex.bean.ett.result.EttSettlementDefinePrice;

import java.util.List;

/**
 * @author chuping.cui
 * @date 2018/7/4
 */
public interface EttProductAPIService {

    /**
     * Get ett constituents
     *
     * @param ett ett name
     * @return constituents
     */
    EttConstituentsResult getConstituents(String ett);

    /**
     * Get ett settlement plan define price
     *
     * @param ett ett name
     * @return settlement plan define price list
     */
    List<EttSettlementDefinePrice> getDefinePrice(String ett);

}
