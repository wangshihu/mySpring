package com.springframework.beans.factory;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public class BeanDefinitionStoreException extends  BeansException {
    private String resourceDescription;

    private String beanName;
    public BeanDefinitionStoreException(String msg) {
        super(msg);
    }
    public BeanDefinitionStoreException(String msg, Throwable cause) {
        super(msg, cause);
    }
    public BeanDefinitionStoreException(String resourceDescription, String msg) {
        super(msg);
        this.resourceDescription = resourceDescription;
    }
    public BeanDefinitionStoreException(String resourceDescription, String msg, Throwable cause) {
        super(msg, cause);
        this.resourceDescription = resourceDescription;
    }

    public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg) {
        this(resourceDescription, beanName, msg, null);
    }

    public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg, Throwable cause) {
        super("Invalid bean definition with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, cause);
        this.resourceDescription = resourceDescription;
        this.beanName = beanName;
    }
    /**
     * Return the description of the resource that the bean
     * definition came from, if any.
     */
    public String getResourceDescription() {
        return this.resourceDescription;
    }

    /**
     * Return the name of the bean requested, if any.
     */
    public String getBeanName() {
        return this.beanName;
    }
}
