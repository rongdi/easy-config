package com.rdpaas.easyconfig.context;

import com.rdpaas.easyconfig.ann.RefreshScope;
import com.rdpaas.easyconfig.observer.ObserverType;
import com.rdpaas.easyconfig.observer.Observers;
import com.rdpaas.easyconfig.utils.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 自定义的springboot上下文类
 * @author rongdi
 * @date 2019-09-21 10:30:01
 */
@Component
public class SpringBootContext implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(SpringBootContext.class);


    private final static String REFRESH_SCOPE_ANNOTATION_NAME = "com.rdpaas.easyconfig.ann.RefreshScope";

    private final static Map<Class<?>, SpringAnnotatedRefreshScopeBeanInvoker> refreshScopeBeanInvokorMap = new HashMap<>();

    private final static String COLON = ":";

    private final static String SET_PREFIX = "set";

    private final static String VALUE_REGEX = "\\$\\{(.*)}";

    private static ApplicationContext applicationContext;

    private static Environment environment;

    private static String filePath;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        applicationContext = ac;
        try {
            /**
             * 初始化准备好哪些类需要更新配置，放入map
             */
            init();

            /**
             * 如果有配置文件中配置了文件路径,并且是本地文件，则开启对应位置的文件监听
             */
            if(filePath != null && !PropUtil.isWebProp(filePath)) {
                File file = new File(filePath);
                String dir = filePath;
                /**
                 * 谁让java就是nb，只能监听目录
                 */
                if(!file.isDirectory()) {
                    dir = file.getParent();
                }
                /**
                 * 开启监听
                 */

                Observers.startWatch(ObserverType.LOCAL_FILE, this, dir);
            }
        }  catch (Exception e) {
            logger.error("init refresh bean error",e);
        }

    }

    /**
     * 刷新spring中被@RefreshScope修饰的类或者方法中涉及到配置的改变，注意该类可能被@Component修饰，也有可能被@Configuration修饰
     * 1.类中被@Value修饰的成员变量需要重新修改更新后的值（
     * 2.类中使用@Bean修饰的方法，如果该方法需要的参数中有其他被@RefreshScope修饰的类的对象，这个方法生成的类也会一同改变
     * 3.类中使用@Bean修饰的方法循环依赖相互对象会报错，因为这种情况是属于构造方法层面的循环依赖，spring里也会报错，
     * 所以我们也不需要考虑循环依赖
     */
    private void init() throws ClassNotFoundException {
        /**
         * 将applicationContext转换为ConfigurableApplicationContext
         */
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
        /**
         * 获取bean工厂并转换为DefaultListableBeanFactory
         */
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
        /**
         * 获取工厂里的所有beanDefinition，BeanDefinition作为spring管理的对象的创建模板，可以类比java中的Class对象，
         */
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for(String beanName : beanDefinitionNames) {

            BeanDefinition bd = defaultListableBeanFactory.getBeanDefinition(beanName);
            /**
             * 使用注解加载到spring中的对象都属于AnnotatedBeanDefinition，毕竟要实现刷新配置也要使用@RefreshScope
             * 没有人丧心病狂的使用xml申明一个bean并且在类中加一个@RefreshScope吧，这里就不考虑非注解方式加载的情况了
             */
            if(bd instanceof AnnotatedBeanDefinition) {
                /**
                 * 得到工厂方法的元信息，使用@Bean修饰的方法放入beanDefinitionMap的beanDefinition对象这个值都不会为空
                 */
                MethodMetadata factoryMethodMeta = ((AnnotatedBeanDefinition) bd).getFactoryMethodMetadata();
                /**
                 * 如果不为空，则该对象是使用@Bean在方法上修饰产生的
                 */
                if(factoryMethodMeta != null) {
                    /**
                     * 如果该方法没有被@RefreshScope注解修饰，则跳过
                     */
                    if(!factoryMethodMeta.isAnnotated(REFRESH_SCOPE_ANNOTATION_NAME)) {
                        continue;
                    }

                    /**
                     * 拿到未被代理的Class对象，如果@Bean修饰的方法在@Configuration修饰的类中，会由于存在cglib代理的关系
                     * 拿不到原始的Method对象
                     */
                    Class<?> clazz = Class.forName(factoryMethodMeta.getDeclaringClassName());
                    Method[] methods = clazz.getDeclaredMethods();
                    /**
                     * 循环从class对象中拿到的所有方法对象，找到当前方法并且被@RefreshScope修饰的方法构造invoker对象
                     * 放入执行器map中，为后续处理@ConfigurationProperties做准备
                     */
                    for(Method m : methods) {
                        if(factoryMethodMeta.getMethodName().equals(m.getName()) && m.isAnnotationPresent(RefreshScope.class)) {
                            refreshScopeBeanInvokorMap.put(Class.forName(factoryMethodMeta.getReturnTypeName()),
                            new SpringAnnotatedRefreshScopeBeanInvoker(true, defaultListableBeanFactory, beanName, (AnnotatedBeanDefinition)bd, clazz,m));
                        }
                    }
                } else {
                    /**
                     * 这里显然是正常的非@Bean注解产生的bd对象了，拿到元信息判断是否被@RefreshScope修饰,这里可不能用
                     * bd.getClassName这个拿到的是代理对象，里面自己定义的属性已经被去掉了，更加不可能拿到被@Value修饰
                     * 的属性了
                     */
                    AnnotationMetadata at = ((AnnotatedBeanDefinition) bd).getMetadata();
                    if(at.isAnnotated(REFRESH_SCOPE_ANNOTATION_NAME)) {
                        Class<?> clazz = Class.forName(at.getClassName());
                        /**
                         * 先放入执行器map，后续循环处理，其实为啥要做
                         */
                        refreshScopeBeanInvokorMap.put(clazz,
                            new SpringAnnotatedRefreshScopeBeanInvoker(false, defaultListableBeanFactory, beanName, (AnnotatedBeanDefinition)bd, clazz,null));

                    }
                }

            }
        }
    }

    /**
     * 根据传入属性刷新spring容器中的配置
     * @param props
     */
    public void refreshConfig(Map<String,Object> props) throws InvocationTargetException, IllegalAccessException {
        if(props.isEmpty() || refreshScopeBeanInvokorMap.isEmpty()) {
            return;
        }

        /**
         * 循环遍历要刷新的执行器map，这里为啥没用foreach就是因为没法向外抛异常，很让人烦躁
         */
        for(Iterator<Map.Entry<Class<?>, SpringAnnotatedRefreshScopeBeanInvoker>> iter = refreshScopeBeanInvokorMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Class<?>, SpringAnnotatedRefreshScopeBeanInvoker> entry = iter.next();
            SpringAnnotatedRefreshScopeBeanInvoker invoker = entry.getValue();
            boolean isMethod = invoker.isMethod();
            /**
             * 判断执行器是不是代表的一个@Bean修饰的方法
             */
            if(isMethod) {
                /**
                 * 使用执行器将属性刷新到@Bean修饰的方法产生的对象中,这里暂时不需要处理，仅仅@Value注解不需要处理@Bean
                 * 修饰的方法
                 * TODO
                 */
            } else {
                /**
                 * 使用执行器将属性刷新到对象中
                 */
                invoker.refreshPropsIntoField(props);
            }
        }

    }

    public static void setFilePath(String filePath) {
        SpringBootContext.filePath = filePath;
    }

}
