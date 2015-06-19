package com.springframework.beans.factory.support;

import com.springframework.beans.*;
import com.springframework.beans.factory.*;
import com.springframework.beans.factory.config.*;
import com.springframework.util.ObjectUtils;
import com.springframework.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
        implements AutowireCapableBeanFactory {
    /**
     * Cache of unfinished FactoryBean instances: FactoryBean name --> BeanWrapper
     */
    private final Map<String, BeanWrapper> factoryBeanInstanceCache =
            new ConcurrentHashMap<String, BeanWrapper>(16);

    /** Whether to automatically try to resolve circular references between beans */
    private boolean allowCircularReferences = true;

    /**
     * Strategy for creating bean instances
     */
    private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

    /**
     * Central method of this class: creates a bean instance,
     * populates the bean instance, applies post-processors, etc.
     *
     * @see #doCreateBean
     */
    @Override
    protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) throws BeanCreationException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating instance of bean '" + beanName + "'");
        }
        RootBeanDefinition mbdToUse = mbd;

        // Make sure bean class is actually resolved at this point, and
        // clone the bean definition in case of a dynamically resolved Class
        // which cannot be stored in the shared merged bean definition.
        Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
        }

        // Prepare method overrides.
//        try {
//            mbdToUse.prepareMethodOverrides();
//        }
//        catch (BeanDefinitionValidationException ex) {
//            throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
//                    beanName, "Validation of method overrides failed", ex);
//        }

        try {
            // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            if (bean != null) {
                return bean;
            }
        } catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                    "BeanPostProcessor before instantiation of bean failed", ex);
        }

        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        if (logger.isDebugEnabled()) {
            logger.debug("Finished creating instance of bean '" + beanName + "'");
        }
        return beanInstance;
    }

    /**
     * Actually create the specified bean. Pre-creation processing has already happened
     * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
     * <p>Differentiates between default bean instantiation, use of a
     * factory method, and autowiring a constructor.
     */
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {
        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
        if (mbd.isSingleton()) {
            instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
        Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);

        // Allow post-processors to modify the merged bean definition.
        synchronized (mbd.postProcessingLock) {
            if (!mbd.postProcessed) {
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                mbd.postProcessed = true;
            }
        }

        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                isSingletonCurrentlyInCreation(beanName));
        if (earlySingletonExposure) {
            if (logger.isDebugEnabled()) {
                logger.debug("Eagerly caching bean '" + beanName +
                        "' to allow for resolving potential circular references");
            }
            addSingletonFactory(beanName, new ObjectFactory<Object>() {
                @Override
                public Object getObject() throws BeansException {
                    return getEarlyBeanReference(beanName, mbd, bean);
                }
            });
        }

        // Initialize the bean instance.
        Object exposedObject = bean;
        try {
            populateBean(beanName, mbd, instanceWrapper);
            if (exposedObject != null) {
                exposedObject = initializeBean(beanName, exposedObject, mbd);
            }
        } catch (Throwable ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            } else {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

//        if (earlySingletonExposure) {
//            Object earlySingletonReference = getSingleton(beanName, false);
//            if (earlySingletonReference != null) {
//                if (exposedObject == bean) {
//                    exposedObject = earlySingletonReference;
//                } else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
//                    String[] dependentBeans = getDependentBeans(beanName);
//                    Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
//                    for (String dependentBean : dependentBeans) {
//                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
//                            actualDependentBeans.add(dependentBean);
//                        }
//                    }
//                    if (!actualDependentBeans.isEmpty()) {
//                        throw new BeanCurrentlyInCreationException(beanName,
//                                "Bean with name '" + beanName + "' has been injected into other beans [" +
//                                        StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
//                                        "] in its raw version as part of a circular reference, but has eventually been " +
//                                        "wrapped. This means that said other beans do not use the final version of the " +
//                                        "bean. This is often the result of over-eager type matching - consider using " +
//                                        "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
//                    }
//                }
//            }
//        }

        // Register bean as disposable.
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
    }

    /**
     * Populate the bean instance in the given BeanWrapper with the property values
     * from the bean definition.
     * @param beanName the name of the bean
     * @param mbd the bean definition for the bean
     * @param bw BeanWrapper with bean instance
     */
    protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
        PropertyValues pvs = mbd.getPropertyValues();

        if (bw == null) {
            if (!pvs.isEmpty()) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            }
            else {
                // Skip property population phase for null instance.
                return;
            }
        }

        // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
        // state of the bean before properties are set. This can be used, for example,
        // to support styles of field injection.
        //在注入属性前,前置器修改当前Bean的状态
        boolean continueWithPropertyPopulation = true;

