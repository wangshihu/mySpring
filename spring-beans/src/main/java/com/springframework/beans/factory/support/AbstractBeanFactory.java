package com.springframework.beans.factory.support;

import com.springframework.beans.BeanWrapper;
import com.springframework.beans.PropertyEditorRegistry;
import com.springframework.beans.PropertyEditorRegistrySupport;
import com.springframework.beans.TypeConverter;
import com.springframework.beans.factory.*;
import com.springframework.beans.factory.config.*;
import com.springframework.core.NamedThreadLocal;
import com.springframework.core.convert.ConversionService;
import com.springframework.util.ClassUtils;
import com.springframework.util.ObjectUtils;
import com.springframework.util.StringUtils;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hadoop on 2015/5/8 0008.
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

    /**
     * Names of beans that have already been created at least once
     */
    private final Set<String> alreadyCreated = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(64));

    /**
     * Map from bean name to merged RootBeanDefinition
     */
    private final Map<String, RootBeanDefinition> mergedBeanDefinitions =
            new ConcurrentHashMap<String, RootBeanDefinition>(64);

    /**
     * ClassLoader to resolve bean class names with, if necessary
     */
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    /**
     * Names of beans that are currently in creation
     */
    private final ThreadLocal<Object> prototypesCurrentlyInCreation =
            new NamedThreadLocal<Object>("Prototype beans currently in creation");

    /**
     * Resolution strategy for expressions in bean definition values
     */
    private BeanExpressionResolver beanExpressionResolver;

    /**
     * Spring ConversionService to use instead of PropertyEditors
     */
    private ConversionService conversionService;

    /**
     * A custom TypeConverter to use, overriding the default PropertyEditor mechanism
     */
    private TypeConverter typeConverter;

    //---------------------------------------------------------------------
    // Implementation of BeanFactory interface
    //---------------------------------------------------------------------

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null, null, false);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType, null, false);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, null, args, false);
    }

    /**
     * Return an instance, which may be shared or independent, of the specified bean.
     *
     * @param name         the name of the bean to retrieve
     * @param requiredType the required type of the bean to retrieve
     * @param args         arguments to use when creating a bean instance using explicit arguments
     *                     (only applied when creating a new instance as opposed to retrieving an existing one)
     * @return an instance of the bean
     * @throws BeansException if the bean could not be created
     */
    public <T> T getBean(String name, Class<T> requiredType, Object... args) throws BeansException {
        return doGetBean(name, requiredType, args, false);
    }

    /**
     * Return an instance, which may be shared or independent, of the specified bean.
     */
    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(
            final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
            throws BeansException {

        final String beanName = transformedBeanName(name);
        Object bean = null;

        // Eagerly check singleton cache for manually registered singletons.
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            //TODO 从缓存中得到singleton
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        } else {
            // Fail if we're already creating this bean instance:
            // We're assumably within a circular reference.如果bean没有初始化，开始组装
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }

            // Check if bean definition exists in this factory.检查bean定义
//            BeanFactory parentBeanFactory = getParentBeanFactory();
//            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
//                // Not found -> check parent.
//                String nameToLookup = originalBeanName(name);
//                if (args != null) {
//                    // Delegation to parent with explicit args.
//                    return (T) parentBeanFactory.getBean(nameToLookup, args);
//                }
//                else {
//                    // No args -> delegate to standard getBean method.
//                    return parentBeanFactory.getBean(nameToLookup, requiredType);
//                }
//            }
//
            if (!typeCheckOnly) {//标记正在组装
                markBeanAsCreated(beanName);
            }

            try {
                final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                checkMergedBeanDefinition(mbd, beanName, args);

                // Guarantee initialization of beans that the current bean depends on.
                String[] dependsOn = mbd.getDependsOn();
                if (dependsOn != null) {
                    for (String dependsOnBean : dependsOn) {
                        if (isDependent(beanName, dependsOnBean)) {
                            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                    "Circular depends-on relationship between '" + beanName + "' and '" + dependsOnBean + "'");
                        }
                        registerDependentBean(dependsOnBean, beanName);
                        getBean(dependsOnBean);
                    }
                }

                // Create bean instance.
                if (mbd.isSingleton()) {
                    sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
                        @Override
                        public Object getObject() throws BeansException {
                            try {
                                return createBean(beanName, mbd, args);
                            } catch (BeansException ex) {
                                // Explicitly remove instance from singleton cache: It might have been put there
                                // eagerly by the creation process, to allow for circular reference resolution.
                                // Also remove any beans that received a temporary reference to the bean.
                                destroySingleton(beanName);
                                throw ex;
                            }
                        }
                    });
                    bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                }
