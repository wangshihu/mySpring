package com.springframework.beans.factory.support;

import com.springframework.beans.factory.BeanDefinitionStoreException;
import com.springframework.beans.factory.NoSuchBeanDefinitionException;
import com.springframework.beans.factory.config.BeanDefinition;
import com.springframework.core.AliasRegistry;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public interface BeanDefinitionRegistry extends AliasRegistry {
    /**
     * Register a new bean definition with this registry.
     * Must support RootBeanDefinition and ChildBeanDefinition.
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
            throws BeanDefinitionStoreException;

    /**
     * Remove the BeanDefinition for the given name.
     */
    void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * Return the BeanDefinition for the given bean name.
     */
    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    /**
     * Check if this registry contains a bean definition with the given name.
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * Return the names of all beans defined in this registry.
     * @return the names of all beans defined in this registry,
     * or an empty array if none defined
     */
    String[] getBeanDefinitionNames();

    /**
     * Return the number of beans defined in the registry.
     * @return the number of beans defined in the registry
     */
    int getBeanDefinitionCount();

    /**
     * Determine whether the given bean name is already in use within this registry,
     * i.e. whether there is a local bean or alias registered under this name.
     * @param beanName the name to check
     * @return whether the given bean name is already in use
     */
    boolean isBeanNameInUse(String beanName);
}
