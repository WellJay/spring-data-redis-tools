import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class Test {

    @Test
    public void distributedLock() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Tester(), "Thread-" + i);
            countDownLatch.countDown();
            thread.start();
        }

        countDownLatch.await();

        System.out.println("down");

        Thread.sleep(Integer.MAX_VALUE);
    }

    class Tester implements Runnable {
        @Override
        public void run() {
            //虚拟requestID
            String requestId = UUID.randomUUID().toString();
            while(true) {
                boolean result = RedisLockTool.tryGetDistributedLock("test", requestId, 60);
                if (result) {
                    System.out.println(Thread.currentThread().getName() + "： 得到锁");
                    break;
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RedisLockTool.releaseDistributedLock("test", requestId);
            System.out.println(Thread.currentThread().getName() + "： 释放锁");
        }
    }
}
