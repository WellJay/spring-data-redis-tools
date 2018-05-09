import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTests {

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

        public static final int RANDOM_NUMBER_RANGE = 3;

        @Override
        public void run() {
            //获取锁， key：caller className&method   value:Current Thread Id
            //也可以传参 key value 可选[expireSecond loopTimes sleepInterval]
            boolean result = RedisLockTool.tryGetDistributedLock();
            if (result) {
                System.out.println(Thread.currentThread().getName() + "： get lock");
            } else {
                System.err.println(Thread.currentThread().getName() + "： get lock timeout");
            }

            //if get the lock
            if (result) {
                try {
                    TimeUnit.SECONDS.sleep(new Random().nextInt(RANDOM_NUMBER_RANGE));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RedisLockTool.releaseDistributedLock();
                System.out.println(Thread.currentThread().getName() + "： release lock");
            }
        }
    }
}

