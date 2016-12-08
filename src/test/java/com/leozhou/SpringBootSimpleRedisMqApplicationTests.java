package com.leozhou;

import com.leozhou.redis.RedisService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootSimpleRedisMqApplicationTests {

    private final static Logger logger = LoggerFactory.getLogger(SpringBootSimpleRedisMqApplicationTests.class);

    @Autowired
    private RedisService redisService;

    @Test
    @Ignore
    public void contextLoads() {
    }

    @Test
    public void testRedis() {
        logger.info("start test reids.");
        redisService.set("testRedis", "hello reids");

        logger.info("finish redis set");
        String value = redisService.get("testRedis");

        logger.info("get value from redis , valie is {}", value);
    }

    @Test
    public void testBlockRedis() {
        logger.info("start to test reids in block process");

        String value = redisService.leftPop("blpop", 1000, TimeUnit.MILLISECONDS, String.class);

        redisService.leftPush("blpop", "hello block pop");

        if (StringUtils.isBlank(value)){
            logger.info("value is null");
        }else{
            logger.info("value is {}", value);
        }
    }

}
