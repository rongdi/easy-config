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
 * 其实有很多方式实现@EnableXX比如先给SpringBootContext类使用@Component修饰然后使用如下注释部分注解
 * @ComponentScan("com.rdpaas") 或者 @Import({SpringBootContext.class}) 强行扫描你需要扫描的类并加载
 * spring的魅力在于扩展很灵活，只有你想不到没有他做不到，呵呵
 * @author rongdi
 * @date 2019-09-22 8:01:09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(EasyConfigSelector.class)
//@ComponentScan("com.rdpaas")
//@Import({SpringBootContext.class})
public @interface EnableEasyConfig {
}
