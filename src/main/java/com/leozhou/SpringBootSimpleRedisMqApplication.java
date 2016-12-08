package com.leozhou;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringBootSimpleRedisMqApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootSimpleRedisMqApplication.class);

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run(SpringBootSimpleRedisMqApplication.class, args);

    }
}