//        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
//            for (BeanPostProcessor bp : getBeanPostProcessors()) {
//                if (bp instanceof InstantiationAwareBeanPostProcessor) {
//                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
//                    if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
//                        continueWithPropertyPopulation = false;
//                        break;
//                    }
//                }
//            }
//        }

        if (!continueWithPropertyPopulation) {
            return;
        }
        //根据Autowire_byName或者ByTpe组装属性.
//        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
//                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
//            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
//
//            // Add property values based on autowire by name if applicable.
//            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
//                autowireByName(beanName, mbd, bw, newPvs);
//            }
//
//            // Add property values based on autowire by type if applicable.
//            if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
//                autowireByType(beanName, mbd, bw, newPvs);
//            }
//
//            pvs = newPvs;
//        }

//        boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
//        boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
//
//        if (hasInstAwareBpps || needsDepCheck) {
//            PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
//            if (hasInstAwareBpps) {
//                for (BeanPostProcessor bp : getBeanPostProcessors()) {
//                    if (bp instanceof InstantiationAwareBeanPostProcessor) {
//                        InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
//                        pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
//                        if (pvs == null) {
//                            return;
//                        }
//                    }
//                }
//            }
//            if (needsDepCheck) {
//                checkDependencies(beanName, mbd, filteredPds, pvs);
//            }
//        }

        applyPropertyValues(beanName, mbd, bw, pvs);
    }

    /**
     * Apply the given property values, resolving any runtime references
     * to other beans in this bean factory. Must use deep copy, so we
     * don't permanently modify this property.
     */
    protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
        if (pvs == null || pvs.isEmpty()) {
            return;
        }

        MutablePropertyValues mpvs = null;
        List<PropertyValue> original;

//        if (System.getSecurityManager() != null) {
//            if (bw instanceof BeanWrapperImpl) {
//                ((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
//            }
//        }

        if (pvs instanceof MutablePropertyValues) {
            mpvs = (MutablePropertyValues) pvs;
            if (mpvs.isConverted()) {
                // Shortcut: use the pre-converted values as-is.
                try {
                    bw.setPropertyValues(mpvs);
                    return;
                }
                catch (BeansException ex) {
                    throw new BeanCreationException(
                            mbd.getResourceDescription(), beanName, "Error setting property values", ex);
                }
            }
            original = mpvs.getPropertyValueList();
        }
        else {
            original = Arrays.asList(pvs.getPropertyValues());
        }

        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

        // Create a deep copy, resolving any references for values.
        List<PropertyValue> deepCopy = new ArrayList<PropertyValue>(original.size());
        boolean resolveNecessary = false;
        for (PropertyValue pv : original) {
            if (pv.isConverted()) {
                deepCopy.add(pv);
            }
            else {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
                Object convertedValue = resolvedValue;
                boolean convertible = bw.isWritableProperty(propertyName) &&
                        !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
                if (convertible) {
                    convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
                }
                // Possibly store converted value in merged bean definition,
                // in order to avoid re-conversion for every created bean instance.
                if (resolvedValue == originalValue) {
                    if (convertible) {
                        pv.setConvertedValue(convertedValue);
                    }
                    deepCopy.add(pv);
                }
                else if (convertible && originalValue instanceof TypedStringValue &&
                        !((TypedStringValue) originalValue).isDynamic() &&
                        !(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
                    pv.setConvertedValue(convertedValue);
                    deepCopy.add(pv);
                }
                else {
                    resolveNecessary = true;
                    deepCopy.add(new PropertyValue(pv, convertedValue));
                }
            }
        }
        if (mpvs != null && !resolveNecessary) {
            mpvs.setConverted();
        }

        // Set our (possibly massaged) deep copy.
        try {
            bw.setPropertyValues(new MutablePropertyValues(deepCopy));
        }
        catch (BeansException ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Error setting property values", ex);
        }
    }

    /**
     * Convert the given value for the specified target property.
     */
    private Object convertForProperty(Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {
//        if (converter instanceof BeanWrapperImpl) {
            return ((BeanWrapperImpl) converter).convertForProperty(value, propertyName);
//        }
//        else {
//            PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
//            MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
//            return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
//        }
    }

    /**
     * Apply MergedBeanDefinitionPostProcessors to the specified bean definition,
     * invoking their {@code postProcessMergedBeanDefinition} methods.
     */
    protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName)
            throws BeansException {

//        try {
//            for (BeanPostProcessor bp : getBeanPostProcessors()) {
//                if (bp instanceof MergedBeanDefinitionPostProcessor) {
//                    MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
//                    bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
//                }
//            }
//        }
//        catch (Exception ex) {
//            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
//                    "Post-processing failed of bean type [" + beanType + "] failed", ex);
//        }
    }

    /**
     * Create a new instance for the specified bean, using an appropriate instantiation strategy:
     * factory method, constructor autowiring, or simple instantiation.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @param args     explicit arguments to use for constructor or factory method invocation
     * @return BeanWrapper for the new instance
     * @see #instantiateBean
     */
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
        // Make sure bean class is actually resolved at this point.
        Class<?> beanClass = resolveBeanClass(mbd, beanName);

        if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
        }