//              else if (mbd.isPrototype()) {
//                    // It's a prototype -> create a new instance.
//                    Object prototypeInstance = null;
//                    try {
//                        beforePrototypeCreation(beanName);
//                        prototypeInstance = createBean(beanName, mbd, args);
//                    } finally {
//                        afterPrototypeCreation(beanName);
//                    }
//                    bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
//                } else {
//                    String scopeName = mbd.getScope();
//                    final Scope scope = this.scopes.get(scopeName);
//                    if (scope == null) {
//                        throw new IllegalStateException("No Scope registered for scope '" + scopeName + "'");
//                    }
//                    try {
//                        Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
//                            @Override
//                            public Object getObject() throws BeansException {
//                                beforePrototypeCreation(beanName);
//                                try {
//                                    return createBean(beanName, mbd, args);
//                                } finally {
//                                    afterPrototypeCreation(beanName);
//                                }
//                            }
//                        });
//                        bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
//                    } catch (IllegalStateException ex) {
//                        throw new BeanCreationException(beanName,
//                                "Scope '" + scopeName + "' is not active for the current thread; " +
//                                        "consider defining a scoped proxy for this bean if you intend to refer to it from a singleton",
//                                ex);
//                    }
//                }
            } catch (BeansException ex) {
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            }

        }

        // Check if required type matches the type of the actual bean instance.
//        if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
//            try {
//                return getTypeConverter().convertIfNecessary(bean, requiredType);
//            } catch (TypeMismatchException ex) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("Failed to convert bean '" + name + "' to required type [" +
//                            ClassUtils.getQualifiedName(requiredType) + "]", ex);
//                }
//                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
//            }
//        }
        return (T) bean;
    }

    /**
     * Add the given bean to the list of disposable beans in this factory,
     * registering its DisposableBean interface and/or the given destroy method
     * to be called on factory shutdown (if applicable). Only applies to singletons.
     *
     * @param beanName the name of the bean
     * @param bean     the bean instance
     * @param mbd      the bean definition for the bean
     * @see RootBeanDefinition#isSingleton
     * @see RootBeanDefinition#getDependsOn
     * @see #registerDependentBean
     */
    protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
        AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
