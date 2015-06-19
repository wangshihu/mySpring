package com.springframework.beans.factory.config;

import com.springframework.beans.factory.BeansException;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public interface BeanExpressionResolver {
    /**
     * Evaluate the given value as an expression, if applicable;
     * return the value as-is otherwise.
     * @param value the value to check
     * @param evalContext the evaluation context
     * @return the resolved value (potentially the given value as-is)
     * @throws BeansException if evaluation failed
     */
    Object evaluate(String value, BeanExpressionContext evalContext) throws BeansException;
}
