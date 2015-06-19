package com.springframework.beans.factory.support;

import com.springframework.beans.PropertyEditorRegistrar;
import com.springframework.beans.PropertyEditorRegistry;
import com.springframework.beans.factory.BeanDefinitionStoreException;
import com.springframework.beans.factory.BeanFactory;
import com.springframework.beans.factory.BeansException;
import com.springframework.beans.factory.NoSuchBeanDefinitionException;
import com.springframework.beans.factory.config.*;
import com.springframework.util.Assert;
import com.springframework.util.StringValueResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.plugin.com.TypeConverter;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

    /**
     * Logger available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * List of bean definition names, in registration order
     */
    private final List<String> beanDefinitionNames = new ArrayList<String>(64);

    /**
     * Map of bean definition objects, keyed by bean name
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(64);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
            throws BeanDefinitionStoreException {

        Assert.hasText(beanName, "Bean name must not be empty");
        Assert.notNull(beanDefinition, "BeanDefinition must not be null");

//        if (beanDefinition instanceof AbstractBeanDefinition) {
//            try {
//                ((AbstractBeanDefinition) beanDefinition).validate();
//            }
//            catch (BeanDefinitionValidationException ex) {
//                throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
//                        "Validation of bean definition failed", ex);
//            }
//        }

        BeanDefinition oldBeanDefinition;

        oldBeanDefinition = this.beanDefinitionMap.get(beanName);
        if (oldBeanDefinition != null) {
            //TODO 当BeanDefinition存在的时候,spring会根据配置是否允许覆盖BeanDefinition
        } else {
            this.beanDefinitionNames.add(beanName);
//            this.manualSingletonNames.remove(beanName);
//            this.frozenBeanDefinitionNames = null;
        }
        this.beanDefinitionMap.put(beanName, beanDefinition);
        //TODO spring在覆盖BeanDefinition的时候，需要充值所有Singleton的Bean实例
//        if (oldBeanDefinition != null || containsSingleton(beanName)) {
//            resetBeanDefinition(beanName);
//        }
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("No bean named '" + beanName + "' found in " + this);
            }
            throw new NoSuchBeanDefinitionException(beanName);
        }
        return bd;
    }

    @Override
    public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {

    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return false;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return new String[0];
    }

    @Override
    public int getBeanDefinitionCount() {
        return 0;
    }

    @Override
    public boolean isBeanNameInUse(String beanName) {
        return false;
    }


    @Override
    public BeanFactory getParentBeanFactory() {
        return null;
    }

    @Override
    public boolean containsLocalBean(String name) {
        return false;
    }


    @Override
    public boolean containsBean(String name) {
        return false;
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public String[] getAliases(String name) {
        return new String[0];
    }

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {

    }

    @Override
    public Object getSingleton(String beanName) {
        return null;
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return false;
    }

    @Override
    public String[] getSingletonNames() {
        return new String[0];
    }

    @Override
    public int getSingletonCount() {
        return 0;
    }

    @Override
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        return null;
    }

    @Override
    public void autowireBean(Object existingBean) throws BeansException {

    }

    @Override
    public Object configureBean(Object existingBean, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        return null;
    }

    @Override
    public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
        return null;
    }

    @Override
    public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException {

    }

    @Override
    public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {

    }

    @Override
    public Object initializeBean(Object existingBean, String beanName) throws BeansException {
        return null;
    }


    @Override
    public void destroyBean(Object existingBean) {

    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {
        return null;
    }

    @Override
    public void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException {

    }

    @Override
    public void setBeanClassLoader(ClassLoader beanClassLoader) {

    }


    @Override
    public void setTempClassLoader(ClassLoader tempClassLoader) {

    }

    @Override
    public ClassLoader getTempClassLoader() {
        return null;
    }

    @Override
    public void setCacheBeanMetadata(boolean cacheBeanMetadata) {

    }

    @Override
    public boolean isCacheBeanMetadata() {
        return false;
    }

    @Override
    public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {

    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {

    }

    @Override
    public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {

    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {

    }

    @Override
    public int getBeanPostProcessorCount() {
        return 0;
    }

    @Override
    public void registerScope(String scopeName, Scope scope) {

    }

    @Override
    public String[] getRegisteredScopeNames() {
        return new String[0];
    }

    @Override
    public Scope getRegisteredScope(String scopeName) {
        return null;
    }

    @Override
    public AccessControlContext getAccessControlContext() {
        return null;
    }

    @Override
    public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {

    }

    @Override
    public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {

    }

    @Override
    public void removeAlias(String alias) {

    }

    @Override
    public boolean isAlias(String beanName) {
        return false;
    }

    @Override
    public void resolveAliases(StringValueResolver valueResolver) {

    }

    @Override
    public BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return null;
    }

    @Override
    public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public void setCurrentlyInCreation(String beanName, boolean inCreation) {

    }

    @Override
    public boolean isCurrentlyInCreation(String beanName) {
        return false;
    }

    @Override
    public void registerDependentBean(String beanName, String dependentBeanName) {

    }

    @Override
    public String[] getDependentBeans(String beanName) {
        return new String[0];
    }

    @Override
    public String[] getDependenciesForBean(String beanName) {
        return new String[0];
    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {

    }

    @Override
    public void destroyScopedBean(String beanName) {

    }

    @Override
    public void destroySingletons() {

    }
}
