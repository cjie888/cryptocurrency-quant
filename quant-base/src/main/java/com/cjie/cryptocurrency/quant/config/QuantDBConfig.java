package com.cjie.cryptocurrency.quant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "quant.datasource")
@Data
public class QuantDBConfig {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private int maxActive;
    private int minIdle;

}
