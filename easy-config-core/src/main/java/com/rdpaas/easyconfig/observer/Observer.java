package com.rdpaas.easyconfig.observer;

import com.rdpaas.easyconfig.context.SpringBootContext;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * 观察者基类
 * @author rongdi
 * @date 2019-09-21 14:30:01
 */
public abstract class Observer {

    public static String EASYCONFIG_CONFIG_PERIOD = "easyconfig.config.period";

    protected volatile boolean isRun = false;

    public void startWatch(ExecutorService executorService, SpringBootContext context, String target) throws IOException {
        isRun = true;
        onStarted(executorService, context, target);
    }

    public void stopWatch() throws IOException {
        isRun = false;
    }

    public abstract void onStarted(ExecutorService executorService, SpringBootContext context, String target) throws IOException;

    public abstract void onChanged(SpringBootContext context, Object... data);
}
