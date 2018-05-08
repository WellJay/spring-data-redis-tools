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

    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final Long SUCCESS = 1L;

    public static boolean tryGetDistributedLock(String lockKey, String requestId) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText("if redis.call('set',KEYS[1],ARGV[1],'EX','60','NX') then return 1 else return 0 end");
        Object result = redisTemplate.execute(redisScript, Lists.newArrayList(lockKey), requestId);
        if (SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    public static boolean releaseDistributedLock(String lockKey, String requestId){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
        Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);
        if (SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }
}
