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

    protected volatile boolean isRun = false;

    public abstract void startWatch(ExecutorService executorService, SpringBootContext context, String target) throws IOException;

    public void stopWatch() throws IOException {
        isRun = false;
    }

    public abstract void onChanged(SpringBootContext context, Object... data);
}
