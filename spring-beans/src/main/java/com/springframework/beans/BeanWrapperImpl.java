package com.springframework.beans;

import com.springframework.beans.factory.BeansException;
import com.springframework.core.convert.ConversionException;
import com.springframework.core.convert.ConverterNotFoundException;
import com.springframework.core.convert.Property;
import com.springframework.core.convert.TypeDescriptor;
import com.springframework.util.Assert;
import com.springframework.util.ClassUtils;
import com.springframework.util.ObjectUtils;
import com.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2015/5/9 0009.
 */
public class BeanWrapperImpl extends AbstractPropertyAccessor implements BeanWrapper {

    /**
     * We'll create a lot of these objects, so we don't want a new logger every time.
     */
    private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

    private static Class<?> javaUtilOptionalClass = null;

    static {
        try {
            javaUtilOptionalClass =
                    ClassUtils.forName("java.util.Optional", BeanWrapperImpl.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // Java 8 not available - Optional references simply not supported then.
        }
    }

    /**
     * Cached introspections results for this object, to prevent encountering
     * the cost of JavaBeans introspection every time.
     */
    private CachedIntrospectionResults cachedIntrospectionResults;


    /**
     * The wrapped object
     */
    private Object object;

    private String nestedPath = "";

    private Object rootObject;

    /**
     * The security context used for invoking the property methods
     */
    private AccessControlContext acc;


    /**
     * Map with cached nested BeanWrappers: nested path -> BeanWrapper instance.
     */
    private Map<String, BeanWrapperImpl> nestedBeanWrappers;

    private int autoGrowCollectionLimit = Integer.MAX_VALUE;


    /**
     * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
     * Registers default editors.
     *
     * @see #setWrappedInstance
     */
    public BeanWrapperImpl() {
        this(true);
    }

    /**
     * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
     *
     * @param registerDefaultEditors whether to register default editors
     *                               (can be suppressed if the BeanWrapper won't need any type conversion)
     * @see #setWrappedInstance
     */
    public BeanWrapperImpl(boolean registerDefaultEditors) {
        if (registerDefaultEditors) {
            registerDefaultEditors();
        }
        this.typeConverterDelegate = new TypeConverterDelegate(this);
    }

    /**
     * Create new BeanWrapperImpl for the given object.
     *
     * @param object object wrapped by this BeanWrapper
     */
    public BeanWrapperImpl(Object object) {
        registerDefaultEditors();
        setWrappedInstance(object);
    }

    /**
     * Switch the target object, replacing the cached introspection results only
     * if the class of the new object is different to that of the replaced object.
     *
     * @param object the new target object
     */
    public void setWrappedInstance(Object object) {
        setWrappedInstance(object, "", null);
    }

    /**
     * Switch the target object, replacing the cached introspection results only
     * if the class of the new object is different to that of the replaced object.
     *
     * @param object     the new target object
     * @param nestedPath the nested path of the object
     * @param rootObject the root object at the top of the path
     */
    public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
        Assert.notNull(object, "Bean object must not be null");
//        if (object.getClass().equals(javaUtilOptionalClass)) {
//            this.object = OptionalUnwrapper.unwrap(object);
//        }
//        else {
        this.object = object;
//        }
        this.nestedPath = (nestedPath != null ? nestedPath : "");
        this.rootObject = (!"".equals(this.nestedPath) ? rootObject : this.object);
        this.nestedBeanWrappers = null;
        this.typeConverterDelegate = new TypeConverterDelegate(this, this.object);
        setIntrospectionClass(this.object.getClass());
    }

    /**
     * Set the class to introspect.
     * Needs to be called when the target object changes.
     *
     * @param clazz the class to introspect
     */
    protected void setIntrospectionClass(Class<?> clazz) {
//        if (this.cachedIntrospectionResults != null &&
//                !clazz.equals(this.cachedIntrospectionResults.getBeanClass())) {
//            this.cachedIntrospectionResults = null;
//        }
    }

    @Override
    public boolean isWritableProperty(String propertyName) {
        try {
            PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
            if (pd != null) {
                if (pd.getWriteMethod() != null) {
                    return true;
                }
            } else {
                // Maybe an indexed/mapped property...
                getPropertyValue(propertyName);
                return true;
            }
        } catch (InvalidPropertyException ex) {
            // Cannot be evaluated, so can't be writable.
        }
        return false;
    }

