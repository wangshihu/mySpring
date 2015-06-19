package com.springframework.core.io;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * Created by hadoop on 2015/5/5 0005.
 */
public class Dom4jTest {
    @Test
    public void test() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(this.getClass().getClassLoader().getResourceAsStream("com/springframework/core/io/Dom4jTest.xml"));
        //获取根节点元素对象
        Element node = document.getRootElement();
        //listNodes(node);
        print(node);
    }


    public void print(Element root){

        Iterator<Element> it = root.elementIterator();
        while(it.hasNext()){
            Element node = it.next();
            dealBean(node);
        }
    }

    public void dealBean(Element node){
        String id = node.attributeValue("id");
        String name = node.attributeValue("name");
        System.out.println("id="+id+" name="+name);

        Iterator<Element> it = node.elementIterator();
        while(it.hasNext()){
            Element child = it.next();
            dealProperty(child);
        }
    }

    public void dealProperty(Element child) {
        String name = child.attributeValue("name");

        System.out.println("property name="+name+" "+child.elementText("value")+" "+child.elementText("ref"));
    }

    /**
     * 遍历当前节点元素下面的所有(元素的)子节点
     *
     * @param node
     */
    public void listNodes(Element node) {
        System.out.println("当前节点的名称：：" + node.getName()+",");
        // 获取当前节点的所有属性节点
        List<Attribute> list = node.attributes();
        // 遍历属性节点
        for (Attribute attr : list) {
            System.out.println(attr.getText() + "-----" + attr.getName()
                    + "---" + attr.getValue());
        }

        if (!(node.getTextTrim().equals(""))) {
            System.out.println("文本内容：：：：" + node.getText());
        }

        // 当前节点下面子节点迭代器
        Iterator<Element> it = node.elementIterator();
        // 遍历
        while (it.hasNext()) {
            // 获取某个子节点对象
            Element e = it.next();
            // 对子节点进行遍历
            listNodes(e);
        }
    }
}
