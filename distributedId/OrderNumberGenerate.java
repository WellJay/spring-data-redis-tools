import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by WenJie on 2015/11/3.
 * 可视化分布式唯一订单号生成器 20151103001
 */
@Component
public class OrderNumberGenerate {

    public static final String FROMAT_STR = "yyyyMMddHHmm";
    private static StringRedisTemplate staticTemplate;

    @Autowired
    public static void setStaticTemplate(StringRedisTemplate staticTemplate) {
        OrderNumberGenerate.staticTemplate = staticTemplate;
    }

    private OrderNumberGenerate() {
    }

    /**
     * 生成订单编号
     * date存redis，只要date变化则直接increment=1，否则就是并发冲突直接increment++保证分布式唯一id
     *
     * public static final String FLOWER_ORDER_NUMBER_INCR = "order_number_incr";
     * public static final String FLOWER_ORDER_NUMBER_DATE = "order_number_date";
     *
     * @return
     */
    public static synchronized String getOrderNo() {
        String str = new SimpleDateFormat(FROMAT_STR).format(new Date());
        String date = staticTemplate.opsForValue().get(CacheKey.FLOWER_ORDER_NUMBER_DATE);
        if (date == null || !date.equals(str)) {
            //保存时间
            staticTemplate.opsForValue().set(CacheKey.FLOWER_ORDER_NUMBER_DATE, str);
            //缓存清除
            staticTemplate.delete(CacheKey.FLOWER_ORDER_NUMBER_INCR);
        }
        //缓存++
        Long incrementValue = staticTemplate.opsForValue().increment(CacheKey.FLOWER_ORDER_NUMBER_INCR, 1);
        long orderNo = Long.parseLong(date) * 10000;
        orderNo += incrementValue;
        return orderNo + "";
    }
}
