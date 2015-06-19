package com.springframework.beans.factory.xml;

import com.springframework.beans.factory.BeanDefinitionStoreException;
import com.springframework.beans.factory.config.BeanDefinitionHolder;
import com.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.Iterator;

/**
 * Created by hadoop on 2015/5/7 0007.
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

    public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;


    XmlReaderContext readerContext;
    BeanDefinitionParserDelegate delegate;

    @Override
    public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) throws BeanDefinitionStoreException {
        this.readerContext = readerContext;
        Element root = doc.getRootElement();
        doRegisterBeanDefinitions(root);
    }

    public void doRegisterBeanDefinitions(Element root) {
        BeanDefinitionParserDelegate parent = this.delegate;
        //创建delegate并且设置一些默认属性，例如Lazy-init
        this.delegate = createDelegate(getReaderContext(), root, parent);

        parseBeanDefinitions(root, this.delegate);

        this.delegate = parent;
    }

    /**
     * 创建Delegate
     */
    private BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {
        BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
        delegate.initDefaults(root, parentDelegate);
        return delegate;
    }

    /**
     * 解析Document,遍历根节点
     */
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
        Iterator<Element> iterator = root.elementIterator();
        while(iterator.hasNext()){
            Element element = iterator.next();
            parseDefaultElement(element, delegate);
        }
    }

    protected void parseDefaultElement(Element element, BeanDefinitionParserDelegate delegate) {
        if(element.getName().equals(BEAN_ELEMENT)){
            processBeanDefinition(element, delegate);
        }
    }
    /**
     * 解析<bean></bean>这个标签
     * Process the given bean element, parsing the bean definition
     * and registering it with the registry.
     */
    private void processBeanDefinition(Element element, BeanDefinitionParserDelegate delegate) {
        BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(element);
        if (bdHolder != null) {
            //bdHolder = delegate.decorateBeanDefinitionIfRequired(element, bdHolder);
            try {
                // Register the final decorated instance.
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
            }
            catch (BeanDefinitionStoreException ex) {
                throw ex;
            }
            // Send registration event.
            //getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
        }
    }




    public XmlReaderContext getReaderContext() {
        return readerContext;
    }

    public void setReaderContext(XmlReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    public BeanDefinitionParserDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(BeanDefinitionParserDelegate delegate) {
        this.delegate = delegate;
    }
}