//        if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
//            if (mbd.isSingleton()) {
//                // Register a DisposableBean implementation that performs all destruction
//                // work for the given bean: DestructionAwareBeanPostProcessors,
//                // DisposableBean interface, custom destroy method.
//                registerDisposableBean(beanName,
//                        new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
//            }
//            else {
//                // A bean with a custom scope...
//                Scope scope = this.scopes.get(mbd.getScope());
//                if (scope == null) {
//                    throw new IllegalStateException("No Scope registered for scope '" + mbd.getScope() + "'");
//                }
//                scope.registerDestructionCallback(beanName,
//                        new DisposableBeanAdap
// ter(bean, beanName, mbd, getBeanPostProcessors(), acc));
//            }
//        }
    }

    /**
     * Initialize the given BeanWrapper with the custom editors registered
     * with this factory. To be called for BeanWrappers that will create
     * and populate bean instances.
     * <p>The default implementation delegates to {@link #registerCustomEditors}.
     * Can be overridden in subclasses.
     *
     * @param bw the BeanWrapper to initialize
     */
    protected void initBeanWrapper(BeanWrapper bw) {
        bw.setConversionService(getConversionService());
        registerCustomEditors(bw);
    }

    /**
     * Return the custom TypeConverter to use, if any.
     *
     * @return the custom TypeConverter, or {@code null} if none specified
     */
    protected TypeConverter getCustomTypeConverter() {
        return this.typeConverter;
    }

    /**
     * Initialize the given PropertyEditorRegistry with the custom editors
     * that have been registered with this BeanFactory.
     * <p>To be called for BeanWrappers that will create and populate bean
     * instances, and for SimpleTypeConverter used for constructor argument
     * and factory method type conversion.
     *
     * @param registry the PropertyEditorRegistry to initialize
     */
    protected void registerCustomEditors(PropertyEditorRegistry registry) {
        PropertyEditorRegistrySupport registrySupport =
                (registry instanceof PropertyEditorRegistrySupport ? (PropertyEditorRegistrySupport) registry : null);
        if (registrySupport != null) {
            registrySupport.useConfigValueEditors();
        }
//        if (!this.propertyEditorRegistrars.isEmpty()) {
//            for (PropertyEditorRegistrar registrar : this.propertyEditorRegistrars) {
//                try {
//                    registrar.registerCustomEditors(registry);
//                }
//                catch (BeanCreationException ex) {
//                    Throwable rootCause = ex.getMostSpecificCause();
//                    if (rootCause instanceof BeanCurrentlyInCreationException) {
//                        BeanCreationException bce = (BeanCreationException) rootCause;
//                        if (isCurrentlyInCreation(bce.getBeanName())) {
//                            if (logger.isDebugEnabled()) {
//                                logger.debug("PropertyEditorRegistrar [" + registrar.getClass().getName() +
//                                        "] failed because it tried to obtain currently created bean '" +
//                                        ex.getBeanName() + "': " + ex.getMessage());
//                            }
//                            onSuppressedException(ex);
//                            continue;
//                        }
//                    }
//                    throw ex;
//                }
//            }
//        }
//        if (!this.customEditors.isEmpty()) {
//            for (Map.Entry<Class<?>, Class<? extends PropertyEditor>> entry : this.customEditors.entrySet()) {
//                Class<?> requiredType = entry.getKey();
//                Class<? extends PropertyEditor> editorClass = entry.getValue();
//                registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass));
//            }
//        }
    }

    /**
     * Return a merged RootBeanDefinition, traversing the parent bean definition
     * if the specified bean corresponds to a child bean definition.
     *
     * @param beanName the name of the bean to retrieve the merged definition for
     * @return a (potentially merged) RootBeanDefinition for the given bean
     * @throws NoSuchBeanDefinitionException if there is no bean with the given name
     * @throws BeanDefinitionStoreException  in case of an invalid bean definition
     */
    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        // Quick check on the concurrent map first, with minimal locking.
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null) {
            return mbd;
        }
        return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
    }

    /**
     * Return the bean name, stripping out the factory dereference prefix if necessary,
     * and resolving aliases to canonical names.
     */
    protected String transformedBeanName(String name) {
        return canonicalName(BeanFactoryUtils.transformedBeanName(name));
    }

    /**
     * Get the object for the given bean instance, either the bean
     * instance itself or its created object in case of a FactoryBean.
     *
     * @param beanInstance the shared bean instance
     * @param name         name that may include factory dereference prefix
     * @param beanName     the canonical bean name
     * @param mbd          the merged bean definition
     * @return the object to expose for the bean
     */
    protected Object getObjectForBeanInstance(
            Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

        // Don't let calling code try to dereference the factory if the bean isn't a factory.
        if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
            throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
        }

        // Now we have the bean instance, which may be a normal bean or a FactoryBean.
        // If it's a FactoryBean, we use it to create a bean instance, unless the
        // caller actually wants a reference to the factory.
        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
            return beanInstance;
        }

        Object object = null;
