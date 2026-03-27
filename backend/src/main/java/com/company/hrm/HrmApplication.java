package com.company.hrm;

import com.company.hrm.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class HrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmApplication.class, args);
    }
}
