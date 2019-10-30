package chapter1;

import bean.User;
import bean.Util;
import com.sun.tracing.dtrace.ArgsAttributes;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jedis操作redis
 *
 * @author: mahao
 * @date: 2019/10/7
 */
public class Jedis1 {

    final static String host = "127.0.0.1";
    final static int port = 6379;

    /**
     * 操作string数据
     *
     * @param args
     */
    public static void main(String[] args) {

        Jedis jedis = new Jedis(host, port);
        String auth = jedis.auth("123456");
        jedis.set("ORDER:03", "order[订单3]");
        String s = jedis.get("ORDER:03");
        System.out.println(s);

        //setnx命令，不存在key，则赋值
        Long num = jedis.setnx("ORDER:03", "新订单3");
        System.out.println(num);//返回更改的记录数

        //getset获取旧的，并设置新的
        String getset1 = jedis.getSet("ORDER:05", "新订单3");//不存在旧值，返回null，并设置新的
        String getset2 = jedis.getSet("ORDER:03", "新订单3");
        System.out.println(getset1);
        System.out.println(getset2);

        Long incr = jedis.incr("k1");//自增1,返回增加后的结果
        Long by = jedis.incrBy("k1", 10);//自增10

        Boolean exists = jedis.exists("k1");//存在指定key

        jedis.persist("k1"); //让key持续保存

        jedis.close();
    }

    /**
     * 测试hash类型数据
     */
    @Test
    public void testHash() {

        Jedis jedis = new Jedis(host, port);
        String auth = jedis.auth("123456");

        //hset设置单个值
        Long hset = jedis.hset("USER:01", "name", "mahao");
        Long hset2 = jedis.hset("USER:01", "age", "18");//向hash表中存数据

        //hmset 设置多个值
        jedis.hmset("USER:02", new HashMap<>());

        ////////////////////////
        //获取单个值
        String name = jedis.hget("USER：01", "name");
        //获取多个值
        List<String> hmget = jedis.hmget("USER:01", "name", "age");
        //获取hashmap的keys
        Set<String> hkeys = jedis.hkeys("USER:01");

        /////////////////////
        //删除某个属性，或多个
        Long hdel = jedis.hdel("USER:01", "name", "gae");

        ////////////其余操作
        //为某个属性自增操作
        Long hincrBy = jedis.hincrBy("USER:01", "age", 18);

    }

    @Test
    public void testHash2() {
        Jedis jedis = new Jedis(host, port);
        String auth = jedis.auth("123456");

        if (jedis.exists("USER:1001")) {
            Map<String, String> map = jedis.hgetAll("USER:1001");
            System.out.println(map);
        } else {
            User user = new User(1001, "李硕", 18);//数据库查询
            jedis.hset("USER:" + user.getId(), "name", user.getName());
        }


    }


    @Test
    public void demo3() {
        Jedis jedis = Util.getJedis();
        Boolean exists = jedis.exists("CACHE:01");
        System.out.println(exists);
    }
}
