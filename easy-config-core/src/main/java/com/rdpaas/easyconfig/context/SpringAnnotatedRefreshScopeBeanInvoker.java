package com.rdpaas.easyconfig.context;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 封装的执行器，主要负责真正修改属性值
 * @author rongdi
 * @date 2019-09-21 10:10:01
 */
public class SpringAnnotatedRefreshScopeBeanInvoker {

    private final static String SET_PREFIX = "set";

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
    public void refreshPropsIntoField(Map<String,Object> props) throws IllegalAccessException {
        /**
         * 先根据beanName再根据beanType获取spring容器中的对象
         */
        Object bean = defaultListableBeanFactory.getBean(beanName);
        if(bean == null) {
            bean = defaultListableBeanFactory.getBean(clazz);
        }

        /**
         * 获取类上可能被修饰的注解
         */
        ConfigurationProperties cp = clazz.getAnnotation(ConfigurationProperties.class);
        String prefix = "";
        if(cp != null && !StringUtils.isEmpty(cp.prefix())) {
            prefix = cp.prefix() + ".";
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
            if (valueAnn == null && "".equals(prefix)) {
                continue;
            }

            String key = "";
            /**
             * 如果没有@Value注解则直接根据前缀拼接属性名作为key，否则以前缀拼接@Value里的key
             */
            if(valueAnn == null) {
                key = prefix + field.getName();
            } else {
                key = valueAnn.value();
                /**
                 * 提取@Value("${xx.yy:dd}")中的key：xx.yy
                 */
                key = key.replaceAll(VALUE_REGEX,"$1");
                /**
                 * 如果前缀不为空，拼接上前缀
                 */
                key = prefix + key.split(COLON)[0];
            }

            /**
             * 如果属性map中包含@Value注解中的key，强行使用反射修改里面的值,
             * 严格意义来说应该是使用对应setXX方法修改属性值，这里图方便直接使用属性修改了
             */
            if (props.containsKey(key)) {
                field.setAccessible(true);
                field.set(bean, props.get(key));
            }
        }
    }

    /**
     * 把属性值刷新到Bean方法返回的对象中
     * @param props
     */
    public void refreshPropsIntoBean(Map<String,Object> props) throws InvocationTargetException, IllegalAccessException {
        if(!method.isAnnotationPresent(ConfigurationProperties.class)) {
            return;
        }

        /**
         * 获取方法上可能被修饰的注解
         */
        ConfigurationProperties cp = method.getAnnotation(ConfigurationProperties.class);
        /**
         * 获取到注解上的前缀信息并且拼上
         */
        String prefix = cp.prefix() + ".";
        /**
         * 获取@Bean方法的返回值类型
         */
        Class<?> returnClazz = method.getReturnType();

        /**
         * 先根据beanName再根据返回的beanType获取spring容器中的对象
         */
        Object bean = defaultListableBeanFactory.getBean(beanName);
        if(bean == null) {
            bean = defaultListableBeanFactory.getBean(returnClazz);
        }

        /**
         * 循环返回类型里的所有setXX方法，调用对应的方法修改返回对象里的属性值
         */
        Method[] methods = returnClazz.getDeclaredMethods();
        for(Method m : methods) {
            /**
             * 根据set方法获取对应的属性名称
             */
            String name = getNameBySetMethod(m);
            if(name == null) {
                continue;
            }
            String key = prefix + name;
            if (props.containsKey(key)) {
                m.invoke(bean,props.get(key));
            }
        }
    }

    /**
     * 根据set方法获取对应的属性名称
     */
    private String getNameBySetMethod(Method setMethod) {
        String setMethodName = setMethod.getName();
        /**
         * 如果方法名为空
         * 如果方法不是以set开头
         * 如果方法名长度小于4
         * 如果set后第一个字母不是大写
         * 这些通通不是setXX方法
         */
        if(setMethodName == null || !setMethodName.startsWith(SET_PREFIX) || setMethodName.length() < 4 || !Character.isUpperCase(setMethodName.charAt(3))) {
            return null;
        }
        /**
         * 获取把名称第一位大写变成小写的属性名
         */
        String tempName = setMethodName.substring(3);
        return tempName.substring(0,1).toLowerCase() + tempName.substring(1);
    }

}