    /**
     * Internal version of {@link #getPropertyDescriptor}:
     * Returns {@code null} if not found rather than throwing an exception.
     *
     * @param propertyName the property to obtain the descriptor for
     * @return the property descriptor for the specified property,
     * or {@code null} if not found
     * @throws BeansException in case of introspection failure
     */
    protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
        Assert.notNull(propertyName, "Property name must not be null");
        BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
        return nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
    }

    /**
     * Recursively navigate to return a BeanWrapper for the nested property path.
     *
     * @param propertyPath property property path, which may be nested
     * @return a BeanWrapper for the target bean
     */
    protected BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) {
        int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
        // Handle nested properties recursively.

//        if (pos > -1) {
//            String nestedProperty = propertyPath.substring(0, pos);
//            String nestedPath = propertyPath.substring(pos + 1);
//            BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
//            return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
//        }
//        else {
        return this;
//        }
    }

    /**
     * Obtain a lazily initializted CachedIntrospectionResults instance
     * for the wrapped object.
     */
    private CachedIntrospectionResults getCachedIntrospectionResults() {
        Assert.state(this.object != null, "BeanWrapper does not hold a bean instance");
        if (this.cachedIntrospectionResults == null) {
            this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
        }
        return this.cachedIntrospectionResults;
    }

    /**
     * Get the last component of the path. Also works if not nested.
     *
     * @param bw         BeanWrapper to work on
     * @param nestedPath property path we know is nested
     * @return last component of the path (the property on the target bean)
     */
    private String getFinalPath(BeanWrapper bw, String nestedPath) {
        if (bw == this) {
            return nestedPath;
        }
        return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
    }

    /**
     * Convert the given value for the specified property to the latter's type.
     * <p>This method is only intended for optimizations in a BeanFactory.
     * Use the {@code convertIfNecessary} methods for programmatic conversion.
     * @param value the value to convert
     * @param propertyName the target property
     * (note that nested or indexed properties are not supported here)
     * @return the new value, possibly the result of type conversion
     * @throws TypeMismatchException if type conversion failed
     */
    public Object convertForProperty(Object value, String propertyName) throws TypeMismatchException {
        CachedIntrospectionResults cachedIntrospectionResults = getCachedIntrospectionResults();
        PropertyDescriptor pd = cachedIntrospectionResults.getPropertyDescriptor(propertyName);
        if (pd == null) {
            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
                    "No property '" + propertyName + "' found");
        }
        TypeDescriptor td = cachedIntrospectionResults.getTypeDescriptor(pd);
        if (td == null) {
            td = cachedIntrospectionResults.addTypeDescriptor(pd, new TypeDescriptor(property(pd)));
        }
        return convertForProperty(propertyName, null, value, td);
    }
    private Object convertForProperty(String propertyName, Object oldValue, Object newValue, TypeDescriptor td)
            throws TypeMismatchException {

        return convertIfNecessary(propertyName, oldValue, newValue, td.getType(), td);
    }

    private Object convertIfNecessary(String propertyName, Object oldValue, Object newValue, Class<?> requiredType,
                                      TypeDescriptor td) throws TypeMismatchException {
        try {
            return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue, requiredType, td);
        }
        catch (ConverterNotFoundException ex) {
            PropertyChangeEvent pce =
                    new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
            throw new ConversionNotSupportedException(pce, td.getType(), ex);
        }
        catch (ConversionException ex) {
            PropertyChangeEvent pce =
                    new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
            throw new TypeMismatchException(pce, requiredType, ex);
        }
        catch (IllegalStateException ex) {
            PropertyChangeEvent pce =
                    new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
            throw new ConversionNotSupportedException(pce, requiredType, ex);
        }
        catch (IllegalArgumentException ex) {
            PropertyChangeEvent pce =
                    new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
            throw new TypeMismatchException(pce, requiredType, ex);
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

    private Property property(PropertyDescriptor pd) {
        GenericTypeAwarePropertyDescriptor typeAware = (GenericTypeAwarePropertyDescriptor) pd;
        return new Property(typeAware.getBeanClass(), typeAware.getReadMethod(), typeAware.getWriteMethod(), typeAware.getName());
    }

    @Override
    public void setPropertyValue(PropertyValue pv) throws BeansException {
        PropertyTokenHolder tokens = (PropertyTokenHolder) pv.resolvedTokens;
        if (tokens == null) {
            String propertyName = pv.getName();
            BeanWrapperImpl nestedBw;
            try {
                nestedBw = getBeanWrapperForPropertyPath(propertyName);
            }
            catch (NotReadablePropertyException ex) {
                throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
                        "Nested property in path '" + propertyName + "' does not exist", ex);
            }
            tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
            if (nestedBw == this) {
                pv.getOriginalPropertyValue().resolvedTokens = tokens;
            }
            nestedBw.setPropertyValue(tokens, pv);
        }
        else {
            setPropertyValue(tokens, pv);
        }
    }


    @SuppressWarnings("unchecked")
    private void setPropertyValue(PropertyTokenHolder tokens, PropertyValue pv) throws BeansException {
        String propertyName = tokens.canonicalName;
        String actualName = tokens.actualName;

        if (tokens.keys != null) {
//            // Apply indexes and map keys: fetch value for all keys but the last one.
//            PropertyTokenHolder getterTokens = new PropertyTokenHolder();
//            getterTokens.canonicalName = tokens.canonicalName;
//            getterTokens.actualName = tokens.actualName;
//            getterTokens.keys = new String[tokens.keys.length - 1];
//            System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);
//            Object propValue;
//            try {
//                propValue = getPropertyValue(getterTokens);
//            }
//            catch (NotReadablePropertyException ex) {
//                throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
//                        "Cannot access indexed value in property referenced " +
//                                "in indexed property path '" + propertyName + "'", ex);
//            }
//            // Set value for last key.
//            String key = tokens.keys[tokens.keys.length - 1];
//            if (propValue == null) {
//                // null map value case
//                if (isAutoGrowNestedPaths()) {
//                    // TODO: cleanup, this is pretty hacky
//                    int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
//                    getterTokens.canonicalName = tokens.canonicalName.substring(0, lastKeyIndex);
//                    propValue = setDefaultValue(getterTokens);
//                }
//                else {
//                    throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
//                            "Cannot access indexed value in property referenced " +
//                                    "in indexed property path '" + propertyName + "': returned null");
//                }
//            }
//            if (propValue.getClass().isArray()) {
//                PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
//                Class<?> requiredType = propValue.getClass().getComponentType();
//                int arrayIndex = Integer.parseInt(key);
//                Object oldValue = null;
//                try {
//                    if (isExtractOldValueForEditor() && arrayIndex < Array.getLength(propValue)) {
//                        oldValue = Array.get(propValue, arrayIndex);
//                    }
//                    Object convertedValue = convertIfNecessary(propertyName, oldValue, pv.getValue(),
//                            requiredType, TypeDescriptor.nested(property(pd), tokens.keys.length));
//                    int length = Array.getLength(propValue);
//                    if (arrayIndex >= length && arrayIndex < this.autoGrowCollectionLimit) {
//                        Class<?> componentType = propValue.getClass().getComponentType();
//                        Object newArray = Array.newInstance(componentType, arrayIndex + 1);
//                        System.arraycopy(propValue, 0, newArray, 0, length);
//                        setPropertyValue(actualName, newArray);
//                        propValue = getPropertyValue(actualName);
//                    }
//                    Array.set(propValue, arrayIndex, convertedValue);
//                }
//                catch (IndexOutOfBoundsException ex) {
//                    throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
//                            "Invalid array index in property path '" + propertyName + "'", ex);
//                }
//            }
//            else if (propValue instanceof List) {
//                PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
//                Class<?> requiredType = GenericCollectionTypeResolver.getCollectionReturnType(
//                        pd.getReadMethod(), tokens.keys.length);
//                List<Object> list = (List<Object>) propValue;
//                int index = Integer.parseInt(key);
//                Object oldValue = null;
//                if (isExtractOldValueForEditor() && index < list.size()) {
//                    oldValue = list.get(index);
//                }
//                Object convertedValue = convertIfNecessary(propertyName, oldValue, pv.getValue(),
//                        requiredType, TypeDescriptor.nested(property(pd), tokens.keys.length));
//                int size = list.size();
//                if (index >= size && index < this.autoGrowCollectionLimit) {
//                    for (int i = size; i < index; i++) {
//                        try {
//                            list.add(null);
//                        }
//                        catch (NullPointerException ex) {
//                            throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
//                                    "Cannot set element with index " + index + " in List of size " +
//                                            size + ", accessed using property path '" + propertyName +
//                                            "': List does not support filling up gaps with null elements");
//                        }
//                    }
//                    list.add(convertedValue);
//                }
//                else {
//                    try {
//                        list.set(index, convertedValue);
//                    }
//                    catch (IndexOutOfBoundsException ex) {
//                        throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
//                                "Invalid list index in property path '" + propertyName + "'", ex);
//                    }
//                }
//            }
//            else if (propValue instanceof Map) {
//                PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
//                Class<?> mapKeyType = GenericCollectionTypeResolver.getMapKeyReturnType(
//                        pd.getReadMethod(), tokens.keys.length);
//                Class<?> mapValueType = GenericCollectionTypeResolver.getMapValueReturnType(
//                        pd.getReadMethod(), tokens.keys.length);
//                Map<Object, Object> map = (Map<Object, Object>) propValue;
//                // IMPORTANT: Do not pass full property name in here - property editors
//                // must not kick in for map keys but rather only for map values.
//                TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(mapKeyType);
//                Object convertedMapKey = convertIfNecessary(null, null, key, mapKeyType, typeDescriptor);
//                Object oldValue = null;
//                if (isExtractOldValueForEditor()) {
//                    oldValue = map.get(convertedMapKey);
//                }
//                // Pass full property name and old value in here, since we want full
//                // conversion ability for map values.
//                Object convertedMapValue = convertIfNecessary(propertyName, oldValue, pv.getValue(),
//                        mapValueType, TypeDescriptor.nested(property(pd), tokens.keys.length));
//                map.put(convertedMapKey, convertedMapValue);
//            }
//            else {
//                throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
//                        "Property referenced in indexed property path '" + propertyName +
//                                "' is neither an array nor a List nor a Map; returned value was [" + propValue + "]");
//            }
        }

        else {
            PropertyDescriptor pd = pv.resolvedDescriptor;
            if (pd == null || !pd.getWriteMethod().getDeclaringClass().isInstance(this.object)) {
                pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
                if (pd == null || pd.getWriteMethod() == null) {
                    if (pv.isOptional()) {
                        logger.debug("Ignoring optional value for property '" + actualName +
                                "' - property not found on bean class [" + getRootClass().getName() + "]");
                        return;
                    }
                    else {
                        PropertyMatches matches = PropertyMatches.forProperty(propertyName, getRootClass());
                        throw new NotWritablePropertyException(
                                getRootClass(), this.nestedPath + propertyName,
                                matches.buildErrorMessage(), matches.getPossibleMatches());
                    }
                }
                pv.getOriginalPropertyValue().resolvedDescriptor = pd;
            }

            Object oldValue = null;
            try {
                Object originalValue = pv.getValue();
                Object valueToApply = originalValue;
                if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
                    if (pv.isConverted()) {
                        valueToApply = pv.getConvertedValue();
                    }
                    else {
                        if (isExtractOldValueForEditor() && pd.getReadMethod() != null) {
                            final Method readMethod = pd.getReadMethod();
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) &&
                                    !readMethod.isAccessible()) {
                                if (System.getSecurityManager()!= null) {
                                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                                        @Override
                                        public Object run() {
                                            readMethod.setAccessible(true);
                                            return null;
                                        }
                                    });
                                }
                                else {
                                    readMethod.setAccessible(true);
                                }
                            }
                            try {
                                if (System.getSecurityManager() != null) {
                                    oldValue = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                                        @Override
                                        public Object run() throws Exception {
                                            return readMethod.invoke(object);
                                        }
                                    }, acc);
                                }
                                else {
                                    oldValue = readMethod.invoke(object);
                                }
                            }
                            catch (Exception ex) {
                                if (ex instanceof PrivilegedActionException) {
                                    ex = ((PrivilegedActionException) ex).getException();
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Could not read previous value of property '" +
                                            this.nestedPath + propertyName + "'", ex);
                                }
                            }
                        }
                        valueToApply = convertForProperty(
                                propertyName, oldValue, originalValue, new TypeDescriptor(property(pd)));
                    }
                    pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
                }
                final Method writeMethod = (pd instanceof GenericTypeAwarePropertyDescriptor ?
                        ((GenericTypeAwarePropertyDescriptor) pd).getWriteMethodForActualAccess() :
                        pd.getWriteMethod());
                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()) && !writeMethod.isAccessible()) {
                    if (System.getSecurityManager()!= null) {
                        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                            @Override
                            public Object run() {
                                writeMethod.setAccessible(true);
                                return null;
                            }
                        });
                    }
                    else {
                        writeMethod.setAccessible(true);
                    }
                }
                final Object value = valueToApply;
                if (System.getSecurityManager() != null) {
                    try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                            @Override
                            public Object run() throws Exception {
                                writeMethod.invoke(object, value);
                                return null;
                            }
                        }, acc);
                    }
                    catch (PrivilegedActionException ex) {
                        throw ex.getException();
                    }
                }
                else {
                    //赋值。。。
                    writeMethod.invoke(this.object, value);
                }
            }
            catch (TypeMismatchException ex) {
                throw ex;
            }
            catch (InvocationTargetException ex) {
                PropertyChangeEvent propertyChangeEvent =
                        new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
                if (ex.getTargetException() instanceof ClassCastException) {
                    throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
                }
                else {
                    Throwable cause = ex.getTargetException();
                    if (cause instanceof UndeclaredThrowableException) {
                        // May happen e.g. with Groovy-generated methods
                        cause = cause.getCause();
                    }
                    throw new MethodInvocationException(propertyChangeEvent, cause);
                }
            }
            catch (Exception ex) {
                PropertyChangeEvent pce =
                        new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
                throw new MethodInvocationException(pce, ex);
            }
        }
    }

    /**
     * Parse the given property name into the corresponding property name tokens.
     * @param propertyName the property name to parse
     * @return representation of the parsed property tokens
     */
    private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
        PropertyTokenHolder tokens = new PropertyTokenHolder();
        String actualName = null;
        List<String> keys = new ArrayList<String>(2);
        int searchIndex = 0;
        while (searchIndex != -1) {
            int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
            searchIndex = -1;
            if (keyStart != -1) {
                int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
                if (keyEnd != -1) {
                    if (actualName == null) {
                        actualName = propertyName.substring(0, keyStart);
                    }
                    String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
                    if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
                        key = key.substring(1, key.length() - 1);
                    }
                    keys.add(key);
                    searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
                }
            }
        }
        tokens.actualName = (actualName != null ? actualName : propertyName);
        tokens.canonicalName = tokens.actualName;
        if (!keys.isEmpty()) {
            tokens.canonicalName +=
                    PROPERTY_KEY_PREFIX +
                            StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX) +
                            PROPERTY_KEY_SUFFIX;
            tokens.keys = StringUtils.toStringArray(keys);
        }
        return tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        if (this.object != null) {
            sb.append(": wrapping object [").append(ObjectUtils.identityToString(this.object)).append("]");
        }
        else {
            sb.append(": no wrapped object set");
        }
        return sb.toString();
    }

    @Override
    public Object getPropertyValue(String propertyName) throws BeansException {
        return null;
    }

    @Override
    public void setPropertyValue(String propertyName, Object value) throws BeansException {

    }

    @Override
    public final Object getWrappedInstance() {
        return this.object;
    }

    @Override
    public final Class<?> getWrappedClass() {
        return (this.object != null ? this.object.getClass() : null);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException {
        return null;
    }

    @Override
    public void setAutoGrowCollectionLimit(int autoGrowCollectionLimit) {

    }

    @Override
    public int getAutoGrowCollectionLimit() {
        return 0;
    }

    /**
     * Return the class of the root object at the top of the path of this BeanWrapper.
     */
    public final Class<?> getRootClass() {
        return (this.rootObject != null ? this.rootObject.getClass() : null);
    }

    private static class PropertyTokenHolder {

        public String canonicalName;

        public String actualName;

        public String[] keys;
    }
}
