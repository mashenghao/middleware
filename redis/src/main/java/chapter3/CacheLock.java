package chapter3;

import bean.Util;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 加互斥锁解决缓存击穿：
 * 在平常高并发的系统中，大量的请求同时查询一个 key 时，此时这个key正好失效了，就会导致大量的请求都打到数据库上面去。
 * 这种现象我们称为**缓存击穿**。
 * <p>
 * **2.带来的问题**
 * 会造成某一个时候数据库请求量巨大，压力剧增。
 * <p>
 * **3.如何解决**
 * 上面的现象是多个线程同时去查询数据库的这条数据，那么我们可以在第一个查询的请求上使用一个互斥锁来锁住他。
 * <p>
 * 其线程走到这一步拿不到锁，只能等着，等第一个线程查询到了数据，然后做缓存。后面的线程进来发现已经有缓存了，就直接走缓存。
 * <p>
 * <p>
 * 注意Jedis的坑，因为有递归，如果关闭了两次上，会报错。
 *
 * @author: mahao
 * @date: 2019/10/8
 */
public class CacheLock {

    final ReentrantLock lock = new ReentrantLock();

    static AtomicInteger redisCount = new AtomicInteger(0);
    static AtomicInteger sqlCount = new AtomicInteger(0);


    /**
     * 模拟1000个请求，每个请求100次数据
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        CacheLock instance = new CacheLock();
        final String key = "CACHE:01";
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {//1000个任务
            executor.execute(() -> {
                Jedis jedis = Util.getJedis();
                for (int j = 0; j < 20; j++) {//100次请求数据
                    try {
                        instance.getData(key, jedis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                jedis.close();
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println(redisCount.get());
        System.out.println(sqlCount.get());

    }

    /**
     * 从缓存中获取数据，获取区间会发生缓存击穿的问题
     */
    public String getData(String key, Jedis jedis) throws InterruptedException {

        String data = getDataRedis(key, jedis);
        if (data == null) {//缓存为null,加锁从数据库中查询
            if (lock.tryLock()) {//尝试获取锁，成功了去查询数据库，并设置缓存
                data = getDataSQL(key);
                setCache(key, data, jedis);
                lock.unlock();
            } else {//获取锁，失败，修整一会，递归在此获取数据

                Thread.sleep(100);
                //这里的递归操作，则会再去执行从redis查询并获取的操作。
                return getData(key, jedis);
            }
        }
        return data;
    }


    public static String getDataRedis(String key, Jedis jedis) {

        int i = redisCount.incrementAndGet();
        System.out.println("redis--" + i);
        if (jedis.exists(key))
            return jedis.get(key);
        return null;
    }

    //模拟数据库查询
    public static String getDataSQL(String key) {
        int i = sqlCount.incrementAndGet();
        System.out.println("sql--" + i);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "mysql" + key;
    }

    //5秒失效
    public static void setCache(String key, String value, Jedis jedis) {
        jedis.set(key, value);
        jedis.expire(key, 30);
    }
}
