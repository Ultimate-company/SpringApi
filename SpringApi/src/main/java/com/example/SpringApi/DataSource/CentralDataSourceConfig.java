package com.example.SpringApi.DataSource;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableTransactionManagement
@EntityScan("com.example.SpringApi.DatabaseModels.CentralDatabase")
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryCentralData",
        basePackages = {
                "com.example.SpringApi.Repository.CentralDatabase",
                "com.example.SpringApi.DatabaseModels.CentralDatabase"
        }
)
public class CentralDataSourceConfig {
    @Bean(name = "entityManagerFactoryBuilderCentralData")
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }

    @Primary
    @Bean(name = "centralDataSource")
    public DataSource centralDataSource(Environment environment) {
        String profile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";
        switch (profile) {
            case "development":
                return DataSourceBuilder.create()
                        .url("jdbc:mysql://host.docker.internal:3307/CentralDatabase")
                        .password("root")
                        .username("root")
                        .driverClassName("com.mysql.cj.jdbc.Driver")
                        .build();
            case "localhost":
                return DataSourceBuilder.create()
                        .url("jdbc:mysql://localhost:3307/CentralDatabase")
                        .password("root")
                        .username("root")
                        .driverClassName("com.mysql.cj.jdbc.Driver")
                        .build();
            case "staging":
               return null;
            case "uat":
               return null;
            case "main":
               return null;
        }
        return null;
    }

    @Primary
    @Bean(name = "entityManagerFactoryCentralData")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(@Qualifier("entityManagerFactoryBuilderCentralData") EntityManagerFactoryBuilder builder,
                                                                           @Qualifier("centralDataSource") DataSource dataSource){
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");

        return builder.dataSource(dataSource)
                .properties(properties)
                .packages("com.example.SpringApi.DatabaseModels.CentralDatabase")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactoryCentralData") EntityManagerFactory entityManagerFactory){
        return new JpaTransactionManager(entityManagerFactory);
    }
}
