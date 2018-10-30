import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wenjie
 * @create 2017-06-15 19:09
 **/
@Component
public class RedisCacheUtil<T> {

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return 缓存的对象
     */
    public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value);
        return operation;
    }

    public <T> ValueOperations<String, T> setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value, timeout, timeUnit);
        return operation;
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deleteObject(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection
     */
    public void deleteObject(Collection collection) {
        redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList) {
        ListOperations listOperation = redisTemplate.opsForList();
        if (null != dataList) {
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                listOperation.leftPush(key, dataList.get(i));
            }
        }
        return listOperation;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(String key) {
        List<T> dataList = new ArrayList<T>();
        ListOperations<String, T> listOperation = redisTemplate.opsForList();
        Long size = listOperation.size(key);

        for (int i = 0; i < size; i++) {
            dataList.add(listOperation.index(key, i));
        }
        return dataList;
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public Set<T> getCacheSet(String key) {
        Set<T> dataSet = new HashSet<T>();
        BoundSetOperations<String, T> operation = redisTemplate.boundSetOps(key);
        Long size = operation.size();
        for (int i = 0; i < size; i++) {
            dataSet.add(operation.pop());
        }
        return dataSet;
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     * @return
     */
    public <T> HashOperations<String, String, T> setCacheMap(String key, Map<String, T> dataMap) {

        HashOperations hashOperations = redisTemplate.opsForHash();
        if (null != dataMap) {
            for (Map.Entry<String, T> entry : dataMap.entrySet()) {
                hashOperations.put(key, entry.getKey(), entry.getValue());
            }
        }
        return hashOperations;
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(String key) {
        Map<String, T> map = redisTemplate.opsForHash().entries(key);
        return map;
    }


    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     * @return
     */
    public <T> HashOperations<String, Integer, T> setCacheIntegerMap(String key, Map<Integer, T> dataMap) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        if (null != dataMap) {
            for (Map.Entry<Integer, T> entry : dataMap.entrySet()) {
                hashOperations.put(key, entry.getKey(), entry.getValue());
            }
        }
        return hashOperations;
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<Integer, T> getCacheIntegerMap(String key) {
        Map<Integer, T> map = redisTemplate.opsForHash().entries(key);
        return map;
    }


    //=====================GEO地理位置相关=====================

    /**
     * 增加定位点
     *
     * @param x
     * @param y
     * @param member
     * @param time
     * @return
     */
    public boolean addGeo(double x, double y, String member, long time) {
        String key = GEO_KEY;
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            geoOps.add(key, new Point(x, y), member);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            log.error("缓存[" + key + "]" + "失败, point[" + x + "," +
                    y + "], member[" + member + "]" + ", error[" + t + "]");
        }
        return true;
    }


    /**
     * 删除定位点
     *
     * @param members
     * @return
     */
    public boolean removeGeo(String... members) {
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            geoOps.remove(GEO_KEY, members);
        } catch (Throwable t) {
            log.error("移除[" + GEO_KEY + "]" + "失败" + ", error[" + t + "]");
        }
        return true;
    }


    /**
     * 计算定位距离
     *
     * @param member1
     * @param member2
     * @return
     */
    public Distance distanceGeo(String member1, String member2) {
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            return geoOps.geoDist(GEO_KEY, member1, member2);
        } catch (Throwable t) {
            log.error("计算距离[" + GEO_KEY + "]" + "失败, member[" + member1 + "," + member2 + "], error[" + t + "]");
        }
        return null;
    }

    /**
     * 获取坐标
     *
     * @param members
     * @return
     */
    public List<Point> getGeo(String... members) {
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
            return geoOps.position(GEO_KEY, members);
        } catch (Throwable t) {
            log.error("获取坐标[" + GEO_KEY + "]" + "失败]" + ", error[" + t + "]");
        }
        return null;
    }

    /**
     * 基于某个坐标的附近的东西
     *
     * @param x
     * @param y
     * @param distance
     * @param direction
     */
    public List<GeoRadiusDto> raduisGeo(double x, double y, double distance, Sort.Direction direction) {
        List<GeoRadiusDto> radiusDtos = new ArrayList<>();
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

            //设置geo查询参数
            RedisGeoCommands.GeoRadiusCommandArgs geoRadiusArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
            geoRadiusArgs = geoRadiusArgs.includeCoordinates().includeDistance();//查询返回结果包括距离和坐标
            if (Sort.Direction.ASC.equals(direction)) {//按查询出的坐标距离中心坐标的距离进行排序
                geoRadiusArgs.sortAscending();
            } else if (Sort.Direction.DESC.equals(direction)) {
                geoRadiusArgs.sortDescending();
            }
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = geoOps.radius(GEO_KEY, new Circle(new Point(x, y), new Distance(distance, RedisGeoCommands.DistanceUnit.METERS)), geoRadiusArgs);

            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoResultList = geoResults.getContent();
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResultList) {
                String name = geoResult.getContent().getName();
                Point point = geoResult.getContent().getPoint();
                GeoRadiusDto radiusDto = new GeoRadiusDto();
                radiusDto.setMember(name);
                radiusDto.setX(point.getX());
                radiusDto.setY(point.getY());
                radiusDtos.add(radiusDto);
            }
        } catch (Throwable t) {

        }
        return radiusDtos;
    }


    /**
     * 基于某个key的附近的东西
     *
     * @param member
     * @param distance
     * @param direction
     */
    public List<GeoRadiusDto> raduisGeo(String member, double distance, Sort.Direction direction) {
        List<GeoRadiusDto> radiusDtos = new ArrayList<>();
        try {
            GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

            //设置geo查询参数
            RedisGeoCommands.GeoRadiusCommandArgs geoRadiusArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
            geoRadiusArgs = geoRadiusArgs.includeCoordinates().includeDistance();//查询返回结果包括距离和坐标
            if (Sort.Direction.ASC.equals(direction)) {//按查询出的坐标距离中心坐标的距离进行排序
                geoRadiusArgs.sortAscending();
            } else if (Sort.Direction.DESC.equals(direction)) {
                geoRadiusArgs.sortDescending();
            }
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = geoOps.radius(GEO_KEY, member, new Distance(distance, RedisGeoCommands.DistanceUnit.METERS), geoRadiusArgs);

            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoResultList = geoResults.getContent();
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResultList) {
                String name = geoResult.getContent().getName();
                //结果集排除自己
                if (!name.equals(member)) {
                    Point point = geoResult.getContent().getPoint();
                    GeoRadiusDto radiusDto = new GeoRadiusDto();
                    radiusDto.setMember(name);
                    radiusDto.setX(point.getX());
                    radiusDto.setY(point.getY());
                    radiusDtos.add(radiusDto);
                }
            }
        } catch (Throwable t) {

        }
        return radiusDtos;
    }


}
