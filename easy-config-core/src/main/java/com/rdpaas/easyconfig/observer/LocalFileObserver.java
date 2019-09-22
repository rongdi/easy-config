package com.rdpaas.easyconfig.observer;

import com.rdpaas.easyconfig.context.SpringBootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * 本地文件目录监听器
 */
public class LocalFileObserver extends Observer {

    private Logger logger = LoggerFactory.getLogger(LocalFileObserver.class);

    @Override
    public void startWatch(ExecutorService executorService, SpringBootContext context, String filePath) throws IOException {
        isRun = true;
        /**
         * 设置需要监听的文件目录（只能监听目录）
         */
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path p = Paths.get(filePath);
        /**
         * 注册监听事件，修改，创建，删除
         */
        p.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_CREATE);
        executorService.execute(() -> {
            try {
                while(isRun){
                    /**
                     * 拿出一个轮询所有event，如果有事件触发watchKey.pollEvents();这里就有返回
                     * 其实这里类似于nio中的Selector的轮询，都是属于非阻塞轮询
                     */
                    WatchKey watchKey = watchService.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for(WatchEvent<?> event : watchEvents){
                        /**
                         * 拼接一个文件全路径执行onChanged方法刷新配置
                         */
                        String fileName =  filePath + File.separator +event.context();
                        logger.info("start update config event,fileName:{}",fileName);
                        onChanged(context,fileName);
                    }
                    watchKey.reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onChanged(SpringBootContext context, Object... data) {
        /**
         * 取出传递过来的参数构造本地资源文件
         */
        File resourceFile = new File(String.valueOf(data[0]));
        FileSystemResource resource = new FileSystemResource(resourceFile);
        try {
            /**
             * 使用spring工具类加载资源，spring真是个好东西，你能想到的基本都有了
             */
            Properties prop = PropertiesLoaderUtils.loadProperties(resource);
            Map<String,Object> props = new HashMap<>();
            prop.forEach((key,value) -> {
                props.put(String.valueOf(key),value);
            });
            /**
             * 调用SpringBootContext刷新配置
             */
            context.refreshConfig(props);
        } catch(InvocationTargetException | IllegalAccessException e1){
            logger.error("refresh config error",e1);
        }catch (Exception e) {
            logger.error("load config error",e);
        }
    }


}