//        if (mbd.getFactoryMethodName() != null)  {
//            return instantiateUsingFactoryMethod(beanName, mbd, args);
//        }

        // Shortcut when re-creating the same bean...
//        boolean resolved = false;
//        boolean autowireNecessary = false;
//        if (args == null) {
//            synchronized (mbd.constructorArgumentLock) {
//                if (mbd.resolvedConstructorOrFactoryMethod != null) {
//                    resolved = true;
//                    autowireNecessary = mbd.constructorArgumentsResolved;
//                }
//            }
//        }
//        if (resolved) {
//            if (autowireNecessary) {
//                return autowireConstructor(beanName, mbd, null, null);
//            }
//            else {
//                return instantiateBean(beanName, mbd);
//            }
//        }

        // Need to determine the constructor...
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
//        if (ctors != null ||
//                mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
//                mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
//            return autowireConstructor(beanName, mbd, ctors, args);
//        }

        // No special handling: simply use no-arg constructor.
        return instantiateBean(beanName, mbd);
    }

    /**
     * Instantiate the given bean using its default constructor.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @return BeanWrapper for the new instance
     */
    protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
        try {
            Object beanInstance;
            final BeanFactory parent = this;
            if (System.getSecurityManager() != null) {
                beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    @Override
                    public Object run() {
                        return getInstantiationStrategy().instantiate(mbd, beanName, parent);
                    }
                }, getAccessControlContext());
            } else {
                beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
            }
            BeanWrapper bw = new BeanWrapperImpl(beanInstance);
            initBeanWrapper(bw);
            return bw;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
        }
    }

    /**
     * Return the instantiation strategy to use for creating bean instances.
     */
    protected InstantiationStrategy getInstantiationStrategy() {
        return this.instantiationStrategy;
    }

    /**
     * Determine candidate constructors to use for the given bean, checking all registered
     *
     * @param beanClass the raw class of the bean
     * @param beanName  the name of the bean
     * @return the candidate constructors, or {@code null} if none specified
     */
    protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass, String beanName)
            throws BeansException {

//        if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
//            for (BeanPostProcessor bp : getBeanPostProcessors()) {
//                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
//                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
//                    Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
//                    if (ctors != null) {
//                        return ctors;
//                    }
//                }
//            }
//        }
        return null;
    }

    /**
     * Apply before-instantiation post-processors, resolving whether there is a
     * before-instantiation shortcut for the specified bean.
     *
     * @param beanName the name of the bean
     * @param mbd      the bean definition for the bean
     * @return the shortcut-determined bean instance, or {@code null} if none
     */
    protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
        Object bean = null;
