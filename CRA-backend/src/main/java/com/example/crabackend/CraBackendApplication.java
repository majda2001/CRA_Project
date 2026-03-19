package com.example.crabackend;

import com.example.crabackend.config.AdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AdminProperties.class)
public class CraBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CraBackendApplication.class, args);
    }

}
