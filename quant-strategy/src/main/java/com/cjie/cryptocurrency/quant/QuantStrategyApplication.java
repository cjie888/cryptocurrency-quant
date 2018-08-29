package com.cjie.cryptocurrency.quant;

import com.cxytiandi.elasticjob.annotation.EnableElasticJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@EnableElasticJob
public class QuantStrategyApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuantStrategyApplication.class,args);
    }
}