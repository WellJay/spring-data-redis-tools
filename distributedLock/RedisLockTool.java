import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
/**
 * @author wenjie
 * @date 2018/5/7 0007 16:44
 */
@Component
public final class RedisLockTool {

    private static final Long SUCCESS = 1L;
    public static final String LOCK_SCRIPT_STR = "if redis.call('set',KEYS[1],ARGV[1],'EX',ARGV[2],'NX') then return 1 else return 0 end";
    public static final String UNLOCK_SCRIPT_STR = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    //default value
    public static final Integer DEFAULT_EXPIRE_SECOND = 60;
    public static final Long DEFAULT_LOOP_TIMES = 10L;
    public static final Long DEFAULT_SLEEP_INTERVAL = 500L;
    public static final String PACKAGE_NAME_SPLIT_STR = "\\.";
    public static final String CLASS_AND_METHOD_CONCAT_STR = "->";

    private static RedisTemplate redisTemplate;

    @Autowired
    public void init(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 得到分布式锁
     * 默认key：调用者类名
     *
     * @return
     * @throws InterruptedException
     */
    public static boolean tryGetDistributedLock() {
        String callerKey = getCurrentThreadCaller();
        String requestId = String.valueOf(Thread.currentThread().getId());
        return tryGetDistributedLock(callerKey, requestId);
    }

    /**
     * @param lockKey   锁名称
     * @param requestId 随机请求id
     * @return
     * @throws InterruptedException
     */
    public static boolean tryGetDistributedLock(String lockKey, String requestId) {
        return tryGetDistributedLock(lockKey, requestId, DEFAULT_EXPIRE_SECOND);
    }

    /**
     * @param lockKey      key
     * @param requestId    随机请求id
     * @param expireSecond 超时秒
     * @return
     * @throws InterruptedException
     */
    public static boolean tryGetDistributedLock(String lockKey, String requestId, Integer expireSecond) {
        return tryGetDistributedLock(lockKey, requestId, expireSecond, DEFAULT_LOOP_TIMES, DEFAULT_SLEEP_INTERVAL);
    }


    /**
     * 加锁
     *
     * @param lockKey       key
     * @param requestId     随机请求id
     * @param expireSecond  超时秒
     * @param loopTimes     循环次数
     * @param sleepInterval 等待间隔（毫秒）
     * @return
     */
    public static boolean tryGetDistributedLock(String lockKey, String requestId, Integer expireSecond, Long loopTimes, Long sleepInterval) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LOCK_SCRIPT_STR, Long.class);
        while (loopTimes-- >= 0) {
            Object result = redisTemplate.execute(redisScript, Lists.newArrayList(lockKey), requestId, String.valueOf(expireSecond));
            if (SUCCESS.equals(result)) {
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleepInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }
        return false;
    }


    /**
     * 释放锁
     *
     * @return
     */
    public static boolean releaseDistributedLock() {
        String callerKey = getCurrentThreadCaller();
        String requestId = String.valueOf(Thread.currentThread().getId());
        return releaseDistributedLock(callerKey, requestId);
    }

    /**
     * 释放锁
     *
     * @param lockKey   key
     * @param requestId 加锁的请求id
     * @return
     */
    public static boolean releaseDistributedLock(String lockKey, String requestId) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(UNLOCK_SCRIPT_STR, Long.class);
        Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);
        if (SUCCESS.equals(result)) {
            return true;
        }
        return false;
    }

    private static String getSimpleClassName(String className) {
        String[] splits = className.split(PACKAGE_NAME_SPLIT_STR);
        return splits[splits.length - 1];
    }

    /**
     * Get caller
     *
     * @return
     */
    private static String getCurrentThreadCaller() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        return getSimpleClassName(stackTraceElement.getClassName()) + CLASS_AND_METHOD_CONCAT_STR + stackTraceElement.getMethodName();
    }
}