//        if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
//            // Make sure bean class is actually resolved at this point.
//            if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
//                Class<?> targetType = determineTargetType(beanName, mbd);
//                if (targetType != null) {
//                    bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
//                    if (bean != null) {
//                        bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
//                    }
//                }
//            }
//            mbd.beforeInstantiationResolved = (bean != null);
//        }
        return bean;
    }


    /**
     * Initialize the given bean instance, applying factory callbacks
     * as well as init methods and bean post processors.
     * <p>Called from {@link #createBean} for traditionally defined beans,
     * and from {@link #initializeBean} for existing bean instances.
     * @param beanName the bean name in the factory (for debugging purposes)
     * @param bean the new bean instance we may need to initialize
     * @param mbd the bean definition that the bean was created with
     * (can also be {@code null}, if given an existing bean instance)
     * @return the initialized bean instance (potentially wrapped)
     * @see #applyBeanPostProcessorsBeforeInitialization
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    invokeAwareMethods(beanName, bean);
                    return null;
                }
            }, getAccessControlContext());
        }
        else {
            invokeAwareMethods(beanName, bean);
        }

        Object wrappedBean = bean;
        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
        }

        try {
            invokeInitMethods(beanName, wrappedBean, mbd);
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    (mbd != null ? mbd.getResourceDescription() : null),
                    beanName, "Invocation of init method failed", ex);
        }

        if (mbd == null || !mbd.isSynthetic()) {
            wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
        }
        return wrappedBean;
    }

    private void invokeAwareMethods(final String beanName, final Object bean) {
        if (bean instanceof Aware) {
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }
            if (bean instanceof BeanClassLoaderAware) {
                ((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
            }
            if (bean instanceof BeanFactoryAware) {
                ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
            }
        }
    }


    /**
     * Give a bean a chance to react now all its properties are set,
     * and a chance to know about its owning bean factory (this object).
     * This means checking whether the bean implements InitializingBean or defines
     * a custom init method, and invoking the necessary callback(s) if it does.
     * @param beanName the bean name in the factory (for debugging purposes)
     * @param bean the new bean instance we may need to initialize
     * @param mbd the merged bean definition that the bean was created with
     * (can also be {@code null}, if given an existing bean instance)
     * @throws Throwable if thrown by init methods or by the invocation process
     */
    protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd)
            throws Throwable {

        boolean isInitializingBean = (bean instanceof InitializingBean);
//        if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
//            }
//            if (System.getSecurityManager() != null) {
//                try {
//                    AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
//                        @Override
//                        public Object run() throws Exception {
//                            ((InitializingBean) bean).afterPropertiesSet();
//                            return null;
//                        }
//                    }, getAccessControlContext());
//                }
//                catch (PrivilegedActionException pae) {
//                    throw pae.getException();
//                }
//            }
//            else {
//                ((InitializingBean) bean).afterPropertiesSet();
//            }
//        }

        if (mbd != null) {
            String initMethodName = mbd.getInitMethodName();
//            if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
//                    !mbd.isExternallyManagedInitMethod(initMethodName)) {
//                invokeCustomInitMethod(beanName, bean, mbd);
//            }
        }
    }

    /**
     * Obtain a reference for early access to the specified bean,
     * typically for the purpose of resolving a circular reference.
     * @param beanName the name of the bean (for error handling purposes)
     * @param mbd the merged bean definition for the bean
     * @param bean the raw bean instance
     * @return the object to expose as bean reference
     */
    protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
        Object exposedObject = bean;
//        if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
//            for (BeanPostProcessor bp : getBeanPostProcessors()) {
//                if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
//                    SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
//                    exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
//                    if (exposedObject == null) {
//                        return exposedObject;
//                    }
//                }
//            }
//        }
        return exposedObject;
    }


    @Override
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            {

        Object result = existingBean;
//        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
//            result = beanProcessor.postProcessBeforeInitialization(result, beanName);
//            if (result == null) {
//                return result;
//            }
//        }
        return result;
    }

    @Override
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
             {

        Object result = existingBean;
//        for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
//            result = beanProcessor.postProcessAfterInitialization(result, beanName);
//            if (result == null) {
//                return result;
//            }
//        }
        return result;
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
    public Object resolveDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames, sun.plugin.com.TypeConverter typeConverter) throws BeansException {
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

    @Override
    public BeanFactory getParentBeanFactory() {
        return null;
    }

    @Override
    public boolean containsLocalBean(String name) {
        return false;
    }



    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return null;
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return null;
    }

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return null;
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
}
