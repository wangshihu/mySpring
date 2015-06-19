package com.springframework.beans.factory.config;

/**
 * 单例注册接口
 * Created by hadoop on 2015/5/6 0006.
 */
public interface SingletonBeanRegistry {

    /**
     *  根据beanName注册单例的bean
     */
    void registerSingleton(String beanName, Object singletonObject);

    /**
     * 获得单例的Bean在已注册的容器中
     */
    Object getSingleton(String beanName);

    /**
     * 判断容器是否包含单例Bean
     */
    boolean containsSingleton(String beanName);

    /**
     *Return the names of singleton beans registered in this registry.
     */
    String[] getSingletonNames();

    /**
     *Return the number of singleton beans registered in this registry.
     */
    int getSingletonCount();
}
