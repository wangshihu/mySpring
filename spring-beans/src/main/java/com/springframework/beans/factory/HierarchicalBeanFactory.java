package com.springframework.beans.factory;

/**
 * 可继承的beanFactory,寻找parentFactroy
 * Created by hadoop on 2015/5/6 0006.
 */
public interface HierarchicalBeanFactory extends BeanFactory {
    /**
     * Return the parent bean factory, or {@code null} if there is none.
     */
    BeanFactory getParentBeanFactory();

    /**
     * Return whether the local bean factory contains a bean of the given name,
     * ignoring beans defined in ancestor contexts.
     */
    boolean containsLocalBean(String name);
}