//        if (mbd == null) {
//            object = getCachedObjectForFactoryBean(beanName);
//        }
//        if (object == null) {
//            // Return bean instance from factory.
//            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
//            // Caches object obtained from FactoryBean if it is a singleton.
//            if (mbd == null && containsBeanDefinition(beanName)) {
//                mbd = getMergedLocalBeanDefinition(beanName);
//            }
//            boolean synthetic = (mbd != null && mbd.isSynthetic());
//            object = getObjectFromFactoryBean(factory, beanName, !synthetic);
//        }
        return object;
    }


    /**
     * Return whether the specified prototype bean is currently in creation
     * (within the current thread).
     *
     * @param beanName the name of the bean
     */
    protected boolean isPrototypeCurrentlyInCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        return (curVal != null &&
                (curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName))));
    }

    /**
     * Mark the specified bean as already created (or about to be created).
     * <p>This allows the bean factory to optimize its caching for repeated
     * creation of the specified bean.
     *
     * @param beanName the name of the bean
     */
    protected void markBeanAsCreated(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            this.alreadyCreated.add(beanName);
        }
    }

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    /**
     * Return a RootBeanDefinition for the given top-level bean, by merging with
     * the parent if the given bean's definition is a child bean definition.
     */
    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
            throws BeanDefinitionStoreException {

        return getMergedBeanDefinition(beanName, bd, null);
    }

    /**
     * Return a RootBeanDefinition for the given bean, by merging with the
     * parent if the given bean's definition is a child bean definition.
     *
     * @param beanName     the name of the bean definition
     * @param bd           the original bean definition (Root/ChildBeanDefinition)
     * @param containingBd the containing bean definition in case of inner bean,
     *                     or {@code null} in case of a top-level bean
     * @return a (potentially merged) RootBeanDefinition for the given bean
     * @throws BeanDefinitionStoreException in case of an invalid bean definition
     */
    protected RootBeanDefinition getMergedBeanDefinition(
            String beanName, BeanDefinition bd, BeanDefinition containingBd)
            throws BeanDefinitionStoreException {

        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;

            // Check with full lock now in order to enforce the same merged instance.
            if (containingBd == null) {
                mbd = this.mergedBeanDefinitions.get(beanName);
            }

            if (mbd == null) {
                if (bd.getParentName() == null) {
                    // Use copy of given root bean definition.
                    if (bd instanceof RootBeanDefinition) {
                        mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                    } else {
                        mbd = new RootBeanDefinition(bd);
                    }
                } else {
                    // Child bean definition: needs to be merged with parent.
                    BeanDefinition pbd;
                    try {
                        String parentBeanName = transformedBeanName(bd.getParentName());
                        if (!beanName.equals(parentBeanName)) {
                            pbd = getMergedBeanDefinition(parentBeanName);
                        } else {
                            if (getParentBeanFactory() instanceof ConfigurableBeanFactory) {
                                pbd = ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(parentBeanName);
                            } else {
                                throw new NoSuchBeanDefinitionException(bd.getParentName(),
                                        "Parent name '" + bd.getParentName() + "' is equal to bean name '" + beanName +
                                                "': cannot be resolved without an AbstractBeanFactory parent");
                            }
                        }
                    } catch (NoSuchBeanDefinitionException ex) {
                        throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
                                "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                    }
                    // Deep copy with overridden values.
                    mbd = new RootBeanDefinition(pbd);
                    mbd.overrideFrom(bd);
                }

                // Set default singleton scope, if not configured before.设置作用域为单例,如果没有设置过作用域
                if (!StringUtils.hasLength(mbd.getScope())) {
                    mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
                }

                // A bean contained in a non-singleton bean cannot be a singleton itself.
                // Let's correct this on the fly here, since this might be the result of
                // parent-child merging for the outer bean, in which case the original inner bean
                // definition will not have inherited the merged outer bean's singleton status.
                //如果一个Bean被包含在一个非singleton的Bean中 那么他不能是Singleton,所以在这纠错下。
                if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                    mbd.setScope(containingBd.getScope());
                }

                // Only cache the merged bean definition if we're already about to create an
                // instance of the bean, or at least have already created an instance before.
                if (containingBd == null && isCacheBeanMetadata() && isBeanEligibleForMetadataCaching(beanName)) {
                    this.mergedBeanDefinitions.put(beanName, mbd);
                }
            }

            return mbd;
        }
    }

    /**
     * Resolve the bean class for the specified bean definition,
     * resolving a bean class name into a Class reference (if necessary)
     * and storing the resolved Class in the bean definition for further use.
     */
    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
            throws CannotLoadBeanClassException {
        try {
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                    @Override
                    public Class<?> run() throws Exception {
                        return doResolveBeanClass(mbd, typesToMatch);
                    }
                }, getAccessControlContext());
            } else {
                return doResolveBeanClass(mbd, typesToMatch);
            }
        } catch (PrivilegedActionException pae) {
            ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        }
