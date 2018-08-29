package com.cjie.cryptocurrency.quant.strategy;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class BeanConfig {

    /**
     * 任务执行事件数据源
     * @return
     */
    @Bean("logDatasource")
    @ConfigurationProperties("spring.datasource.druid.log")
    public DataSource dataSourceTwo(){
        return DruidDataSourceBuilder.create().build();
    }

}
