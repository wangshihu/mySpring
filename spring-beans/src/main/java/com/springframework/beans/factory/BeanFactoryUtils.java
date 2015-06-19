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

package com.springframework.beans.factory;


import com.springframework.util.Assert;

/**
 * Convenience methods operating on bean factories, in particular
 * on the {@link ListableBeanFactory} interface.
 *
 * <p>Returns bean counts, bean names or bean instances,
 * taking into account the nesting hierarchy of a bean factory
 * (which the methods defined on the ListableBeanFactory interface don't,
 * in contrast to the methods defined on the BeanFactory interface).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2003
 */
public abstract class BeanFactoryUtils {

	/**
	 * Return the actual bean name, stripping out the factory dereference
	 * prefix (if any, also stripping repeated factory prefixes if found).
	 * 去掉&前缀
	 */
	public static String transformedBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		String beanName = name;
		while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return beanName;
	}

	/**
	 * Return whether the given name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 * @param name the name of the bean
	 * @return whether the given name is a factory dereference
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public static boolean isFactoryDereference(String name) {
		return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
	}


}
