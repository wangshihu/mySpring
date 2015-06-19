package com.springframework.beans.factory.xml;

import com.springframework.beans.factory.BeanDefinitionStoreException;
import com.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import com.springframework.beans.factory.support.BeanDefinitionRegistry;
import com.springframework.core.NamedThreadLocal;
import com.springframework.core.io.Resource;
import com.springframework.core.io.support.EncodedResource;
import com.springframework.util.Assert;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

    private ThreadLocal<Set<EncodedResource>> currentLoadingResources = new NamedThreadLocal<Set<EncodedResource>>("XML bean definition resources currently being loaded");

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }



    public void loadBeanDefinitions(EncodedResource encodedResource) {
        Assert.notNull(encodedResource, "EncodedResource must not be null");

        Set<EncodedResource> currentResources = currentLoadingResources.get();
        if(currentResources==null){
            currentResources = new HashSet<EncodedResource>();
            currentLoadingResources.set(currentResources);
        }

        if (!currentResources.add(encodedResource)) {
            throw new BeanDefinitionStoreException(
                    "Detected cyclic loading of " + encodedResource + " - check your import definitions!");
        }

        try {
            InputStream inputStream = encodedResource.getResource().getInputStream();
            try {
                InputSource inputSource = new InputSource(inputStream);
                if (encodedResource.getEncoding() != null) {
                    inputSource.setEncoding(encodedResource.getEncoding());
                }
                doLoadBeanDefinitions(inputSource,encodedResource.getResource());
            }finally {
                inputStream.close();
            }
        }catch (IOException ex){
            throw new BeanDefinitionStoreException(
                    "IOException parsing XML document from " + encodedResource.getResource(), ex);
        }finally {
            currentResources.remove(encodedResource);
            if(currentResources.isEmpty()){
                currentLoadingResources=null;
            }
        }
    }

    public void doLoadBeanDefinitions(InputSource inputsource, Resource resource) {
        SAXReader sax = new SAXReader();
        try {
            Document doc = sax.read(inputsource);
            registerBeanDefinitions(doc, resource);
        } catch (BeanDefinitionStoreException ex){
            throw ex;
        } catch (DocumentException e) {
            throw new BeanDefinitionStoreException("DocumentReadLoad has error"+e.getMessage());
        }
    }

    public void registerBeanDefinitions(Document doc, Resource resource) {
        BeanDefinitionDocumentReader documentReader = new DefaultBeanDefinitionDocumentReader();
        documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
    }

    private XmlReaderContext createReaderContext(Resource resource) {
        return new XmlReaderContext(resource,this);
    }




    @Override
    public ClassLoader getBeanClassLoader() {
        return null;
    }

    @Override
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        return 0;
    }

    @Override
    public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
        return 0;
    }


}
