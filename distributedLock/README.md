- `RedisLockTool.tryGetDistributedLock();` GET LOCK  key = caller className&method   value = Current Thread Id
- `RedisLockTool.tryGetDistributedLock(String lockKey, String requestId);` GET LOCK
- `RedisLockTool.tryGetDistributedLock(String lockKey, String requestId, Integer expireSecond);`
- `RedisLockTool.tryGetDistributedLock(String lockKey, String requestId, Integer expireSecond, Long loopTimes, Long sleepInterval);`  
  
redis集群情况下推荐[RedLock](https://github.com/redisson/redisson)
