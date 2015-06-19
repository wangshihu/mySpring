/*
 * Copyright 2002-2015 the original author or authors.
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

package com.springframework.core.convert;


import com.springframework.core.MethodParameter;
import com.springframework.core.ResolvableType;
import com.springframework.util.Assert;
import com.springframework.util.ClassUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Context about a type to convert from or to.
 *
 * @author Keith Donald
 * @author Andy Clement
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 3.0
 */
@SuppressWarnings("serial")
public class TypeDescriptor implements Serializable {

    static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private static final boolean streamAvailable = ClassUtils.isPresent(
            "java.util.stream.Stream", TypeDescriptor.class.getClassLoader());

    private static final Map<Class<?>, TypeDescriptor> commonTypesCache = new HashMap<Class<?>, TypeDescriptor>(18);

    private static final Class<?>[] CACHED_COMMON_TYPES = {
            boolean.class, Boolean.class, byte.class, Byte.class, char.class, Character.class,
            double.class, Double.class, int.class, Integer.class, long.class, Long.class,
            float.class, Float.class, short.class, Short.class, String.class, Object.class};

    static {
        for (Class<?> preCachedClass : CACHED_COMMON_TYPES) {
            commonTypesCache.put(preCachedClass, valueOf(preCachedClass));
        }
    }


    private final Class<?> type;

    private final ResolvableType resolvableType;

    private final Annotation[] annotations;

    /**
     * Create a new type descriptor from a {@link MethodParameter}.
     * <p>Use this constructor when a source or target conversion point is a
     * constructor parameter, method parameter, or method return value.
     * @param methodParameter the method parameter
     */
    public TypeDescriptor(MethodParameter methodParameter) {
        Assert.notNull(methodParameter, "MethodParameter must not be null");
        this.resolvableType = ResolvableType.forMethodParameter(methodParameter);
        this.type = this.resolvableType.resolve(methodParameter.getParameterType());
        this.annotations = (methodParameter.getParameterIndex() == -1 ?
                nullSafeAnnotations(methodParameter.getMethodAnnotations()) :
                nullSafeAnnotations(methodParameter.getParameterAnnotations()));
    }

    /**
     * Create a new type descriptor from a {@link Field}.
     * <p>Use this constructor when a source or target conversion point is a field.
     * @param field the field
     */
//    public TypeDescriptor(Field field) {
//        Assert.notNull(field, "Field must not be null");
//        this.resolvableType = ResolvableType.forField(field);
//        this.type = this.resolvableType.resolve(field.getType());
//        this.annotations = nullSafeAnnotations(field.getAnnotations());
//    }

    /**
     * Create a new type descriptor from a {@link Property}.
     * <p>Use this constructor when a source or target conversion point is a
     * property on a Java class.
     * @param property the property
     */
    public TypeDescriptor(Property property) {
        Assert.notNull(property, "Property must not be null");
        this.resolvableType = ResolvableType.forMethodParameter(property.getMethodParameter());
        this.type = this.resolvableType.resolve(property.getType());
        this.annotations = nullSafeAnnotations(property.getAnnotations());
    }

    /**
     * Create a new type descriptor from a {@link ResolvableType}. This protected
     * constructor is used internally and may also be used by subclasses that support
     * non-Java languages with extended type systems.
     * @param resolvableType the resolvable type
     * @param type the backing type (or {@code null} if it should get resolved)
     * @param annotations the type annotations
     */
    protected TypeDescriptor(ResolvableType resolvableType, Class<?> type, Annotation[] annotations) {
        this.resolvableType = resolvableType;
        this.type = (type != null ? type : resolvableType.resolve(Object.class));
        this.annotations = nullSafeAnnotations(annotations);
    }

    private Annotation[] nullSafeAnnotations(Annotation[] annotations) {
        return (annotations != null ? annotations : EMPTY_ANNOTATION_ARRAY);
    }

    /**
     * Create a new type descriptor from the given type.
     * <p>Use this to instruct the conversion system to convert an object to a
     * specific target type, when no type location such as a method parameter or
     * field is available to provide additional conversion context.
     * <p>Generally prefer use of {@link #forObject(Object)} for constructing type
     * descriptors from source objects, as it handles the {@code null} object case.
     * @param type the class (may be {@code null} to indicate {@code Object.class})
     * @return the corresponding type descriptor
     */
    public static TypeDescriptor valueOf(Class<?> type) {
        if (type == null) {
            type = Object.class;
        }
        TypeDescriptor desc = commonTypesCache.get(type);
        return (desc != null ? desc : new TypeDescriptor(ResolvableType.forClass(type), null, null));
    }

    /**
     * The type of the backing class, method parameter, field, or property
     * described by this TypeDescriptor.
     * <p>Returns primitive types as-is.
     * <p>See {@link #getObjectType()} for a variation of this operation that
     * resolves primitive types to their corresponding Object types if necessary.
     */
    public Class<?> getType() {
        return this.type;
    }
}
