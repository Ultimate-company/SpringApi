package com.example.SpringApi.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@EntityScan("org.example.SpringApi.DatabaseModels.CarrierDatabase")
@EnableJpaRepositories(
        entityManagerFactoryRef = "multiEntityManager",
        transactionManagerRef = "multiTransactionManager",
        basePackages = {
                "com.example.SpringApi.Repository.CarrierDatabase",
                "com.example.SpringApi.DatabaseModels.CarrierDatabase"
        })
public class CarrierDataSource {
    //add JPA entities path here
    private final String PACKAGE_SCAN = "com.example.SpringApi.DatabaseModels.CarrierDatabase";

    @Bean(name = "client02DataSource")
    public DataSource client02DataSource() {
        try {
            return DataSourceBuilder.create()
                    .url("jdbc:mysql://35.237.130.224:3306/CentralDatabase")
                    .password("uUS2qz?e+@~$j&dm")
                    .username("root-dev-sqluser")
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .build();
        } catch (Exception ex) {
            System.err.println("Error creating client02DataSource: " + ex.getMessage());
            ex.printStackTrace();  // Print the full stack trace for better visibility
            throw new RuntimeException("Failed to create client02DataSource", ex); // Re-throw to fail the context
        }
    }

    @Bean(name = "client03DataSource")
    public DataSource client03DataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://35.237.130.224:3306/CentralDatabase")
                .password("uUS2qz?e+@~$j&dm")
                .username("root-dev-sqluser")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    //The multidatasource configuration
    @Primary
    @Bean(name = "multiRoutingDataSource")
    public DataSource multiRoutingDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(1L, client02DataSource());
        targetDataSources.put(2L, client03DataSource());
        MultiRoutingDataSource multiRoutingDataSource
                = new MultiRoutingDataSource();
        multiRoutingDataSource.setDefaultTargetDataSource(client02DataSource());
        multiRoutingDataSource.setTargetDataSources(targetDataSources);
        return multiRoutingDataSource;
    }

    //add multi entity configuration code
    @Primary
    @Bean(name = "multiEntityManager")
    public LocalContainerEntityManagerFactoryBean multiEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(multiRoutingDataSource());
        em.setPackagesToScan(PACKAGE_SCAN);
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    @Primary
    @Bean(name = "multiTransactionManager")
    public PlatformTransactionManager multiTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(multiEntityManager().getObject());
        transactionManager.setRollbackOnCommitFailure(true);
        return transactionManager;
    }

    @Primary
    @Bean(name="entityManagerFactory")
    public LocalSessionFactoryBean dbSessionFactory() {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(multiRoutingDataSource());
        sessionFactoryBean.setPackagesToScan(PACKAGE_SCAN);

        sessionFactoryBean.setHibernateProperties(hibernateProperties());
        return sessionFactoryBean;
    }

    //add hibernate properties
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.id.new_generator_mappings", false);
        properties.put("hibernate.jdbc.lob.non_contextual_creation", true);
        properties.put("hibernate.hbm2ddl.auto", "update");
        return properties;
    }
}