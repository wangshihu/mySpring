package com.springframework.beans.factory.xml;

import com.springframework.beans.factory.BeanDefinitionStoreException;
import org.dom4j.Document;


/**
 * Created by hadoop on 2015/5/7 0007.
 */
public interface BeanDefinitionDocumentReader {
    void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
            throws BeanDefinitionStoreException;

}
