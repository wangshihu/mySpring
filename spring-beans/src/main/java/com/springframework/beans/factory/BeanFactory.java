package com.springframework.beans.factory;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public interface BeanFactory {
    /**
     *用于区分FactoryBean和普通Bean,如果一个Bean是FatoryBean,例如
     * &myObject 就会加上&前缀
     */
    String FACTORY_BEAN_PREFIX = "&";

    /**
     * 根据名称获得Bean
     */
    Object getBean(String name) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    /**
     * 根绝name和显式的构造参数获得Bean
     */
    Object getBean(String name, Object... args) throws BeansException;

    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
    /**
     * 是否包含Bean
     */
    boolean containsBean(String name);

    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;


    boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
    /**
     * 判断Bean是否匹配Class
     */
    boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;
    /**
     * 获得Bean的Class
     */
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;
    /**
     * 获得Bean的所有别名
     */
    String[] getAliases(String name);
}
