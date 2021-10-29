package com.qingyun.network.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description： 有关多线程的工具类
 * @author: 張青云
 * @create: 2021-10-27 19:32
 **/
public class ThreadUtil {
    //  CPU核数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    //  空闲保活时限，单位秒
    private static final int KEEP_ALIVE_SECONDS = 30;

    //  有界队列size
    private static final int QUEUE_SIZE = 10000;

    //  混合型线程池参数
    private static final int MIXED_CORE = 128;  //混合线程池核心线程数
    private static final int MIXED_MAX = 128;  //最大线程数
    private static final String MIXED_THREAD_AMOUNT = "mixed.thread.amount";

    /**
     * 混合型线程池
     */
    static final class MixedTargetThreadPool {
        //  首先从环境变量 mixed.thread.amount 中获取预先配置的线程数
        //  如果没有对 mixed.thread.amount 做配置，则使用常量 MIXED_MAX 作为线程数
        private static final int max = (null != System.getProperty(MIXED_THREAD_AMOUNT)) ?
                Integer.parseInt(System.getProperty(MIXED_THREAD_AMOUNT)) : MIXED_MAX;
        //  自定义线程池
        private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
                MIXED_CORE,
                max,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(QUEUE_SIZE),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        static {
            EXECUTOR.allowCoreThreadTimeOut(true);
            //  钩子函数，用来关闭线程池
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    shutdownThreadPoolGracefully(EXECUTOR);
                }
            }));
        }
    }

    /**
     * 获取混合型任务线程池
     * @return 混合型任务线程池
     */
    public static ThreadPoolExecutor getMixedTargetThreadPool() {
        return MixedTargetThreadPool.EXECUTOR;
    }

    /**
     * 关闭线程池
     * @param threadPool 线程池
     */
    public static void shutdownThreadPoolGracefully(ExecutorService threadPool) {
        if (threadPool.isTerminated()) {
            return;
        }
        try {
            threadPool.shutdown();  // 拒绝接受新任务
            //  等待60s，等待线程池中的任务完成执行
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                //  调用shutdownNow取消正在执行的任务
                threadPool.shutdownNow();
            }
        } catch (Exception e) {
            System.err.println("关闭线程池出现异常");
        }
    }
}
