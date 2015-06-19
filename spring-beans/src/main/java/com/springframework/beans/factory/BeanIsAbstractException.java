package com.springframework.beans.factory;

/**
 * Created by hadoop on 2015/5/9 0009.
 */
public class BeanIsAbstractException extends BeanCreationException {
    /**
     * Create a new BeanIsAbstractException.
     * @param beanName the name of the bean requested
     */
    public BeanIsAbstractException(String beanName) {
        super(beanName, "Bean definition is abstract");
    }
}
