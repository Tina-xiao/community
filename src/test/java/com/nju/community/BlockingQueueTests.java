package com.nju.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {
    public static void main(String[] args) {
        //阻塞队列容量10
        BlockingQueue queue = new ArrayBlockingQueue(10);
        //一个生产者三个消费者同时消费数据
        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();

    }
}

//生产者和消费者都是进程，要继承Runnable接口
class Producer implements Runnable {
    //阻塞队列
    private BlockingQueue<Integer> queue;

    public Producer( BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            //生产者生产100个数据就不生产了
            for(int i=0;i<100;i++) {
                //时间间隔
                Thread.sleep(20);
                //模拟阻塞队列，i就是生产的数据
                 queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产" + queue.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class Consumer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Consumer( BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
                while(true){
                    //消费者两次消费中间的时间间隔随机
                    Thread.sleep(new Random().nextInt(1000));
                    queue.take();
                    System.out.println(Thread.currentThread().getName() + "消费" + queue.size());
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}