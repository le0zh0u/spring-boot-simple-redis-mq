package com.leozhou.redis.impl;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Created by zhouchunjie on 2016/12/8.
 */
public class RedisMessageListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] bytes) {
        System.out.println("Message received: " + message.toString());
    }
}
