package com.rdpaas.easyconfig.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.rdpaas")
public class RunApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RunApplication.class);
    }

}
