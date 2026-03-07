package com.hungtd.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.SpringVersion;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.hungtd.chatapp"})
//@ComponentScan({"com.hungtd.chatapp.*"})
//@EntityScan("com.hungtd.chatapp.*")
//@EnableJpaRepositories(basePackages = "com.hungtd.chatapp.*")
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableDiscoveryClient
public class ChatAppApplication {

    public static void main(String[] args) {
        System.out.println("Spring Boot version: " + SpringVersion.getVersion());
        
        SpringApplication.run(ChatAppApplication.class, args);
    }

}
