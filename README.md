# spring-data-redis-tools
- RedisTemplate封装工具类 `redisTools`
- 可视化分布式ID生成器 `distributedId`
- 可靠分布式锁工具类 `distributedLock`（lua脚本实现原子性解决断电问题、valueId避免错误释放问题）
***
## Maven
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
## RedisConfiguration
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author wellJay
 */
@Configuration
@EnableCaching
public class RedisConfiguration {
    //过期时间一天
    private static final int DEFAULT_EXPIRE_TIME = 3600 * 24;

    //从配置文件读取redis参数
    @Autowired
    private CloudConfigProperties cloudConfigProperties;

    /**
     * jedisPoolConfig config
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(cloudConfigProperties.getRedis().getMaxIdle());
        jedisPoolConfig.setMinIdle(cloudConfigProperties.getRedis().getMinIdle());
        jedisPoolConfig.setTestOnBorrow(cloudConfigProperties.getRedis().getTestOnBorrow());
        jedisPoolConfig.setTestOnReturn(cloudConfigProperties.getRedis().getTestOnReturn());
        return jedisPoolConfig;
    }

    /**
     * JedisConnectionFactory
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(cloudConfigProperties.getRedis().getHost());
        jedisConnectionFactory.setPort(cloudConfigProperties.getRedis().getPort());
        jedisConnectionFactory.setPassword(cloudConfigProperties.getRedis().getPassword());
        jedisConnectionFactory.setTimeout(cloudConfigProperties.getRedis().getTimeout());
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        return jedisConnectionFactory;
    }
    
    /**
     * RedisTemplate
     * 从执行时间上来看，JdkSerializationRedisSerializer是最高效的（毕竟是JDK原生的），但是是序列化的结果字符串是最长的。
     * JSON由于其数据格式的紧凑性，序列化的长度是最小的，时间比前者要多一些。
     * 所以个人的选择是倾向使用JacksonJsonRedisSerializer作为POJO的序列器。 
     */
    @Bean
    public RedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        //设置普通value序列化方式
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new JdkSerializationRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate);
        redisCacheManager.setDefaultExpiration(DEFAULT_EXPIRE_TIME);
        return redisCacheManager;
    }
}
```

## How To Use
1、注入util方式，适用于复杂的业务处理
```java
@Autowired
private RedisCacheUtil redisCacheUtil;
```
2、Spring注解方式适用于简单的数据缓存
```java
@Cacheable(value = Constants.Redis.SYSTEM, key = ACTIONS_CACHE_KEY)
```
