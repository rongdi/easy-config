package com.rdpaas.easyconfig.sample;

import com.rdpaas.easyconfig.ann.EnableEasyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEasyConfig
public class RunApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RunApplication.class);
    }

}
