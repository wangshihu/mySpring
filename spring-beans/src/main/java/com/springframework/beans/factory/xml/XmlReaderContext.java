package com.springframework.beans.factory.xml;

import com.springframework.beans.factory.parsing.ReadContext;
import com.springframework.beans.factory.support.BeanDefinitionRegistry;
import com.springframework.core.io.Resource;

/**
 * Created by hadoop on 2015/5/7 0007.
 */
public class XmlReaderContext extends ReadContext {
    private final XmlBeanDefinitionReader reader;

    public XmlReaderContext(Resource resource, XmlBeanDefinitionReader reader) {
        super(resource);
        this.reader = reader;
    }

    public final ClassLoader getBeanClassLoader() {
        return this.reader.getBeanClassLoader();
    }

    public final BeanDefinitionRegistry getRegistry() {
        return this.reader.getRegistry();
    }
}
