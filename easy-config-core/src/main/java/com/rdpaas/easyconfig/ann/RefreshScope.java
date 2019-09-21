package com.rdpaas.easyconfig.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义的修饰可以被刷新的注解，模仿springcloud的同名注解
 * @author rongdi
 * @date 2019-09-21 10:00:01
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshScope {

}
