package bean;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import sun.plugin.com.PropertyGetDispatcher;

/**
 * @author: mahao
 * @date: 2019/10/8
 */
public class Util {

    private final static JedisPool pool;

    static {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(128 );
        pool = new JedisPool(config, "127.0.0.1", 6379, Protocol.DEFAULT_TIMEOUT, "123456",
                Protocol.DEFAULT_DATABASE, null);
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            Jedis jedis = getJedis();
            System.out.println(jedis);

        }
    }
}
