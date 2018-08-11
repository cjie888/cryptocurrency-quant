package com.cjie.cryptocurrency.quant.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan("com.cjie.cryptocurrency.quant.mapper")
public class QuantMyBatisConfig {

    private static String MYBATIS_CONFIG = "mybatis-config.xml";

    private static String MAPPER_PATH = "quant_mapper/**.xml";

    private String typeAliasPackage = "com.cjie.cryptocurrency.quant.model";

    @Autowired
    private QuantDBConfig quantDbConfig;

    @Bean(name = "quantDatasource")
    @Primary
    public DataSource createDataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl( quantDbConfig.getUrl() );
        datasource.setDriverClassName( quantDbConfig.getDriverClassName() );
        datasource.setUsername( quantDbConfig.getUsername() );
        datasource.setPassword( quantDbConfig.getPassword() );
        datasource.setMaxActive( quantDbConfig.getMaxActive() );
        datasource.setMinIdle( quantDbConfig.getMinIdle() );
        datasource.setTestOnBorrow( true );

        return datasource;
    }

    @Bean(name = "quantSqlSessionFactory")
    @Primary
    public SqlSessionFactory createSqlSessionFactoryBean(@Qualifier("quantDatasource") DataSource dataSource)
        throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        /** 设置mybatis configuration 扫描路径 */
        sqlSessionFactoryBean.setConfigLocation( new ClassPathResource( MYBATIS_CONFIG ) );
        /** 添加mapper 扫描路径*/
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + MAPPER_PATH;
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver
            = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(
            pathMatchingResourcePatternResolver.getResources( packageSearchPath ) );
        /** 设置datasource */
        sqlSessionFactoryBean.setDataSource( dataSource );
        /** 设置typeAlias 包扫描路径 */
        sqlSessionFactoryBean.setTypeAliasesPackage( typeAliasPackage );

        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "quantTransactionManager")
    @Primary
    public DataSourceTransactionManager createTransactionManager(@Qualifier("quantDatasource") DataSource dataSource) {
        return new DataSourceTransactionManager( dataSource );
    }

    @Bean(name = "quantSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate( sqlSessionFactory );
    }
}
