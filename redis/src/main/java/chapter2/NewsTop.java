package chapter2;

import bean.User;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;

/**
 * 用list数据结构实现新闻top的功能；
 * <p>
 * 当第一次获取时，从数据库查询出，top的数据，存入redis
 * 以后获取，从redis中查询。
 *
 * @author: mahao
 * @date: 2019/10/8
 */
public class NewsTop {

    private final static String LKEY = "USER:TOP:5";
    static Jedis jedis = new Jedis("127.0.0.1", 6379);

    public static void main(String[] args) {
        List<String> list = topNum(5);
        System.out.println(list);
    }

    public static List<String> topNum(int size) {
        jedis.auth("123456");

        if (jedis.exists(LKEY)) {
            System.out.println("redis...");
            List<String> lrange = jedis.lrange(LKEY, 0, 5);
            return lrange;
        } else {
            System.out.println("sql....");
            List<String> news = Arrays.asList("news1", "news2", "news3", "news4", "news5");
            for (String s : news) {
                jedis.rpush(LKEY, s);
            }
            return news;
        }

    }

}
