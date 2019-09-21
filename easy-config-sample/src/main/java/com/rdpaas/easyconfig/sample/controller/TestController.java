package com.rdpaas.easyconfig.sample.controller;

import com.rdpaas.easyconfig.sample.bean.TestProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @RequestMapping("/getBean")
    @ResponseBody
    public Object getConfig(String beanName) {
        Object obj = applicationContext.getBean(beanName);
        /**
         * 这里被代理过无法直接被序列化成json,只为演示出属性已经改变的效果，直接返回一个toString
         */
        if(obj instanceof TestProperties) {
            obj = obj.toString();
        }
        return obj;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
