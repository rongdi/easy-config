package com.rdpaas.easyconfig.boot;

import com.rdpaas.easyconfig.context.EnvironmentContext;
import com.rdpaas.easyconfig.context.SpringBootContext;
import com.rdpaas.easyconfig.utils.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * 使用环境的后置处理器，将自己的配置放在优先级最高的最前面，这里其实是仿照springboot中
 * SpringApplication构造方法 ->setInitializers()->getSpringFactoriesInstances()->loadFactoryNames()->
 * loadSpringFactories(@Nullable ClassLoader classLoader)断点到里面可以发现这里会加载各个jar包
 * FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories"文件，所以这里把这个类配置本模块的同样
 * 位置，内容为org.springframework.boot.env.EnvironmentPostProcessor=com.rdpaas.easyconfig.boot.InitSettingsEnvironmentPostProcessor
 * 这里其实java的spi的方式，springboot中大量使用这种花样
 * @author rongdi
 * @date 2019-09-21 11:00:01
 */
public class InitSettingsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private Logger logger = LoggerFactory.getLogger(InitSettingsEnvironmentPostProcessor.class);

    private final static String FILE_KEY = "easyconfig.config.file";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication application) {
        /**
         * 得到当前环境的所有配置
         */
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        try {
            /**
             * 拿到bootstrap.properties文件，并读取
             */
            File resourceFile = new File(InitSettingsEnvironmentPostProcessor.class.getResource("/bootstrap.properties").getFile());
            FileSystemResource resource = new FileSystemResource(resourceFile);
            Properties prop = PropertiesLoaderUtils.loadProperties(resource);
            /**
             * 找到配置文件中的FILE_KEY配置，这个配置表示你想把配置文件放在哪个目录下
             */
            String filePath = prop.getProperty(FILE_KEY);

            EnvironmentContext.easyConfigProperties = prop;

            /**
             * 判断文件资源是网络或者本地文件系统，比如从配置中心获取的就是网络的配置信息
             */
            boolean isWeb = PropUtil.isWebProp(filePath);

            /**
             * 根据资源类型，网络或者本地文件系统初始化好配置信息，其实springcloud中配置服务就是可以
             * 直接通过一个url获取到属性，这个url地址也可以放在这里，spring就是好东西，UrlResource这种工具
             * 也有提供，也免了自己写的麻烦了
             */
            Properties config = new Properties();
            Resource configRes = null;
            if(isWeb) {
                configRes = new UrlResource(filePath);
            } else {
                configRes = new FileSystemResource(filePath);
            }
            try {
                /**
                 * 将资源填充到config中
                 */
                PropertiesLoaderUtils.fillProperties(config, configRes);

                /**
                 * 将当前配置更新到环境中
                 */
                EnvironmentContext.currProperties = config;
                /**
                 * 将自己配置的资源加入到资源列表的最前面，使其具有最高优先级
                 */
                propertySources.addFirst(new PropertiesPropertySource("Config", config));
            } catch (IOException e) {
                logger.error("load config error",e);
            }

            /**
             * 将读出来的filePath设置到环境类中，暂时只搞一个文件，要搞多个文件也很简单
             */
            SpringBootContext.setFilePath(filePath);
        } catch (Exception e) {
            logger.info("load easyconfig bootstrap.properties error",e);
        }
    }



}
