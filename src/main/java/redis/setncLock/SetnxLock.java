package redis.setncLock;

import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 使用setnx创建最简单的分布式锁
 *
 * @author: mahao
 * @date: 2019/10/28
 */
public class SetnxLock implements Lock {

    private Thread lockThread;
    private final String LOCK_KEY;

    public SetnxLock(String LOCK_KEY) {
        this.LOCK_KEY = LOCK_KEY;

    }

    @Override
    public void lock() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.auth("123456");
        for (; ; ) {//空转知道获取到锁
            Long setnx = jedis.setnx(LOCK_KEY, Thread.currentThread().getName());
            if (setnx == 1) {
                lockThread = Thread.currentThread();
                jedis.close();
                return;
            } else {
                Thread.yield();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {

        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        if (lockThread == Thread.currentThread()) {
            Jedis jedis = new Jedis("127.0.0.1", 6379);
            jedis.auth("123456");
            Long del = jedis.del(LOCK_KEY);
            jedis.close();
        } else {
            throw new IllegalMonitorStateException("非法状态错误");
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
