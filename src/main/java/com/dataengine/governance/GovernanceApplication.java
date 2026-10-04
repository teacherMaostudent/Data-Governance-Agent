package com.dataengine.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.dataengine.governance.config.IntegrationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IntegrationProperties.class)
public class GovernanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GovernanceApplication.class, args);
    }
}
