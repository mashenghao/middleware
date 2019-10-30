package chapter1;

import bean.User;
import redis.clients.jedis.Jedis;

/**
 * 用户登录案例：
 * <p>
 * 用户在2个小时内，仅允许输入密码次数为3次，如果输入错误，限制5个小时登录；
 *
 * @author: mahao
 * @date: 2019/10/8
 */
public class LoginService {

    public static final int SUCCESS = -1;
    public static final int FAILED = 3;
    private final static String PREX = "USER:";
    private final Jedis jedis = new Jedis("127.0.0.1", 6379);

    public int login(User user) {
        jedis.auth("123456");
        if (jedis.exists(PREX + user.getId())) {
            Integer i = Integer.valueOf(jedis.get(PREX + user.getId()));
            if (i == FAILED) {
                System.out.println(jedis.ttl(PREX + user.getId()));
                return FAILED;
            }
        }
        if (1001 == user.getId() && "zs".equals(user.getName())) {
            jedis.del(PREX + user.getId());
            return SUCCESS;
        } else {
            long incr = jedis.incr(PREX + user.getId());
            if (incr == 1) {
                jedis.expire(PREX + user.getId(), 60 * 5);
            }
            return (int) incr;
        }
    }

    public static void main(String[] args) {
        LoginService service = new LoginService();
        User user = new User(1001, "z2", 18);
        System.out.println(service.login(user));
    }
}
