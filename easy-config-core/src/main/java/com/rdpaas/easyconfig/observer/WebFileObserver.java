package com.rdpaas.easyconfig.observer;

import com.rdpaas.easyconfig.context.EnvironmentContext;
import com.rdpaas.easyconfig.context.SpringBootContext;
import com.rdpaas.easyconfig.utils.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * 网络文件监听器
 * @author rongdi
 * @date 2019-09-24 19:40:05
 */
public class WebFileObserver extends Observer {

    private Logger logger = LoggerFactory.getLogger(WebFileObserver.class);

    @Override
    public void onStarted(ExecutorService executorService, SpringBootContext context, String target) throws IOException {
        executorService.execute(() -> {
            while(isRun){
                try {
                    Resource configRes = new UrlResource(target);
                    Properties config = new Properties();
                    /**
                     * 将资源填充到config中
                     */
                    PropertiesLoaderUtils.fillProperties(config, configRes);
                    /**
                     * 判断网络配置是否和当前配置有差别,如果有差别则触发更新配置并且将当前配置更新到环境中
                     */
                    if(!PropUtil.isSame(EnvironmentContext.currProperties,config)) {
                        onChanged(context,config);
                        EnvironmentContext.currProperties = config;
                    }

                    /**
                     * 从配置文件中获取到监听的周期，然后休眠指定时间，默认30s
                     */
                    Long period = 30000L;
                    String periodStr = EnvironmentContext.easyConfigProperties.getProperty(EASYCONFIG_CONFIG_PERIOD);
                    if(StringUtils.isEmpty(periodStr)) {
                        period = Long.parseLong(periodStr) * 1000;
                    }
                    Thread.sleep(period);

                } catch (Exception e) {
                    logger.info("load web config error",e);
                }
            }
        });
    }

    @Override
    public void onChanged(SpringBootContext context, Object... data) {
        Properties props = (Properties)data[0];
        /**
         * 调用SpringBootContext刷新配置
         */
        try {
            context.refreshConfig(PropUtil.prop2Map(props));
        } catch (InvocationTargetException e) {
            logger.error("refresh web config error",e);
        } catch (IllegalAccessException e) {
            logger.error("load web  config error",e);
        }
    }
}
