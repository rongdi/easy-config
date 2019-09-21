package com.rdpaas.easyconfig.sample.bean;


import com.rdpaas.easyconfig.ann.RefreshScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration("props")
@RefreshScope
@ComponentScan("com.rdpaas.easyconfig")
public class TestProperties {

    @Value("${test.name:test}")
    private String name;

    @RefreshScope
    @Bean
    public Cat cat(Person person) {
        return new Cat(person);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestProperties{" +
            "name='" + name + '\'' +
            '}';
    }
}
