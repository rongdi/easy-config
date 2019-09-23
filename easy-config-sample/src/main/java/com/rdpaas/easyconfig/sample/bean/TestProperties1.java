package com.rdpaas.easyconfig.sample.bean;


import com.rdpaas.easyconfig.ann.RefreshScope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("props1")
@ConfigurationProperties(prefix = "test")
@RefreshScope
public class TestProperties1 {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Bean
    @RefreshScope
    @ConfigurationProperties(prefix = "food")
    public Food food() {
        return new Food();
    }

    @Bean
    public Dog dog() {
        return new Dog(food());
    }

    @Override
    public String toString() {
        return "TestProperties{" +
            "name='" + name + '\'' +
            '}';
    }
}
