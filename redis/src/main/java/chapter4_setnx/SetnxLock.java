package chapter4_setnx;

import bean.Util;
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
    private Jedis jedis = Util.getJedis();
    private final String LOCK_KEY;

    public SetnxLock(String LOCK_KEY) {
        this.LOCK_KEY = LOCK_KEY;
    }

    @Override
    public void lock() {
        for (; ; ) {//空转知道获取到锁
            Long setnx = jedis.setnx(LOCK_KEY, Thread.currentThread().getName());
            if (setnx == 1) {
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
        if (jedis.setnx(LOCK_KEY, Thread.currentThread().getName()) == 1) {
            lockThread = Thread.currentThread();
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        if (lockThread == Thread.currentThread()) {
            Long del = jedis.del(LOCK_KEY);
        } else {
            throw new IllegalMonitorStateException("非法状态错误");
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
