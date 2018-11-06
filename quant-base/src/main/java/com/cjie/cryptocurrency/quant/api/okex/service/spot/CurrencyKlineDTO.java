package com.cjie.cryptocurrency.quant.api.okex.service.spot;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyKlineDTO {

    private String time;

    private String amount;

    private String count;

    private String open;

    private String close;

    private String low;

    private String high;

    private String volume;

}
