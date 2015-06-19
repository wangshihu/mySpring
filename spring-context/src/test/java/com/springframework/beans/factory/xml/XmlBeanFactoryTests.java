package com.springframework.beans.factory.xml;

import com.springframework.beans.factory.support.DefaultListableBeanFactory;
import com.springframework.core.io.ClassPathResource;
import com.springframework.core.io.support.EncodedResource;
import com.springframework.tests.sample.beans.TestBean;
import org.junit.Test;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public class XmlBeanFactoryTests {

    private static final Class<?> CLASS = XmlBeanFactoryTests.class;
    private static final String CLASSNAME = CLASS.getSimpleName();

    private static  final ClassPathResource REFTYPES_CONTEXT = classPathResource("-reftypes.xml");

    private static ClassPathResource classPathResource(String suffix) {
        return new ClassPathResource(CLASSNAME + suffix, CLASS);
    }

    @Test
    public void testRefToSingleton() throws Exception {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.loadBeanDefinitions(new EncodedResource(REFTYPES_CONTEXT, "ISO-8859-1"));

        TestBean jen = (TestBean) xbf.getBean("jenny");
        System.out.println(jen.getName());
//        TestBean dave = (TestBean) xbf.getBean("david");
//        TestBean jenks = (TestBean) xbf.getBean("jenks");
//        ITestBean davesJen = dave.getSpouse();
//        ITestBean jenksJen = jenks.getSpouse();
//        assertTrue("1 jen instance", davesJen == jenksJen);
//        assertTrue("1 jen instance", davesJen == jen);
    }
}