//        catch (LinkageError err) {
//            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
//        }
    }

    private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch) throws ClassNotFoundException {
        ClassLoader beanClassLoader = getBeanClassLoader();
        ClassLoader classLoaderToUse = beanClassLoader;
        if (!ObjectUtils.isEmpty(typesToMatch)) {
//            // When just doing type checks (i.e. not creating an actual instance yet),
//            // use the specified temporary class loader (e.g. in a weaving scenario).
//            ClassLoader tempClassLoader = getTempClassLoader();
//            if (tempClassLoader != null) {
//                classLoaderToUse = tempClassLoader;
//                if (tempClassLoader instanceof DecoratingClassLoader) {
//                    DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
//                    for (Class<?> typeToMatch : typesToMatch) {
//                        dcl.excludeClass(typeToMatch.getName());
//                    }
//                }
//            }
        }
        String className = mbd.getBeanClassName();
        if (className != null) {
            Object evaluated = evaluateBeanDefinitionString(className, mbd);
            if (!className.equals(evaluated)) {
                // A dynamically resolved expression, supported as of 4.2...
                if (evaluated instanceof Class) {
                    return (Class<?>) evaluated;
                } else if (evaluated instanceof String) {
                    return ClassUtils.forName((String) evaluated, classLoaderToUse);
                } else {
                    throw new IllegalStateException("Invalid class name expression result: " + evaluated);
                }
            }
            // When resolving against a temporary class loader, exit early in order
            // to avoid storing the resolved Class in the bean definition.
            if (classLoaderToUse != beanClassLoader) {
                return ClassUtils.forName(className, classLoaderToUse);
            }
        }
        return mbd.resolveBeanClass(beanClassLoader);
    }

    /**
     * Determine whether the specified bean is eligible for having
     * its bean definition metadata cached.
     */
    protected boolean isBeanEligibleForMetadataCaching(String beanName) {
        return this.alreadyCreated.contains(beanName);
    }

    /**
     * Check the given merged bean definition,
     * potentially throwing validation exceptions.
     */
    protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, Object[] args)
            throws BeanDefinitionStoreException {

        if (mbd.isAbstract()) {
            throw new BeanIsAbstractException(beanName);
        }
    }

    /**
     * Evaluate the given String as contained in a bean definition,
     * potentially resolving it as an expression.
     *
     * @param value          the value to check
     * @param beanDefinition the bean definition that the value comes from
     * @return the resolved value
     */
    protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
        if (this.beanExpressionResolver == null) {
            return value;
        }
        Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
        return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
    }

    /**
     * Create a bean instance for the given merged bean definition (and arguments).
     * The bean definition will already have been merged with the parent definition
     * in case of a child definition.
     * <p>All bean retrieval methods delegate to this method for actual bean creation.
     */
    protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args)
            throws BeanCreationException;

    @Override
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    @Override
    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    /**
     * Perform appropriate cleanup of cached metadata after bean creation failed.
     *
     * @param beanName the name of the bean
     */
    protected void cleanupAfterBeanCreationFailure(String beanName) {
        this.alreadyCreated.remove(beanName);
    }
}
