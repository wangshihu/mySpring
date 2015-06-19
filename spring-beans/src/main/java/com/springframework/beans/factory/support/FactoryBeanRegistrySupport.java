package com.springframework.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hadoop on 2015/5/8 0008.
 */
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {
    /** Cache of singleton objects created by FactoryBeans: FactoryBean name --> object */
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>(16);

    /**
     * Obtain an object to expose from the given FactoryBean, if available
     * in cached form. Quick check for minimal synchronization.
     *
     * @param beanName the name of the bean
     * @return the object obtained from the FactoryBean,
     * or {@code null} if not available
     */
    protected Object getCachedObjectForFactoryBean(String beanName) {
        Object object = this.factoryBeanObjectCache.get(beanName);
        return (object != NULL_OBJECT ? object : null);
    }

}
