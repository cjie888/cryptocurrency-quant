package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class APIKey {
    private Long id;

    private String site;

    private String apiKey;

    private String apiSecret;

    private String apiPassphrase;

    private Date createTime;

    private String domain;

}