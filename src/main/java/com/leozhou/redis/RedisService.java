package com.leozhou.redis;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisService {

    String getValueByKey(final String redisName, final String keyStr);

    void saveValue(String redisName, final String key, final String value);

    void deleteByKey(String redisName, final String key);

    /**
     * 在redis中添加对象
     *
     * @param redisName
     * @param redisKey
     */
    void addObject(final String redisName, final String redisKey, final Object value);

    /**
     * 从redis中获取对象
     *
     * @param redisName
     * @param redisKey
     * @return 返回 LinkedHashMap
     */
    Object getObjectByRedisNameAndKey(final String redisName, final String redisKey);

    /**
     * 从redis中获取对象field and field's value map列表
     *
     * @param redisName
     * @return
     */
    Map getMapsByRedisName(final String redisName);

    /**
     * 从redis中获取对象值all fields value 列表
     *
     * @param redisName
     * @return
     */
    List<Object> getObjectsByRedisName(final String redisName);

    /**
     * 从redis中删除某字段对象
     *
     * @param redisName
     * @param redisKey
     */
    void deleteObjectByNameAndKey(final String redisName, final String redisKey);

    void flushDB();

    /**
     * 通过key删除
     *
     * @param keys
     */
    long del(String... keys);

    /**
     * 通过redisName删除hashRedis中的key和value
     *
     * @param redisName
     * @param keys
     * @return
     */
    long hDel(String redisName, String... keys);

    /**
     * 添加key value 并且设置存活时间(byte)
     *
     * @param key
     * @param value
     * @param liveTime
     */
    void set(byte[] key, byte[] value, long liveTime);

    /**
     * 添加key value 并且设置存活时间
     *
     * @param key
     * @param value
     * @param liveTime 单位秒
     */
    void set(String key, String value, long liveTime);

    /**
     * 添加key value
     *
     * @param key
     * @param value
     */
    void set(String key, String value);

    /**
     * 添加key value (字节)(序列化)
     *
     * @param key
     * @param value
     */
    void set(byte[] key, byte[] value);

    /**
     * 获取redis value (String)
     *
     * @param key
     * @return
     */
    String get(String key);

    void hSet(byte[] key, byte[] field, byte[] value, long liveTime);

    void hSet(String key, String field, String value, long liveTime);

    void hSet(String key, String field, String value);

    void hSet(byte[] key, byte[] field, byte[] value);

    String hGet(String key, String field);


    /**
     * 通过正则匹配keys
     *
     * @param pattern
     * @return
     */
    Set<String> setKeys(String pattern);

    /**
     * 检查key是否已经存在
     *
     * @param key
     * @return
     */
    boolean exists(String key);

    /**
     * 检查是否连接成功
     *
     * @return
     */
    String ping();

    <T> void hSaveWithExpireTime(final byte[] key, final byte[] field, final byte[] value, final long expireTime);

    <T> void hSaveListDataWithExpireTime(final String key, final String field, final List<T> list,
                                         final long expireTime);

    <T> void hSaveListData(final String key, final String field, final List<T> list);

    <T> List<T> hGetListData(final String key, final String field, final Class<T> clazz);

    void leftPush(final String key, final String value);

    <T> T leftPop(final String key, long timeout, TimeUnit unit, final Class<T> clazz);


}
