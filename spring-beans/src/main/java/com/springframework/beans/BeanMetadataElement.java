package com.springframework.beans;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public interface BeanMetadataElement {
    /**
     * Return the configuration source {@code Object} for this metadata element
     * (may be {@code null}).
     */
    Object getSource();
}
