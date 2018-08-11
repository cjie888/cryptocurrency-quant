package com.cjie.cryptocurrency.quant.api.huobi.domain.ws;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HuobiWSSub {

    private String sub;

    private String id;

}
