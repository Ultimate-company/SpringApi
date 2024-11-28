package com.example.SpringApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@ComponentScan(basePackages =
		{
				// beans from authentication
				"com.example.SpringApi.Authentication",

				// scan the models
				"com.example.SpringApi.DatabaseModels.CarrierDatabase",
				"com.example.SpringApi.DatabaseModels.CentralDatabase",

				// scan the repositories
				"com.example.SpringApi.Repository.CarrierDatabase",
				"com.example.SpringApi.Repository.CentralDatabase",

				// scan the controllers
				"com.example.SpringApi.Controllers.CentralDatabase",
				"com.example.SpringApi.Controllers.CarrierDatabase",

				// scan the services
				"com.example.SpringApi.Services.CentralDatabase",
				"com.example.SpringApi.Services.CarrierDatabase",
				"com.example.SpringApi.Services",

				// scan the datasource beans
				"com.example.SpringApi.DataSource",
		}
		)
@EntityScan(basePackages =
		{
				"com.example.SpringApi.DatabaseModels.CentralDatabase",
				"com.example.SpringApi.DatabaseModels.CarrierDatabase"
		}
		)
@EnableAsync
public class SpringApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringApiApplication.class, args);
	}

	@Bean(name = "asyncExecutor")
	public Executor asyncExecutor()  {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(3);
		executor.setMaxPoolSize(3);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("AsynchThread-");
		executor.initialize();
		return executor;
	}
}