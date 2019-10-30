package chapter2;

import bean.Util;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * 使用list数据结构实现，任务队列,
 * 模拟快递的运输流程，将已经完成的任务插入到已经完成的list中。
 * 这里使用redis是将它作为任务队列使用。
 *
 * @author: mahao
 * @date: 2019/10/8
 */
public class TaskQueue {

    public static final String ORDER = "ORDER:";
    static Jedis jedis = Util.getJedis();

    public static void main(String[] args) throws InterruptedException {
        final String orderId = "25004";
        TaskQueue task = new TaskQueue();
        task.createTask(orderId);
        List<String> noTask = null;
        List<String> succTask = null;

        while ((noTask = task.listTask(orderId)) != null && noTask.size() > 0) {
            String s = task.touchTask(orderId);
            System.out.println("触发的任务是：  " + s);
            succTask = task.listSucc(orderId);
            System.out.println("尚未完成的任务： " + noTask);
            System.out.println("已经完成的任务： " + succTask);

            System.out.println("===========================================");
            Thread.sleep(2000);
        }

    }


    //1.创建一个新的任务队列
    public void createTask(String orderId) {
        /*
            流程：
            1.发货
            2.到北京
            3.到商丘
            4.签收
         */
        Long rpush = jedis.lpush(ORDER + orderId, "1.发货", " 2.到北京", " 3.到商丘", " 4.签收");
    }

    //2.触发任务
    public String touchTask(String orderId) {
        String rpoplpush = jedis.rpoplpush(ORDER + orderId, ORDER + orderId + ":succ");
        return rpoplpush;//完成的操作
    }

    //已经完成的任务
    public List<String> listSucc(String orderId) {
        return jedis.lrange(ORDER + orderId + ":succ", 0, -1);
    }

    //未完成的任务
    public List<String> listTask(String orderId) {
        return jedis.lrange(ORDER + orderId, 0, -1);
    }
}
