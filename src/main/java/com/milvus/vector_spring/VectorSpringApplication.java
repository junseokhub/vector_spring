package com.milvus.vector_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan
public class VectorSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(VectorSpringApplication.class, args);
	}

//	@Bean
//	public AuditorAware<String> auditorProvider() {
//		return () -> Optional.of(UUID.randomUUID().toString());
//	}
}
