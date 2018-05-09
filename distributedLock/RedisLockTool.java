import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author wenjie
 * @date 2018/5/7 0007 16:44
 */
@Component
public final class RedisLockTool {

    private static final Long SUCCESS = 1L;
    public static final String LOCK_SCRIPT_STR = "if redis.call('set',KEYS[1],ARGV[1],'EX',ARGV[2],'NX') then return 1 else return 0 end";
    public static final String UNLOCK_SCRIPT_STR = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private static RedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

    @Autowired
    public void init(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisScript.setResultType(Long.class);
    }


    /**
     * 加锁
     * @param lockKey key
     * @param requestId 随机请求id
     * @param timeoutSecond 超时秒
     * @return
     */
    public static boolean tryGetDistributedLock(String lockKey, String requestId, Integer timeoutSecond) {
        redisScript.setScriptText(LOCK_SCRIPT_STR);
        Object result = redisTemplate.execute(redisScript, Lists.newArrayList(lockKey), requestId, String.valueOf(timeoutSecond));
        if (SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 释放锁
     * @param lockKey key
     * @param requestId 加锁的请求id
     * @return
     */
    public static boolean releaseDistributedLock(String lockKey, String requestId) {
        redisScript.setScriptText(UNLOCK_SCRIPT_STR);
        Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);
        if (SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}