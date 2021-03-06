/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springframework.beans;

import com.springframework.core.convert.ConversionFailedException;
import com.springframework.core.convert.ConversionService;
import com.springframework.core.convert.TypeDescriptor;
import com.springframework.util.ClassUtils;
import com.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;

/**
 * Internal helper class for converting property values to target types.
 *
 * <p>Works on a given {@link PropertyEditorRegistrySupport} instance.
 * Used as a delegate by {@link BeanWrapperImpl} and {@link SimpleTypeConverter}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @since 2.0
 * @see BeanWrapperImpl
 */
class TypeConverterDelegate {

	private static final Log logger = LogFactory.getLog(TypeConverterDelegate.class);

	/** Java 8's java.util.Optional.empty() instance */
	private static Object javaUtilOptionalEmpty = null;

	static {
		try {
			Class<?> clazz = ClassUtils.forName("java.util.Optional", TypeConverterDelegate.class.getClassLoader());
			javaUtilOptionalEmpty = ClassUtils.getMethod(clazz, "empty").invoke(null);
		}
		catch (Exception ex) {
			// Java 8 not available - conversion to Optional not supported then.
		}
	}


	private final PropertyEditorRegistrySupport propertyEditorRegistry;

	private final Object targetObject;


	/**
	 * Create a new TypeConverterDelegate for the given editor registry.
	 * @param propertyEditorRegistry the editor registry to use
	 */
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry) {
		this(propertyEditorRegistry, null);
	}

	/**
	 * Create a new TypeConverterDelegate for the given editor registry and bean instance.
	 * @param propertyEditorRegistry the editor registry to use
	 * @param targetObject the target object to work on (as context that can be passed to editors)
	 */
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry, Object targetObject) {
		this.propertyEditorRegistry = propertyEditorRegistry;
		this.targetObject = targetObject;
	}

	/**
	 * Convert the value to the required type (if necessary from a String),
	 * for the specified property.
	 * @param propertyName name of the property
	 * @param oldValue the previous value, if available (may be {@code null})
	 * @param newValue the proposed new value
	 * @param requiredType the type we must convert to
	 * (or {@code null} if not known, for example in case of a collection element)
	 * @param typeDescriptor the descriptor for the target property or field
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	@SuppressWarnings("unchecked")
	public <T> T convertIfNecessary(String propertyName, Object oldValue, Object newValue,
									Class<T> requiredType, TypeDescriptor typeDescriptor) throws IllegalArgumentException {

		Object convertedValue = newValue;

		// Custom editor for this type?
		PropertyEditor editor = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);

		ConversionFailedException firstAttemptEx = null;

		// No custom editor but custom ConversionService specified?
		ConversionService conversionService = this.propertyEditorRegistry.getConversionService();
//		if (editor == null && conversionService != null && convertedValue != null && typeDescriptor != null) {
//			TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(newValue);
//			TypeDescriptor targetTypeDesc = typeDescriptor;
//			if (conversionService.canConvert(sourceTypeDesc, targetTypeDesc)) {
//				try {
//					return (T) conversionService.convert(convertedValue, sourceTypeDesc, targetTypeDesc);
//				}
//				catch (ConversionFailedException ex) {
//					// fallback to default conversion logic below
//					firstAttemptEx = ex;
//				}
//			}
//		}

		// Value not of required type?
		if (editor != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
//			if (requiredType != null && Collection.class.isAssignableFrom(requiredType) && convertedValue instanceof String) {
//				TypeDescriptor elementType = typeDescriptor.getElementTypeDescriptor();
//				if (elementType != null && Enum.class.isAssignableFrom(elementType.getType())) {
//					convertedValue = StringUtils.commaDelimitedListToStringArray((String) convertedValue);
//				}
//			}
			if (editor == null) {
				editor = findDefaultEditor(requiredType);
			}
			convertedValue = doConvertValue(oldValue, convertedValue, requiredType, editor);
		}

		boolean standardConversion = false;

		if (requiredType != null) {
			// Try to apply some standard type conversion rules if appropriate.

			if (convertedValue != null) {
				if (Object.class.equals(requiredType)) {
					return (T) convertedValue;
				}
//				if (requiredType.isArray()) {
//					// Array required -> apply appropriate conversion of elements.
//					if (convertedValue instanceof String && Enum.class.isAssignableFrom(requiredType.getComponentType())) {
//						convertedValue = StringUtils.commaDelimitedListToStringArray((String) convertedValue);
//					}
//					return (T) convertToTypedArray(convertedValue, propertyName, requiredType.getComponentType());
//				}
//				else if (convertedValue instanceof Collection) {
//					// Convert elements to target type, if determined.
//					convertedValue = convertToTypedCollection(
//							(Collection<?>) convertedValue, propertyName, requiredType, typeDescriptor);
//					standardConversion = true;
//				}
//				else if (convertedValue instanceof Map) {
//					// Convert keys and values to respective target type, if determined.
//					convertedValue = convertToTypedMap(
//							(Map<?, ?>) convertedValue, propertyName, requiredType, typeDescriptor);
//					standardConversion = true;
//				}
//				if (convertedValue.getClass().isArray() && Array.getLength(convertedValue) == 1) {
//					convertedValue = Array.get(convertedValue, 0);
//					standardConversion = true;
//				}
//				if (String.class.equals(requiredType) && ClassUtils.isPrimitiveOrWrapper(convertedValue.getClass())) {
//					// We can stringify any primitive value...
//					return (T) convertedValue.toString();
//				}
//				else if (convertedValue instanceof String && !requiredType.isInstance(convertedValue)) {
//					if (firstAttemptEx == null && !requiredType.isInterface() && !requiredType.isEnum()) {
//						try {
//							Constructor<T> strCtor = requiredType.getConstructor(String.class);
//							return BeanUtils.instantiateClass(strCtor, convertedValue);
//						}
//						catch (NoSuchMethodException ex) {
//							// proceed with field lookup
//							if (logger.isTraceEnabled()) {
//								logger.trace("No String constructor found on type [" + requiredType.getName() + "]", ex);
//							}
//						}
//						catch (Exception ex) {
//							if (logger.isDebugEnabled()) {
//								logger.debug("Construction via String failed for type [" + requiredType.getName() + "]", ex);
//							}
//						}
//					}
//					String trimmedValue = ((String) convertedValue).trim();
//					if (requiredType.isEnum() && "".equals(trimmedValue)) {
//						// It's an empty enum identifier: reset the enum value to null.
//						return null;
//					}
//					convertedValue = attemptToConvertStringToEnum(requiredType, trimmedValue, convertedValue);
//					standardConversion = true;
//				}
//			}
//			else {
//				// convertedValue == null
//				if (javaUtilOptionalEmpty != null && requiredType.equals(javaUtilOptionalEmpty.getClass())) {
//					convertedValue = javaUtilOptionalEmpty;
//				}
			}

			if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
				if (firstAttemptEx != null) {
					throw firstAttemptEx;
				}
				// Definitely doesn't match: throw IllegalArgumentException/IllegalStateException
				StringBuilder msg = new StringBuilder();
				msg.append("Cannot convert value of type [").append(ClassUtils.getDescriptiveType(newValue));
				msg.append("] to required type [").append(ClassUtils.getQualifiedName(requiredType)).append("]");
				if (propertyName != null) {
					msg.append(" for property '").append(propertyName).append("'");
				}
				if (editor != null) {
					msg.append(": PropertyEditor [").append(editor.getClass().getName()).append(
							"] returned inappropriate value of type [").append(
							ClassUtils.getDescriptiveType(convertedValue)).append("]");
					throw new IllegalArgumentException(msg.toString());
				}
				else {
					msg.append(": no matching editors or conversion strategy found");
					throw new IllegalStateException(msg.toString());
				}
			}
		}

		if (firstAttemptEx != null) {
			if (editor == null && !standardConversion && requiredType != null && !Object.class.equals(requiredType)) {
				throw firstAttemptEx;
			}
			logger.debug("Original ConversionService attempt failed - ignored since " +
					"PropertyEditor based conversion eventually succeeded", firstAttemptEx);
		}

		return (T) convertedValue;
	}

	/**
	 * Find a default editor for the given type.
	 * @param requiredType the type to find an editor for
	 * @return the corresponding editor, or {@code null} if none
	 */
	private PropertyEditor findDefaultEditor(Class<?> requiredType) {
		PropertyEditor editor = null;
		if (requiredType != null) {
			// No custom editor -> check BeanWrapperImpl's default editors.
			editor = this.propertyEditorRegistry.getDefaultEditor(requiredType);
			if (editor == null && !String.class.equals(requiredType)) {
				// No BeanWrapper default editor -> check standard JavaBean editor.
				editor = BeanUtils.findEditorByConvention(requiredType);
			}
		}
		return editor;
	}

	/**
	 * Convert the value to the required type (if necessary from a String),
	 * using the given property editor.
	 * @param oldValue the previous value, if available (may be {@code null})
	 * @param newValue the proposed new value
	 * @param requiredType the type we must convert to
	 * (or {@code null} if not known, for example in case of a collection element)
	 * @param editor the PropertyEditor to use
	 * @return the new value, possibly the result of type conversion
	 * @throws IllegalArgumentException if type conversion failed
	 */
	private Object doConvertValue(Object oldValue, Object newValue, Class<?> requiredType, PropertyEditor editor) {
		Object convertedValue = newValue;

		if (editor != null && !(convertedValue instanceof String)) {
			// Not a String -> use PropertyEditor's setValue.
			// With standard PropertyEditors, this will return the very same object;
			// we just want to allow special PropertyEditors to override setValue
			// for type conversion from non-String values to the required type.
			try {
				editor.setValue(convertedValue);
				Object newConvertedValue = editor.getValue();
				if (newConvertedValue != convertedValue) {
					convertedValue = newConvertedValue;
					// Reset PropertyEditor: It already did a proper conversion.
					// Don't use it again for a setAsText call.
					editor = null;
				}
			}
			catch (Exception ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("PropertyEditor [" + editor.getClass().getName() + "] does not support setValue call", ex);
				}
				// Swallow and proceed.
			}
		}

		Object returnValue = convertedValue;

		if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[]) {
			// Convert String array to a comma-separated String.
			// Only applies if no PropertyEditor converted the String array before.
			// The CSV String will be passed into a PropertyEditor's setAsText method, if any.
			if (logger.isTraceEnabled()) {
				logger.trace("Converting String array to comma-delimited String [" + convertedValue + "]");
			}
			convertedValue = StringUtils.arrayToCommaDelimitedString((String[]) convertedValue);
		}

		if (convertedValue instanceof String) {
			if (editor != null) {
				// Use PropertyEditor's setAsText in case of a String value.
				if (logger.isTraceEnabled()) {
					logger.trace("Converting String to [" + requiredType + "] using property editor [" + editor + "]");
				}
				String newTextValue = (String) convertedValue;
				return doConvertTextValue(oldValue, newTextValue, editor);
			}
			else if (String.class.equals(requiredType)) {
				returnValue = convertedValue;
			}
		}

		return returnValue;
	}

	/**
	 * Convert the given text value using the given property editor.
	 * @param oldValue the previous value, if available (may be {@code null})
	 * @param newTextValue the proposed text value
	 * @param editor the PropertyEditor to use
	 * @return the converted value
	 */
	private Object doConvertTextValue(Object oldValue, String newTextValue, PropertyEditor editor) {
		try {
			editor.setValue(oldValue);
		}
		catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("PropertyEditor [" + editor.getClass().getName() + "] does not support setValue call", ex);
			}
			// Swallow and proceed.
		}
		editor.setAsText(newTextValue);
		return editor.getValue();
	}
}
