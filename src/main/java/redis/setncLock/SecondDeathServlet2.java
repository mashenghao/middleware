package redis.setncLock;

import redis.clients.jedis.Jedis;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.locks.Lock;


/**
 * 秒杀活动
 *
 * @author: mahao
 * @date: 2019/10/28
 */
@WebServlet(urlPatterns = "/sale2")
public class SecondDeathServlet2 extends HttpServlet {
    final static Long NO;


    static {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.auth("123456");
        NO = jedis.incr("NO");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long setnx = 0l;
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.auth("123456");
        try {
            setnx = jedis.setnx("LOCK:SALE", Thread.currentThread().getName());
            if (setnx == 0) {
                return;
            }

            int sale = Integer.parseInt(jedis.get("SALE"));
            String result;
            if (sale > 0) {
                result = NO + " : success： " + sale;
                sale--;
                jedis.set("SALE", String.valueOf(sale));//更新回缓存
            } else {
                result = NO + " : fail";
            }
            System.out.println(result);
            resp.setContentType("text/plain; charset=utf-8");
            resp.getWriter().write(result);

        } finally {
            if (setnx == 1) {
                jedis.del("LOCK:SALE");
            }
            jedis.close();
        }

    }
}
