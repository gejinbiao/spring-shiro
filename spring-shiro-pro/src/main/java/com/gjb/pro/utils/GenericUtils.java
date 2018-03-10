package com.gjb.pro.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

/**
 * @author gejinbiao@ucfgroup.com
 * @Title
 * @Description: 在xml中配置bean，在程序中用来获取容器中的对象实例
 * @Company: ucfgroup.com
 * @Created 2018-03-05
 */
public class GenericUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext; // Spring应用上下文环境

    private ResourceUrlProvider resourceUrlProvider;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        GenericUtils.applicationContext = applicationContext;
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    /*public static <T> T getBean(String name) throws BeansException {
        return (T) applicationContext.getBean(name);
    }

    *//**
     * 获取类型为requiredType的对象
     *
     * @param
     * @return
     * @throws BeansException
     *//*
    public static <T> T getBean(Class<T> clz) throws BeansException {
        @SuppressWarnings("unchecked")
        T result = (T) applicationContext.getBean(clz);
        return result;
    }*/

    public ResourceUrlProvider getResourceUrlProvider() {
        return resourceUrlProvider;
    }

    public void setResourceUrlProvider(ResourceUrlProvider resourceUrlProvider) {
        this.resourceUrlProvider = resourceUrlProvider;
    }
}
