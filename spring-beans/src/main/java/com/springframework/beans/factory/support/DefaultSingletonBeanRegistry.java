package com.springframework.beans.factory.support;

import com.springframework.beans.factory.BeanCreationException;
import com.springframework.beans.factory.BeanCreationNotAllowedException;
import com.springframework.beans.factory.BeanCurrentlyInCreationException;
import com.springframework.beans.factory.ObjectFactory;
import com.springframework.beans.factory.config.SingletonBeanRegistry;
import com.springframework.core.SimpleAliasRegistry;
import com.springframework.util.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hadoop on 2015/5/8 0008.
 */
public abstract class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
    /**
     * Logger available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Internal marker for a null singleton object:
     * used as marker value for concurrent Maps (which don't support null values).
     */
    protected static final Object NULL_OBJECT = new Object();

    /**
     * Map between dependent bean names: bean name --> Set of dependent bean names
     */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<String, Set<String>>(64);

    /**
     * Cache of singleton objects: bean name --> bean instance
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(64);

    /** Cache of singleton factories: bean name --> ObjectFactory */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>(16);

    /** Cache of early singleton objects: bean name --> bean instance */
    private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);

    /** Set of registered singletons, containing the bean names in registration order */
    private final Set<String> registeredSingletons = new LinkedHashSet<String>(64);

    /**
     * Flag that indicates whether we're currently within destroySingletons
     */
    private boolean singletonsCurrentlyInDestruction = false;

    /**
     * List of suppressed Exceptions, available for associating related causes
     */
    private Set<Exception> suppressedExceptions;

    /**
     * Names of beans currently excluded from in creation checks
     */
    private final Set<String> inCreationCheckExclusions =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    /**
     * Names of beans that are currently in creation
     */
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));

    /**
     * Return the (raw) singleton object registered under the given name,
     * creating and registering a new one if none registered yet.
     *
     * @param beanName         the name of the bean
     * @param singletonFactory the ObjectFactory to lazily create the singleton
     *                         with, if necessary
     * @return the registered singleton object
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName,
                            "Singleton bean creation not allowed while the singletons of this factory are in destruction " +
                                    "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<Exception>();
                }
                try {
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                } catch (IllegalStateException ex) {
                    // Has the singleton object implicitly appeared in the meantime ->
                    // if yes, proceed with it since the exception indicates that state.
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                } catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                } finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }
            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }
    }
    /**
     * Return whether the specified singleton bean is currently in creation
     * (within the entire factory).
     * @param beanName the name of the bean
     */
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    /**
     * Add the given singleton factory for building the specified singleton
     * if necessary.
     * <p>To be called for eager registration of singletons, e.g. to be able to
     * resolve circular references.
     * @param beanName the name of the bean
     * @param singletonFactory the factory for the singleton object
     */
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
            }
        }
    }


    /**
     * Determine whether the specified dependent bean has been registered as
     * dependent on the given bean or on any of its transitive dependencies.
     *
     * @param beanName          the name of the bean to check
     * @param dependentBeanName the name of the dependent bean
     * @since 4.0
     */
    protected boolean isDependent(String beanName, String dependentBeanName) {
        return isDependent(beanName, dependentBeanName, null);
    }

    private boolean isDependent(String beanName, String dependentBeanName, Set<String> alreadySeen) {
        String canonicalName = canonicalName(beanName);
        if (alreadySeen != null && alreadySeen.contains(beanName)) {
            return false;
        }
        Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
        if (dependentBeans == null) {
            return false;
        }
        if (dependentBeans.contains(dependentBeanName)) {
            return true;
        }
        for (String transitiveDependency : dependentBeans) {
            if (alreadySeen == null) {
                alreadySeen = new HashSet<String>();
            }
            alreadySeen.add(beanName);
            if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Callback before singleton creation.
     * <p>The default implementation register the singleton as currently in creation.
     */
    protected void beforeSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

    /**
     * Destroy the given bean. Delegates to {@code destroyBean}
     * if a corresponding disposable bean instance is found.
     */
    public void destroySingleton(String beanName) {
//        // Remove a registered singleton of the given name, if any.
//        removeSingleton(beanName);
//
//        // Destroy the corresponding DisposableBean instance.
//        DisposableBean disposableBean;
//        synchronized (this.disposableBeans) {
//            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
//        }
//        destroyBean(beanName, disposableBean);
    }

    /**
     * Callback after singleton creation.
     * <p>The default implementation marks the singleton as not in creation anymore.
     * @param beanName the name of the singleton that has been created
     * @see #isSingletonCurrentlyInCreation
     */
    protected void afterSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
            throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
        }
    }

    /**
     * Add the given singleton object to the singleton cache of this factory.
     * <p>To be called for eager registration of singletons.
     * @param beanName the name of the bean
     * @param singletonObject the singleton object
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

}
