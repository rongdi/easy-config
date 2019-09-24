package com.rdpaas.easyconfig.context;

import java.util.Properties;

/**
 * 环境全局对象
 */
public class EnvironmentContext {

    /**
     * 当前加载的配置
     */
    public static Properties currProperties;

    /**
     * easy-confi需要的配置文件，放在bootstrap.properties中
     */
    public static Properties easyConfigProperties;

}
