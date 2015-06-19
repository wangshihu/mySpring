package com.springframework.beans;

import com.springframework.beans.factory.BeansException;
import com.springframework.beans.factory.config.MutablePropertyValues;
import com.springframework.core.convert.TypeDescriptor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2015/5/9 0009.
 */
public abstract class AbstractPropertyAccessor extends TypeConverterSupport implements ConfigurablePropertyAccessor {
    private boolean extractOldValueForEditor = false;

    private boolean autoGrowNestedPaths = false;

    @Override
    public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
        this.extractOldValueForEditor = extractOldValueForEditor;
    }

    @Override
    public boolean isExtractOldValueForEditor() {
        return this.extractOldValueForEditor;
    }

    @Override
    public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
        this.autoGrowNestedPaths = autoGrowNestedPaths;
    }

    @Override
    public boolean isAutoGrowNestedPaths() {
        return this.autoGrowNestedPaths;
    }


    @Override
    public boolean isReadableProperty(String propertyName) {
        return false;
    }

    @Override
    public boolean isWritableProperty(String propertyName) {
        return false;
    }

    @Override
    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
        return null;
    }



    @Override
    public void setPropertyValues(Map<?, ?> map) throws BeansException {

    }

    @Override
    public void setPropertyValues(PropertyValues pvs) throws BeansException {
        setPropertyValues(pvs, false, false);
    }

    @Override
    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException {
        setPropertyValues(pvs, ignoreUnknown, false);
    }

    @Override
    public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
            throws BeansException {

        List<PropertyAccessException> propertyAccessExceptions = null;
        List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues ?
                ((MutablePropertyValues) pvs).getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));
        for (PropertyValue pv : propertyValues) {
            try {
                // This method may throw any BeansException, which won't be caught
                // here, if there is a critical failure such as no matching field.
                // We can attempt to deal only with less serious exceptions.
                setPropertyValue(pv);
            }
            catch (NotWritablePropertyException ex) {
                if (!ignoreUnknown) {
                    throw ex;
                }
                // Otherwise, just ignore it and continue...
            }
            catch (NullValueInNestedPathException ex) {
                if (!ignoreInvalid) {
                    throw ex;
                }
                // Otherwise, just ignore it and continue...
            }
            catch (PropertyAccessException ex) {
                if (propertyAccessExceptions == null) {
                    propertyAccessExceptions = new LinkedList<PropertyAccessException>();
                }
                propertyAccessExceptions.add(ex);
            }
        }

        // If we encountered individual exceptions, throw the composite exception.
        if (propertyAccessExceptions != null) {
            PropertyAccessException[] paeArray =
                    propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
            throw new PropertyBatchUpdateException(paeArray);
        }
    }

    // Redefined with public visibility.
    @Override
    public Class<?> getPropertyType(String propertyPath) {
        return null;
    }

    /**
     * Actually get the value of a property.
     * @param propertyName name of the property to get the value of
     * @return the value of the property
     * @throws InvalidPropertyException if there is no such property or
     * if the property isn't readable
     * @throws PropertyAccessException if the property was valid but the
     * accessor method failed
     */
    @Override
    public abstract Object getPropertyValue(String propertyName) throws BeansException;

    /**
     * Actually set a property value.
     * @param propertyName name of the property to set value of
     * @param value the new value
     * @throws InvalidPropertyException if there is no such property or
     * if the property isn't writable
     * @throws PropertyAccessException if the property was valid but the
     * accessor method failed or a type mismatch occured
     */
    @Override
    public abstract void setPropertyValue(String propertyName, Object value) throws BeansException;
}
