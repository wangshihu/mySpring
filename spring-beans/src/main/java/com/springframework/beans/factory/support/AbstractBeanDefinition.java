package com.springframework.beans.factory.support;

import com.springframework.beans.BeanMetadataAttributeAccessor;
import com.springframework.beans.factory.config.AutowireCapableBeanFactory;
import com.springframework.beans.factory.config.BeanDefinition;
import com.springframework.beans.factory.config.ConstructorArgumentValues;
import com.springframework.beans.factory.config.MutablePropertyValues;
import com.springframework.core.io.DescriptiveResource;
import com.springframework.core.io.Resource;
import com.springframework.util.ClassUtils;
import com.springframework.util.ObjectUtils;
import com.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Created by hadoop on 2015/5/7 0007.
 */
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
        implements BeanDefinition, Cloneable {
    /**
     * Constant for the default scope name: "", equivalent to singleton status
     * but to be overridden from a parent bean definition (if applicable).
     */
    public static final String SCOPE_DEFAULT = "";
    //自动装配的模式
    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
    //检查依赖Bean的模式
    public static final int DEPENDENCY_CHECK_NONE = 0;

    public static final int DEPENDENCY_CHECK_OBJECTS = 1;

    public static final int DEPENDENCY_CHECK_SIMPLE = 2;

    public static final int DEPENDENCY_CHECK_ALL = 3;


    //下面是BeanDefinition的一些属性
    private volatile Object beanClass;

    private String scope = SCOPE_DEFAULT;

    private boolean abstractFlag = false;

    private boolean lazyInit = false;

    private int autowireMode = AUTOWIRE_NO;

    private int dependencyCheck = DEPENDENCY_CHECK_NONE;

    private String[] dependsOn;

    private ConstructorArgumentValues constructorArgumentValues;

    private MutablePropertyValues propertyValues;

    private String description;

    private Resource resource;

    private String initMethodName;

    private String destroyMethodName;

    //暂时不懂和没用的属性
    private boolean enforceInitMethod = true;

    private boolean enforceDestroyMethod = true;

    private boolean autowireCandidate = true;

    private boolean primary = false;

    private String factoryBeanName;

    private String factoryMethodName;

    private int role = BeanDefinition.ROLE_APPLICATION;

    private boolean nonPublicAccessAllowed = true;

    private boolean lenientConstructorResolution = true;

    private boolean synthetic = false;

    //private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<String, AutowireCandidateQualifier>(0);
    //private MethodOverrides methodOverrides = new MethodOverrides();

    /**
     * Create a new AbstractBeanDefinition with default settings.
     */
    protected AbstractBeanDefinition() {
        this(null, null);
    }

    /**
     * Create a new AbstractBeanDefinition with the given
     * constructor argument values and property values.
     */
    protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        setConstructorArgumentValues(cargs);
        setPropertyValues(pvs);
    }

    /**
     * Create a new AbstractBeanDefinition as a deep copy of the given
     * bean definition.
     *
     * @param original the original bean definition to copy from
     */
    protected AbstractBeanDefinition(BeanDefinition original) {
        //基础Bedefinition的参数
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        setRole(original.getRole());
        setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
        setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
        setSource(original.getSource());
        copyAttributesFrom(original);

        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            setAutowireMode(originalAbd.getAutowireMode());
            setDependencyCheck(originalAbd.getDependencyCheck());
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());

            setPrimary(originalAbd.isPrimary());
            setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
            setInitMethodName(originalAbd.getInitMethodName());
            setEnforceInitMethod(originalAbd.isEnforceInitMethod());
            setDestroyMethodName(originalAbd.getDestroyMethodName());
            setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
            setSynthetic(originalAbd.isSynthetic());
            setResource(originalAbd.getResource());
        } else {
            setResourceDescription(original.getResourceDescription());
        }
    }

    /**
     * Override settings in this bean definition (presumably a copied parent
     * from a parent-child inheritance relationship) from the given bean
     * definition (presumably the child).
     * <ul>
     * <li>Will override beanClass if specified in the given bean definition.
     * <li>Will always take {@code abstract}, {@code scope},
     * {@code lazyInit}, {@code autowireMode}, {@code dependencyCheck},
     * and {@code dependsOn} from the given bean definition.
     * <li>Will add {@code constructorArgumentValues}, {@code propertyValues},
     * {@code methodOverrides} from the given bean definition to existing ones.
     * <li>Will override {@code factoryBeanName}, {@code factoryMethodName},
     * {@code initMethodName}, and {@code destroyMethodName} if specified
     * in the given bean definition.
     * </ul>
     */
    public void overrideFrom(BeanDefinition other) {
        if (StringUtils.hasLength(other.getBeanClassName())) {
            setBeanClassName(other.getBeanClassName());
        }
        if (StringUtils.hasLength(other.getFactoryBeanName())) {
            setFactoryBeanName(other.getFactoryBeanName());
        }
        if (StringUtils.hasLength(other.getFactoryMethodName())) {
            setFactoryMethodName(other.getFactoryMethodName());
        }
        if (StringUtils.hasLength(other.getScope())) {
            setScope(other.getScope());
        }
        setAbstract(other.isAbstract());
        setLazyInit(other.isLazyInit());
        setRole(other.getRole());
        getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
        getPropertyValues().addPropertyValues(other.getPropertyValues());
        setSource(other.getSource());
        copyAttributesFrom(other);

        if (other instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
            if (otherAbd.hasBeanClass()) {
                setBeanClass(otherAbd.getBeanClass());
            }
            setAutowireCandidate(otherAbd.isAutowireCandidate());
            setAutowireMode(otherAbd.getAutowireMode());
            // copyQualifiersFrom(otherAbd);
            setPrimary(otherAbd.isPrimary());
            setDependencyCheck(otherAbd.getDependencyCheck());
            setDependsOn(otherAbd.getDependsOn());
            setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
            if (StringUtils.hasLength(otherAbd.getInitMethodName())) {
                setInitMethodName(otherAbd.getInitMethodName());
                setEnforceInitMethod(otherAbd.isEnforceInitMethod());
            }
            if (StringUtils.hasLength(otherAbd.getDestroyMethodName())) {
                setDestroyMethodName(otherAbd.getDestroyMethodName());
                setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
            }
            //getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
            setSynthetic(otherAbd.isSynthetic());
            setResource(otherAbd.getResource());
        } else {
            setResourceDescription(other.getResourceDescription());
        }
    }

    /**
     * Return whether this definition specifies a bean class.
     */
    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    /**
     * Specify the class for this bean.
     */
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Return the class of the wrapped bean, if already resolved.
     *
     * @return the bean class, or {@code null} if none defined
     * @throws IllegalStateException if the bean definition does not define a bean class,
     *                               or a specified bean class name has not been resolved into an actual Class
     */
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    /**
     * Return if there are constructor argument values defined for this bean.
     */
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }

    @Override
    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    @Override
    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        } else {
            return (String) beanClassObject;
        }
    }


    @Override
    public String getParentName() {
        return null;
    }

    @Override
    public void setParentName(String parentName) {

    }

    /**
     * Determine the class of the wrapped bean, resolving it from a
     * specified class name if necessary. Will also reload a specified
     * Class from its name when called with the bean class already resolved.
     */
    public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    @Override
    public String getScope() {
        return this.scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;

    }

    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
    }

    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(scope);
    }


    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    @Override
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    @Override
    public boolean isLazyInit() {
        return this.lazyInit;
    }

    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    public int getAutowireMode() {
        return this.autowireMode;
    }


    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    public int getDependencyCheck() {
        return this.dependencyCheck;
    }

    /**
     * Set the names of the beans that this bean depends on being initialized.
     * The bean factory will guarantee that these beans get initialized first.
     * <p>Note that dependencies are normally expressed through bean properties or
     * constructor arguments. This property should just be necessary for other kinds
     * of dependencies like statics (*ugh*) or database preparation on startup.
     */
    @Override
    public void setDependsOn(String... dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * Return the bean names that this bean depends on.
     */
    @Override
    public String[] getDependsOn() {
        return this.dependsOn;
    }

    /**
     * Set whether this bean is a candidate for getting autowired into some other bean.
     */
    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    /**
     * Return whether this bean is a candidate for getting autowired into some other bean.
     */
    @Override
    public boolean isAutowireCandidate() {
        return this.autowireCandidate;
    }

    /**
     * Set whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     */
    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * Return whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     */
    @Override
    public boolean isPrimary() {
        return this.primary;
    }


    public boolean isAbstractFlag() {
        return abstractFlag;
    }

    public void setAbstractFlag(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    @Override
    public ConstructorArgumentValues getConstructorArgumentValues() {
        return constructorArgumentValues;
    }

    public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues = (this.constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
    }

    @Override
    public MutablePropertyValues getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set a description of the resource that this bean definition
     * came from (for the purpose of showing context in case of errors).
     */
    public void setResourceDescription(String resourceDescription) {
        this.resource = new DescriptiveResource(resourceDescription);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getInitMethodName() {
        return initMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    public boolean isEnforceInitMethod() {
        return enforceInitMethod;
    }

    public void setEnforceInitMethod(boolean enforceInitMethod) {
        this.enforceInitMethod = enforceInitMethod;
    }

    public boolean isEnforceDestroyMethod() {
        return enforceDestroyMethod;
    }

    public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
        this.enforceDestroyMethod = enforceDestroyMethod;
    }

    @Override
    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    @Override
    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    @Override
    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    @Override
    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    @Override
    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public boolean isNonPublicAccessAllowed() {
        return nonPublicAccessAllowed;
    }

    public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
    }

    public boolean isLenientConstructorResolution() {
        return lenientConstructorResolution;
    }

    public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
        this.lenientConstructorResolution = lenientConstructorResolution;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }


    /**
     * Return the resolved autowire code,
     * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
     *
     * @see #AUTOWIRE_AUTODETECT
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_BY_TYPE
     */
    public int getResolvedAutowireMode() {
        return this.autowireMode;
    }

    /**
     * Public declaration of Object's {@code clone()} method.
     * Delegates to {@link #cloneBeanDefinition()}.
     */
    @Override
    public Object clone() {
        return cloneBeanDefinition();
    }

    /**
     * Clone this bean definition.
     * To be implemented by concrete subclasses.
     *
     * @return the cloned bean definition object
     */
    public abstract AbstractBeanDefinition cloneBeanDefinition(
    );

    @Override
    public String getResourceDescription() {
        return null;
    }

    @Override
    public BeanDefinition getOriginatingBeanDefinition() {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbstractBeanDefinition)) {
            return false;
        }

        AbstractBeanDefinition that = (AbstractBeanDefinition) other;

        if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
        if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
        if (this.abstractFlag != that.abstractFlag) return false;
        if (this.lazyInit != that.lazyInit) return false;

        if (this.autowireMode != that.autowireMode) return false;
        if (this.dependencyCheck != that.dependencyCheck) return false;
        if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
        if (this.autowireCandidate != that.autowireCandidate) return false;
        if (this.primary != that.primary) return false;

        if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
        if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
        if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
        if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;

        if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
        if (this.enforceInitMethod != that.enforceInitMethod) return false;
        if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
        if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

        if (this.synthetic != that.synthetic) return false;
        if (this.role != that.role) return false;

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
        hashCode = 29 * hashCode + super.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append("]");
        sb.append("; scope=").append(this.scope);
        sb.append("; abstract=").append(this.abstractFlag);
        sb.append("; lazyInit=").append(this.lazyInit);
        sb.append("; autowireMode=").append(this.autowireMode);
        sb.append("; dependencyCheck=").append(this.dependencyCheck);
        sb.append("; autowireCandidate=").append(this.autowireCandidate);
        sb.append("; primary=").append(this.primary);
        sb.append("; factoryBeanName=").append(this.factoryBeanName);
        sb.append("; factoryMethodName=").append(this.factoryMethodName);
        sb.append("; initMethodName=").append(this.initMethodName);
        sb.append("; destroyMethodName=").append(this.destroyMethodName);
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }
}
