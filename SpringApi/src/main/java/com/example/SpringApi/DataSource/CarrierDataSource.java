package com.example.SpringApi.DataSource;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
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

    //The multidatasource configuration
    @Primary
    @Bean(name = "multiRoutingDataSource")
    public DataSource multiRoutingDataSource(Environment environment) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        String profile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";

        int totalClients = 0;
        switch (profile) {
            case "development":
                totalClients = 7;
                for (int i = 1; i <= totalClients; i++) {
                    targetDataSources.put((long) i, DataSourceBuilder.create()
                            .url("jdbc:mysql://localhost:3307/Client_" + String.format("%02d", i))
                            .password("root")
                            .username("root")
                            .driverClassName("com.mysql.cj.jdbc.Driver")
                            .build());
                }
                break;
            case "staging":
                totalClients = 0;
                for (int i = 1; i <= totalClients; i++) {

                }
                break;
            case "uat":
                totalClients = 0;
                for (int i = 1; i <= totalClients; i++) {

                }
                break;
            case "main":
                totalClients = 0;
                for (int i = 1; i <= totalClients; i++) {

                }
                break;
        }

        if (targetDataSources.isEmpty()) {
            return null;
        }

        MultiRoutingDataSource multiRoutingDataSource = new MultiRoutingDataSource();
        multiRoutingDataSource.setDefaultTargetDataSource(targetDataSources.get(1L));
        multiRoutingDataSource.setTargetDataSources(targetDataSources);
        return multiRoutingDataSource;
    }

    //add multi entity configuration code
    @Primary
    @Bean(name = "multiEntityManager")
    public LocalContainerEntityManagerFactoryBean multiEntityManager(Environment environment) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(multiRoutingDataSource(environment));
        em.setPackagesToScan(PACKAGE_SCAN);
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        return em;
    }

    @Primary
    @Bean(name = "multiTransactionManager")
    public PlatformTransactionManager multiTransactionManager(Environment environment) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(multiEntityManager(environment).getObject());
        transactionManager.setRollbackOnCommitFailure(true);
        return transactionManager;
    }

    @Primary
    @Bean(name="entityManagerFactory")
    public LocalSessionFactoryBean dbSessionFactory(Environment environment) {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(multiRoutingDataSource(environment));
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