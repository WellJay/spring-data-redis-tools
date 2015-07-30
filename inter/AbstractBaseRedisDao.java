package com.brandbigdata.rep.redis.inter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public abstract class AbstractBaseRedisDao<K, V> {
	@Autowired  
    protected RedisTemplate<K, V> redisTemplate;  
  
    /** 
     * 设置redisTemplate 
     * @param redisTemplate the redisTemplate to set 
     */  
    public void setRedisTemplate(RedisTemplate<K, V> redisTemplate) {  
        this.redisTemplate = redisTemplate;
    }  
      
    /** 
     * 获取 String  RedisSerializer 
     * <br>------------------------------<br> 
     */  
    protected RedisSerializer<String> getStringSerializer() {  
        return redisTemplate.getStringSerializer();  
    }
    
}
