package com.springframework.beans.factory.config;

import com.springframework.beans.factory.BeanFactory;
import com.springframework.beans.factory.BeansException;
import sun.plugin.com.TypeConverter;

import java.util.Set;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public interface AutowireCapableBeanFactory extends BeanFactory {
    /**
     * Constant that indicates no externally defined autowiring. Note that
     * BeanFactoryAware etc and annotation-driven injection will still be applied.
     */
    int AUTOWIRE_NO = 0;

    /**
     * Constant that indicates autowiring bean properties by name
     */
    int AUTOWIRE_BY_NAME = 1;

    /**
     * Constant that indicates autowiring bean properties by type
     * (applying to all bean property setters).
     */
    int AUTOWIRE_BY_TYPE = 2;

    /**
     * Constant that indicates autowiring the greediest constructor that
     * can be satisfied (involves resolving the appropriate constructor).
     */
    int AUTOWIRE_CONSTRUCTOR = 3;

    /**
     * Fully create a new bean instance of the given class.
     * <p>Performs full initialization of the bean, including all applicable
     * <p>Note: This is intended for creating a fresh instance, populating annotated
     */
    <T> T createBean(Class<T> beanClass) throws BeansException;

    /**
     * Populate the given bean instance through applying after-instantiation callbacks
     * and bean property post-processing (e.g. for annotation-driven injection).
     * <p>Note: This is essentially intended for (re-)populating annotated fields and
     * methods, either for new instances or for deserialized instances. It does
     * <i>not</i> imply traditional by-name or by-type autowiring of properties;
     * 装配给定的instance,包括fields和一些方法
     */
    void autowireBean(Object existingBean) throws BeansException;

    /**
     * Configure the given raw bean: autowiring bean properties, applying
     * bean property values, applying factory callbacks such as {@code setBeanName}
     * and {@code setBeanFactory}, and also applying all bean post processors
     * (including ones which might wrap the given raw bean).
     * <p>This is effectively a superset of what {@link #initializeBean} provides,
     * fully applying the configuration specified by the corresponding bean definition.
     * <b>Note: This method requires a bean definition for the given name!</b>
     * 配置Bean的属性值，以及一些后置处理器
     * 如果没有BeanName会抛出一个NoSuchBeanDefinitionException
     */
    Object configureBean(Object existingBean, String beanName) throws BeansException;

    /**
     * Resolve the specified dependency against the beans defined in this factory.
     * 解析Bean的依赖
     */
    Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException;


    //-------------------------------------------------------------------------
    // Specialized methods for fine-grained control over the bean lifecycle
    //-------------------------------------------------------------------------

    /**
     * Fully create a new bean instance of the given class with the specified
     * autowire strategy. All constants defined in this interface are supported here.
     * <p>Performs full initialization of the bean, including all applicable
     * {@link BeanPostProcessor BeanPostProcessors}. This is effectively a superset
     * of what {@link #autowire} provides, adding {@link #initializeBean} behavior.
     * 完全装载一个Bean的instance
     */
    Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    /**
     * Instantiate a new bean instance of the given class with the specified autowire
     * strategy. All constants defined in this interface are supported here.
     * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
     * before-instantiation callbacks (e.g. for annotation-driven injection).
     * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
     * callbacks or perform any further initialization of the bean. This interface
     * offers distinct, fine-grained operations for those purposes, for example
     * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
     * callbacks are applied, if applicable to the construction of the instance.
     * @param beanClass the class of the bean to instantiate
     * @param autowireMode by name or type, using the constants in this interface
     * @param dependencyCheck whether to perform a dependency check for object
     * references in the bean instance (not applicable to autowiring a constructor,
     * thus ignored there)
     * @return the new bean instance
     * @throws BeansException if instantiation or wiring failed
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_AUTODETECT
     * @see #initializeBean
     * @see #applyBeanPostProcessorsBeforeInitialization
     * @see #applyBeanPostProcessorsAfterInitialization
     */
    Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

    /**
     * Autowire the bean properties of the given bean instance by name or type.
     * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
     * after-instantiation callbacks (e.g. for annotation-driven injection).
     * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
     * callbacks or perform any further initialization of the bean. This interface
     * offers distinct, fine-grained operations for those purposes, for example
     * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
     * callbacks are applied, if applicable to the configuration of the instance.
     * @param existingBean the existing bean instance
     * @param autowireMode by name or type, using the constants in this interface
     * @param dependencyCheck whether to perform a dependency check for object
     * references in the bean instance
     * @throws BeansException if wiring failed
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_NO
     */
    void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
            throws BeansException;

    /**
     * Apply the property values of the bean definition with the given name to
     * the given bean instance. The bean definition can either define a fully
     * self-contained bean, reusing its property values, or just property values
     * meant to be used for existing bean instances.
     * <p>This method does <i>not</i> autowire bean properties; it just applies
     * explicitly defined property values. Use the {@link #autowireBeanProperties}
     * method to autowire an existing bean instance.
     * <b>Note: This method requires a bean definition for the given name!</b>
     * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
     * callbacks or perform any further initialization of the bean. This interface
     * offers distinct, fine-grained operations for those purposes, for example
     * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
     * callbacks are applied, if applicable to the configuration of the instance.
     * @param existingBean the existing bean instance
     * @param beanName the name of the bean definition in the bean factory
     * (a bean definition of that name has to be available)
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
     * if there is no bean definition with the given name
     * @throws BeansException if applying the property values failed
     * @see #autowireBeanProperties
     */
    void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

    /**
     * Initialize the given raw bean, applying factory callbacks
     * such as {@code setBeanName} and {@code setBeanFactory},
     * also applying all bean post processors (including ones which
     * might wrap the given raw bean).
     * <p>Note that no bean definition of the given name has to exist
     * in the bean factory. The passed-in bean name will simply be used
     * for callbacks but not checked against the registered bean definitions.
     * @param existingBean the existing bean instance
     * @param beanName the name of the bean, to be passed to it if necessary
     * (only passed to {@link BeanPostProcessor BeanPostProcessors})
     * @return the bean instance to use, either the original or a wrapped one
     * @throws BeansException if the initialization failed
     */
    Object initializeBean(Object existingBean, String beanName) throws BeansException;

    /**
     * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
     * instance, invoking their {@code postProcessBeforeInitialization} methods.
     * The returned bean instance may be a wrapper around the original.
     * @param existingBean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one
     * @throws BeansException if any post-processing failed
     * @see BeanPostProcessor#postProcessBeforeInitialization
     */
    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException;

    /**
     * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
     * instance, invoking their {@code postProcessAfterInitialization} methods.
     * The returned bean instance may be a wrapper around the original.
     * @param existingBean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one
     * @throws BeansException if any post-processing failed
     * @see BeanPostProcessor#postProcessAfterInitialization
     */
    Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException;

    /**
     * Destroy the given bean instance (typically coming from {@link #createBean}),
     * applying the {@link org.springframework.beans.factory.DisposableBean} contract as well as
     * registered {@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}.
     * <p>Any exception that arises during destruction should be caught
     * and logged instead of propagated to the caller of this method.
     */
    void destroyBean(Object existingBean);

    /**
     * Resolve the specified dependency against the beans defined in this factory.
     */
    Object resolveDependency(DependencyDescriptor descriptor, String beanName,
                             Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException;

}
