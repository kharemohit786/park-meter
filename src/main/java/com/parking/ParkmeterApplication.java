package com.parking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.aws.SQSConsumer;
import com.parking.model.NotificationBean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EntityScan(basePackages = { "com.parking.*" })
public class ParkmeterApplication {

    @Value("hackathon-2020")
    private String queueName;
    
	public static void main(String[] args) {
		SpringApplication.run(ParkmeterApplication.class, args);
	}
	
	@Bean
    @Qualifier("syncUserExecutor")
    public ExecutorService syncUserExecutor() {
        return Executors.newFixedThreadPool(1);
    }
	
	@Bean
    public SQSConsumer<NotificationBean> sqsConsumer() {
        return new SQSConsumer<NotificationBean>(queueName, new ObjectMapper(), NotificationBean.class);
    }
}
