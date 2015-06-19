package com.springframework.beans.factory.config;

import com.springframework.beans.factory.ListableBeanFactory;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public interface ConfigurableListableBeanFactory extends
        ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

}
