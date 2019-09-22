package com.rdpaas.easyconfig.boot;

import com.rdpaas.easyconfig.context.SpringBootContext;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 这里就是配合@EnableEasyConfig使用的目的。在注解@EnableEasyConfig上使用@Import(EasyConfigSelector.class)
 * 来让spring在检测到有这个注解时，加载下面selectImports方法里提供的数组里代表的类，其实就是为了避免需要在
 * SpringBootContext类显示使用@Component注解，毕竟万一有人不用这东西或者是别人项目中压根就不配置扫码你的
 * com.rdpaas包那也会出现SpringBootContext类无法正常被扫描导致无法正常进行工作。简单来说自己提供的依赖包应该
 * 尽量直接使用@Component注解让spring管理（鬼知道还要去扫描你的包名呢），需要让用户自己选择是否需要被spring管理
 * @author rongdi
 * @date 2019-09-22 8:05:14
 */
public class EasyConfigSelector implements ImportSelector{

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{SpringBootContext.class.getName()};
    }

}