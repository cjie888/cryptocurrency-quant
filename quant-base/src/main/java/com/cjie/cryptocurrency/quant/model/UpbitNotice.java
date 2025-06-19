package com.cjie.cryptocurrency.quant.model;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpbitNotice {

    private Long id;

    private Date createTime;

    private Date listedAt;

    private Date firstListedAt;

    private Long upbitId;

    private String title;

    private Boolean needNewBadge;

    private Boolean needUpdateBadge;


}
