package com.example.SpringApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
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
public class SpringApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringApiApplication.class, args);
	}

}
