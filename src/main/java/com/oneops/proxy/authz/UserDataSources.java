package com.oneops.proxy.authz;

import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator;
import org.springframework.boot.autoconfigure.jooq.SpringTransactionProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * OneOps user data source bean configurations.
 *
 * @author Suresh
 */
@Configuration
public class UserDataSources {

  @Bean
  @Primary
  @ConfigurationProperties("oneops.datasources.prod")
  public DataSourceProperties prodProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  @ConfigurationProperties("oneops.datasources.prod")
  public DataSource prodDataSource() {
    return prodProperties().initializeDataSourceBuilder().build();
  }

  @Bean
  @ConfigurationProperties("oneops.datasources.mgmt")
  public DataSourceProperties mgmtProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("oneops.datasources.mgmt")
  public DataSource mgmtDataSource() {
    return mgmtProperties().initializeDataSourceBuilder().build();
  }

  @Bean
  @ConfigurationProperties("oneops.datasources.stg")
  public DataSourceProperties stgProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("oneops.datasources.stg")
  public DataSource stgDataSource() {
    return stgProperties().initializeDataSourceBuilder().build();
  }

  @Bean
  @ConfigurationProperties("oneops.datasources.dev")
  public DataSourceProperties devProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("oneops.datasources.dev")
  public DataSource devDataSource() {
    return devProperties().initializeDataSourceBuilder().build();
  }

  @Bean
  @Primary
  @Qualifier("prod")
  public DSLContext prodDSLContext(PlatformTransactionManager txManager) {
    return getDslContext(txManager, prodDataSource());
  }

  @Bean
  @Qualifier("mgmt")
  public DSLContext mgmtDSLContext(PlatformTransactionManager txManager) {
    return getDslContext(txManager, mgmtDataSource());
  }

  @Bean
  @Qualifier("stg")
  public DSLContext stgDSLContext(PlatformTransactionManager txManager) {
    return getDslContext(txManager, stgDataSource());
  }

  @Bean
  @Qualifier("dev")
  public DSLContext devDSLContext(PlatformTransactionManager txManager) {
    return getDslContext(txManager, devDataSource());
  }

  private DSLContext getDslContext(PlatformTransactionManager txManager, DataSource dataSource) {
    DefaultConfiguration config = new DefaultConfiguration();
    config.set(new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource)));
    config.set(new DefaultExecuteListenerProvider(new JooqExceptionTranslator()));
    config.set(new SpringTransactionProvider(txManager));
    return new DefaultDSLContext(config);
  }
}
