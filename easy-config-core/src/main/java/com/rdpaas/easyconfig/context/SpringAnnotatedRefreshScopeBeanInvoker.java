package com.rdpaas.easyconfig.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 封装的执行器，主要负责真正修改属性值
 * @author rongdi
 * @date 2019-09-21 10:10:01
 */
public class SpringAnnotatedRefreshScopeBeanInvoker {

    private Logger logger = LoggerFactory.getLogger(SpringAnnotatedRefreshScopeBeanInvoker.class);


    private final static String VALUE_REGEX = "\\$\\{(.*)}";

    private final static String COLON = ":";

    private DefaultListableBeanFactory defaultListableBeanFactory;

    private boolean isMethod = false;

    private String beanName;

    private AnnotatedBeanDefinition abd;

    private Class<?> clazz;

    private Method method;


    public SpringAnnotatedRefreshScopeBeanInvoker(boolean isMethod, DefaultListableBeanFactory defaultListableBeanFactory, String beanName, AnnotatedBeanDefinition abd, Class<?> clazz, Method method) {
        this.abd = abd;
        this.isMethod = isMethod;
        this.defaultListableBeanFactory = defaultListableBeanFactory;
        this.beanName = beanName;
        this.clazz = clazz;
        this.method = method;
    }

    public boolean isMethod() {
        return isMethod;
    }

    /**
     * 把属性值刷新到属性中
     * @param props
     */
    public void refreshPropsIntoField(Map<String,Object> props) {
        /**
         * 先根据beanName再根据beanType获取spring容器中的对象
         */
        Object bean = defaultListableBeanFactory.getBean(beanName);
        if(bean == null) {
            bean = defaultListableBeanFactory.getBean(clazz);
        }
        /**
         * 获取所有可用的属性
         */
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            /**
             * 如果属性被@Value修饰
             */
            Value valueAnn = field.getAnnotation(Value.class);
            if (valueAnn == null) {
                continue;
            }
            String key = valueAnn.value();
            if (key == null) {
                continue;
            }
            /**
             * 提取@Value("${xx.yy:dd}")中的key：xx.yy
             */
            key = key.replaceAll(VALUE_REGEX,"$1");
            key = key.split(COLON)[0];
            /**
             * 如果属性map中包含@Value注解中的key，强行使用反射修改里面的值
             */
            if (props.containsKey(key)) {
                field.setAccessible(true);
                try {
                    field.set(bean, props.get(key));
                } catch (Exception e) {
                    logger.info("set field error",e);
                }
            }
        }
    }

}
