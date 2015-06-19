package com.springframework.beans.factory.config;

import com.springframework.beans.BeanMetadataElement;
import com.springframework.util.ObjectUtils;
import com.springframework.util.StringUtils;

/**
 * Created by hadoop on 2015/5/7 0007.
 */
public class BeanDefinitionHolder implements BeanMetadataElement {
    private final BeanDefinition beanDefinition;

    private final String beanName;

    private final String[] aliases;

    public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }

    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }

    public String[] getAliases() {
        return aliases;
    }

    /**
     * Return a friendly, short description for the bean, stating name and aliases.
     */
    public String getShortDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean definition with name '").append(this.beanName).append("'");
        if (this.aliases != null) {
            sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
        }
        return sb.toString();
    }

    /**
     * Return a long description for the bean, including name and aliases
     * as well as a description of the contained {@link BeanDefinition}.
     */
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder(getShortDescription());
        sb.append(": ").append(this.beanDefinition);
        return sb.toString();
    }

    /**
     * This implementation returns the long description. Can be overridden
     * to return the short description or any kind of custom description instead.
     */
    @Override
    public String toString() {
        return getLongDescription();
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeanDefinitionHolder)) {
            return false;
        }
        BeanDefinitionHolder otherHolder = (BeanDefinitionHolder) other;
        return this.beanDefinition.equals(otherHolder.beanDefinition) &&
                this.beanName.equals(otherHolder.beanName) &&
                ObjectUtils.nullSafeEquals(this.aliases, otherHolder.aliases);
    }

    @Override
    public int hashCode() {
        int hashCode = this.beanDefinition.hashCode();
        hashCode = 29 * hashCode + this.beanName.hashCode();
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
        return hashCode;
    }

    @Override
    public Object getSource() {
        return getBeanDefinition().getSource();
    }
}
