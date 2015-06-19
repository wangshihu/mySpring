package com.springframework.beans.factory.support;

import com.springframework.beans.factory.BeanDefinitionStoreException;
import com.springframework.core.io.Resource;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public interface BeanDefinitionReader {

    /**
     * Return the bean factory to register the bean definitions with.
     * <p>The factory is exposed through the BeanDefinitionRegistry interface,
     * encapsulating the methods that are relevant for bean definition handling.
     */
    BeanDefinitionRegistry getRegistry();


    /**
     * Return the class loader to use for bean classes.
     * <p>{@code null} suggests to not load bean classes eagerly
     * but rather to just register bean definitions with class names,
     * with the corresponding Classes to be resolved later (or never).
     */
    ClassLoader getBeanClassLoader();

    /**
     * Load bean definitions from the specified resource.
     */
    int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;



    /**
     * Load bean definitions from the specified resource location.
     * <p>The location can also be a location pattern, provided that the
     * ResourceLoader of this bean definition reader is a ResourcePatternResolver.
     */
    int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

}
