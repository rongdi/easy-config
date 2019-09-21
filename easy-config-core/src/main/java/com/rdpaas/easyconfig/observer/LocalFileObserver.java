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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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
                    WatchKey watchKey = watchService.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    Set<String> fileNames = new HashSet<>();
                    for(WatchEvent<?> event : watchEvents){
                        String path =  filePath + File.separator +event.context();
                        fileNames.add(path);
                    }
                    for(String fileName:fileNames) {
                        System.out.println("触发更新操作:"+fileName);
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
        File resourceFile = new File(String.valueOf(data[0]));
        FileSystemResource resource = new FileSystemResource(resourceFile);
        try {
            Properties prop = PropertiesLoaderUtils.loadProperties(resource);
            Map<String,Object> props = new HashMap<>();
            prop.forEach((key,value) -> {
                props.put(String.valueOf(key),value);
            });
            context.refreshConfig(props);
        } catch(InvocationTargetException | IllegalAccessException e1){
            logger.error("refresh config error",e1);
        }catch (Exception e) {
            logger.error("load config error",e);
        }
    }


}
