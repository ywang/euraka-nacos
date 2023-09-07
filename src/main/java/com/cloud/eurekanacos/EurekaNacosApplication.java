package com.cloud.eurekanacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaNacosApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaNacosApplication.class, args);
    }

}
