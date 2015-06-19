package com.springframework.beans.factory.support;

import com.springframework.beans.factory.config.BeanDefinition;
import com.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * Created by hadoop on 2015/5/8 0008.
 */
public class RootBeanDefinition extends AbstractBeanDefinition {
    boolean allowCaching = true;

    private BeanDefinitionHolder decoratedDefinition;

    private volatile Class<?> targetType;

    boolean isFactoryMethodUnique = false;

    final Object constructorArgumentLock = new Object();

    final Object postProcessingLock = new Object();

    /** Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied */
    boolean postProcessed = false;

    /** Package-visible field for caching the resolved constructor or factory method */
    Object resolvedConstructorOrFactoryMethod;

    /**
     * Create a new RootBeanDefinition as deep copy of the given
     * bean definition.
     * @param original the original bean definition to copy from
     */
    public RootBeanDefinition(RootBeanDefinition original) {
        super(original);
        this.allowCaching = original.allowCaching;
        this.decoratedDefinition = original.decoratedDefinition;
        this.targetType = original.targetType;
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;
    }

    /**
     * Create a new RootBeanDefinition as deep copy of the given
     * bean definition.
     */
    RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    @Override
    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }
}
