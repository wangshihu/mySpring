package com.springframework.beans.factory;

import com.springframework.NestedRuntimeException;
import com.springframework.util.ObjectUtils;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public abstract class BeansException extends NestedRuntimeException {

    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeansException)) {
            return false;
        }
        BeansException otherBe = (BeansException) other;
        return (getMessage().equals(otherBe.getMessage()) &&
                ObjectUtils.nullSafeEquals(getCause(), otherBe.getCause()));
    }

    @Override
    public int hashCode() {
        return getMessage().hashCode();
    }
}
