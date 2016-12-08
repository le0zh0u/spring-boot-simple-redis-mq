package com.leozhou.redis.impl;

import com.leozhou.redis.RedisService;
import com.leozhou.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class RedisServiceImpl implements RedisService {

    public static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);
    private static String redisCode = "utf-8";
    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    public String getValueByKey(final String redisName, final String keyStr) {
        return redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(redisName + keyStr);
                if (connection.exists(key)) {
                    byte[] value = connection.get(key);
                    return redisTemplate.getStringSerializer().deserialize(value);
                }
                return null;
            }
        });
    }

    public void saveValue(final String redisName, final String key, final String value) {
        redisTemplate.execute(new RedisCallback<String>() {

            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(redisTemplate.getStringSerializer().serialize(redisName + key),
                        redisTemplate.getStringSerializer().serialize(value));
                return null;
            }
        });
    }

    public void deleteByKey(String redisName, final String key) {
        redisTemplate.delete(redisName + key);
    }

    public void addObject(final String redisName, final String redisKey, final Object value) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
                connection.hSet(redisTemplate.getStringSerializer().serialize(redisName),
                        redisTemplate.getStringSerializer().serialize(redisKey),
                        ((Jackson2JsonRedisSerializer<Object>) redisTemplate.getValueSerializer()).serialize(value));

                return null;
            }
        });
    }

    public Object getObjectByRedisNameAndKey(final String redisName, final String redisKey) {
        return redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(redisName);
                byte[] field = redisTemplate.getStringSerializer().serialize(redisKey);
                redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
                if (connection.exists(key)) {
                    byte[] value = connection.hGet(key, field);
                    return ((Jackson2JsonRedisSerializer<Object>) redisTemplate.getValueSerializer())
                            .deserialize(value);
                }
                return null;
            }
        });
    }

    public List<Object> getObjectsByRedisName(final String redisName) {
        return redisTemplate.execute(new RedisCallback<List<Object>>() {

            @Override
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(redisName);
                redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
                if (connection.exists(key)) {
                    List<byte[]> values = connection.hVals(key);
                    List<Object> result = new ArrayList<Object>();
                    for (byte[] value : values) {
                        result.add(((Jackson2JsonRedisSerializer<Object>) redisTemplate.getValueSerializer())
                                .deserialize(value));
                    }
                    return result;
                }
                return null;
            }
        });
    }

    public Map<String, String> getMapsByRedisName(final String redisName) {
        return redisTemplate.execute(new RedisCallback<Map>() {

            @Override
            public Map doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(redisName);
                if (connection.exists(key)) {
                    Map<byte[], byte[]> maps = connection.hGetAll(key);
                    Map result = new HashMap();

                    for (byte[] mapKey : maps.keySet()) {
                        //System.out.println("key= "+ mapKey + " and value= " + maps.get(mapKey));
                        String field = redisTemplate.getStringSerializer().deserialize(mapKey);
                        String fieldValue = redisTemplate.getStringSerializer().deserialize(maps.get(mapKey));

                        result.put(field, fieldValue);
                    }

                    return result;
                }
                return null;
            }
        });
    }

    public void deleteObjectByNameAndKey(final String redisName, final String redisKey) {
        redisTemplate.execute(new RedisCallback<Object>() {

            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
                connection.hDel(redisTemplate.getStringSerializer().serialize(redisName),
                        redisTemplate.getStringSerializer().serialize(redisKey));

                return null;
            }
        });
    }

    /**
     * WARNING: This method will clear redis current database data!
     */
    public void flushDB() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    /**
     * @param keys
     * @return
     */
    @Override
    public long del(final String... keys) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                long result = 0;
                for (int i = 0; i < keys.length; i++) {
                    result = redisConnection.del(keys[i].getBytes());
                }
                return result;
            }
        });
    }

    @Override
    public long hDel(final String redisName, final String... keys) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                long result = 0;
                for (int i = 0; i < keys.length; i++) {
                    result = redisConnection.hDel(redisTemplate.getStringSerializer().serialize(redisName),
                            redisTemplate.getStringSerializer().serialize(keys[i]));
                }
                return result;
            }
        });
    }

    @Override
    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(key, value);
                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return 1L;
            }
        });
    }

    @Override
    public void set(String key, String value, long liveTime) {
        this.set(redisTemplate.getStringSerializer().serialize(key),
                redisTemplate.getStringSerializer().serialize(value), liveTime);
    }

    @Override
    public void set(String key, String value) {
        this.set(key, value, 0L);
    }

    @Override
    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    @Override
    public String get(final String key) {
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                try {
                    return new String(redisConnection.get(key.getBytes()), redisCode);
                } catch (UnsupportedEncodingException e) {
                    logger.warn("get value(key: {})in redis failed due to UnsupportedEncodingException", key);
                } catch (NullPointerException nullException) {
                    logger.warn("none value(key: {}) in redis due to NullPointerException", key);
                }
                return "";
            }
        });
    }

    @Override
    public void hSet(final byte[] key, final byte[] field, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.hSet(key, field, value);
                if (liveTime > 0) {
                    redisConnection.expire(key, liveTime);
                    //                    redisConnection.setEx(key, liveTime, value);
                }
                return 1L;
            }
        });
    }

    @Override
    public void hSet(String key, String field, String value, long liveTime) {
        this.hSet(redisTemplate.getStringSerializer().serialize(key),
                redisTemplate.getStringSerializer().serialize(field),
                redisTemplate.getStringSerializer().serialize(value), liveTime);
    }

    @Override
    public void hSet(String key, String field, String value) {
        this.hSet(key, field, value, 0L);
    }

    @Override
    public void hSet(byte[] key, byte[] field, byte[] value) {
        this.hSet(key, field, value, 0L);
    }

    @Override
    public String hGet(final String key, final String field) {
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                try {
                    return new String(redisConnection.hGet(redisTemplate.getStringSerializer().serialize(key),
                            redisTemplate.getStringSerializer().serialize(field)), redisCode);
                } catch (UnsupportedEncodingException e) {
                    logger.warn("redis hash get(key: {}, field{}) failed due to UnsupportedEncodingException!", key, field);
                } catch (Exception e) {
                    logger.warn("redis hash get(key: {}, field{}) failed due to Exception!", key, field);
                }
                return "";
            }
        });
    }

    @Override
    public Set<String> setKeys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public boolean exists(final String key) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.exists(key.getBytes());
            }
        });
    }

    @Override
    public String ping() {
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.ping();
            }
        });
    }

    @Override
    public void hSaveWithExpireTime(final byte[] key, final byte[] field, final byte[] value, final long expireTime) {
        redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.hSet(key, field, value);
                if (expireTime > 0) {
                    redisConnection.expire(key, expireTime);
                }
                return 1L;
            }
        });
    }

    @Override
    public <T> void hSaveListDataWithExpireTime(String key, String field, List<T> list, long expireTime) {
        String value = JsonUtil.toString(list);
        hSaveWithExpireTime(redisTemplate.getStringSerializer().serialize(key),
                redisTemplate.getStringSerializer().serialize(field),
                redisTemplate.getStringSerializer().serialize(value), expireTime);
    }

    @Override
    public <T> void hSaveListData(String key, String field, List<T> list) {
        hSaveListDataWithExpireTime(key, field, list, 0);
    }

    @Override
    public <T> List<T> hGetListData(final String key, final String field, final Class<T> clazz) {
        return redisTemplate.execute(new RedisCallback<List<T>>() {

            @Override
            public List<T> doInRedis(RedisConnection connection) throws DataAccessException {
                List<T> list = new ArrayList<T>();
                byte[] keyByte = redisTemplate.getStringSerializer().serialize(key);
                byte[] fieldByte = redisTemplate.getStringSerializer().serialize(field);
                if (connection.hExists(keyByte, fieldByte)) {
                    byte[] valueByte = connection.hGet(keyByte, fieldByte);
                    String s = redisTemplate.getStringSerializer().deserialize(valueByte);
                    list = JsonUtil.toList(s, clazz);
                }
                return list;
            }
        });
    }

    @Override
    public void leftPush(final String key, final String value) {
        redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisTemplate.opsForList().leftPush(key, value);
                return null;
            }
        });
    }

    @Override
    public <T> T leftPop(final String key, final long timeout, final TimeUnit unit, final Class<T> clazz) {
        return redisTemplate.execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String value = redisTemplate.opsForList().leftPop(key, timeout, unit);
                if (clazz.equals(String.class)){
                    return (T) value;
                }else{
                    return JsonUtil.toObject(value, clazz);
                }
            }
        });
    }
}
