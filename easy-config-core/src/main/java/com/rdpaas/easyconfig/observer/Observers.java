package com.rdpaas.easyconfig.observer;

import com.rdpaas.easyconfig.context.SpringBootContext;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 观察者工具类
 * @author rongdi
 * @date 2019-09-21 15:30:09
 */
public class Observers {

    private final static ExecutorService executorService = new ThreadPoolExecutor(1,1,0, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(10));

    private static Observer currentObserver;
    /**
     * 启动观察者
     * @param type
     * @param context
     * @param target
     * @throws IOException
     */
    public static void startWatch(ObserverType type, SpringBootContext context,String target) throws IOException {
        if(type == ObserverType.LOCAL_FILE) {
            currentObserver = new LocalFileObserver();
            currentObserver.startWatch(executorService,context,target);
        } else if(type == ObserverType.WEB_FILE) {
            currentObserver = new WebFileObserver();
            currentObserver.startWatch(executorService,context,target);
        }
    }

    /**
     * 关闭观察者
     * @param type
     * @throws IOException
     */
    public static void stopWatch(ObserverType type) throws IOException {
          currentObserver.stopWatch();
    }
}
