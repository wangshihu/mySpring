package com.springframework.beans.factory.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {
    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    private final BeanDefinitionRegistry registry;


    private ClassLoader beanClassLoader;

    public AbstractBeanDefinitionReader(BeanDefinitionRegistry registry){
        this.registry = registry;
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }
}
