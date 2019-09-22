package com.rdpaas.easyconfig.ann;

import com.rdpaas.easyconfig.boot.EasyConfigSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启easyconfig的注解，其实springboot里各种开启接口，只是使用spring
 * 的importSelector玩了一个花样，所以这里是关键@Import(EasyConfigSelector.class)
 * 具体可以看spring5中org.springframework.context.annotation.ConfigurationClassParser#processImports(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.Collection, boolean)
 * @author rongdi
 * @date 2019-09-22 8:01:09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(EasyConfigSelector.class)
public @interface EnableEasyConfig {
}
