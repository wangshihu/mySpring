package com.springframework.beans.factory.xml;

import com.springframework.beans.PropertyValue;
import com.springframework.beans.factory.BeanDefinitionStoreException;
import com.springframework.beans.factory.config.BeanDefinition;
import com.springframework.beans.factory.config.BeanDefinitionHolder;
import com.springframework.beans.factory.config.RuntimeBeanReference;
import com.springframework.beans.factory.config.TypedStringValue;
import com.springframework.beans.factory.support.AbstractBeanDefinition;
import com.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import com.springframework.util.ClassUtils;
import com.springframework.util.CollectionUtils;
import com.springframework.util.StringUtils;
import org.dom4j.Element;

import java.util.*;

/**
 * Created by hadoop on 2015/5/7 0007.
 */
public class BeanDefinitionParserDelegate {

    public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";

    public static final String BEAN_ELEMENT = "bean";


    public static final String ID_ATTRIBUTE="id";

    public static final String NAME_ATTRIBUTE="name";

    public static final String AUTOWIRE_ATTRIBUTE="autowire";

    public static final String AUTOWIRE_NO_VALUE = "no";

    public static final String AUTOWIRE_BY_NAME_VALUE = "byName";

    public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";

    public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";

    public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

    public static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";

    public static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";

    public static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

    public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";

    public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";

    public static final String INIT_METHOD_ATTRIBUTE = "init-method";

    public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

    public static final String TRUE_VALUE = "true";

    public static final String FALSE_VALUE = "false";

    public static final String DEFAULT_VALUE = "default";

    public static final String DESCRIPTION_ELEMENT = "description";

    public static final String SCOPE_ATTRIBUTE = "scope";

    public static final String PARENT_ATTRIBUTE = "parent";

    public static final String CLASS_ATTRIBUTE = "class";

    public static final String ABSTRACT_ATTRIBUTE = "abstract";

    public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";

    public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

    public static final String PROPERTY_ELEMENT = "property";

    public static final String REF_ATTRIBUTE = "ref";

    public static final String VALUE_ATTRIBUTE = "value";

    public static final String VALUE_ELEMENT = "value";

    public static final String NULL_ELEMENT = "null";

    public static final String ARRAY_ELEMENT = "array";

    public static final String LIST_ELEMENT = "list";

    public static final String SET_ELEMENT = "set";

    public static final String MAP_ELEMENT = "map";

    public static final String REF_ELEMENT ="ref";

    public static final String ENTRY_ELEMENT = "entry";

    public static final String KEY_ELEMENT = "key";

    public static final String KEY_ATTRIBUTE = "key";

    public static final String KEY_REF_ATTRIBUTE = "key-ref";

    public static final String VALUE_REF_ATTRIBUTE = "value-ref";

    /**
     * Stores all used bean names so we can enforce uniqueness on a per
     * beans-element basis. Duplicate bean ids/names may not exist within the
     * same level of beans element nesting, but may be duplicated across levels.
     */
    private final Set<String> usedNames = new HashSet<String>();

    private final XmlReaderContext readerContext;
    private final DocumentDefaultsDefinition defaults = new DocumentDefaultsDefinition();

    public BeanDefinitionParserDelegate(XmlReaderContext readerContext){
        this.readerContext = readerContext;
    }

    /**
     * 初始化默认属性 lazy-init, autowire, dependency check settings,
     * init-method, destroy-method and merge settings.
     */
    public void initDefaults(Element root, BeanDefinitionParserDelegate parentDelegate) {
        populateDefaults(defaults,(parentDelegate==null?null:parentDelegate.defaults),root);
    }
    /**
     * 装配默认属性 lazy-init, autowire, dependency check settings,
     * init-method, destroy-method and merge settings.
     */
    protected void populateDefaults(DocumentDefaultsDefinition defaults, DocumentDefaultsDefinition parentDefaults, Element root) {

    }

    /**
     * Parses the supplied {@code &lt;bean&gt;} element. May return {@code null}
     * if there were errors during parse. Errors are reported to the
     */
    public BeanDefinitionHolder parseBeanDefinitionElement(Element element) {
        return parseBeanDefinitionElement(element, null);
    }

    private BeanDefinitionHolder parseBeanDefinitionElement(Element element, BeanDefinition containingBean) {

        String id = element.attributeValue(ID_ATTRIBUTE);
        String nameAttr = element.attributeValue(NAME_ATTRIBUTE);

        List<String> aliases = new ArrayList<String>();
        if(StringUtils.hasText(nameAttr)){
            aliases.add(nameAttr);//TODO name转换alias数组
        }
        String beanName = id;
        if(!StringUtils.hasText(beanName)&& !aliases.isEmpty()){//如果beanName为空并且alias集合不为空
            beanName = aliases.remove(0);
        }
        if(containingBean==null){//检查beanName和alias是否被使用
            checkNameUniqueness(beanName,aliases,element);
        }
        AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(element, beanName, containingBean);
        return new BeanDefinitionHolder(beanDefinition,beanName,StringUtils.toStringArray(aliases));

    }

    protected AbstractBeanDefinition parseBeanDefinitionElement(Element element, String beanName, BeanDefinition containingBean) {
        //TODO spring 在parseBeanDeginition的时候加入一个自定义的stack,目的是错误信息输出时,方便调用toString()
        String className = null;
        if (element.attribute(CLASS_ATTRIBUTE)!=null) {
            className = element.attributeValue(CLASS_ATTRIBUTE).trim();
        }
        try {
            String parent = null;
            if (element.attribute(PARENT_ATTRIBUTE)!=null) {
                parent = element.attribute(PARENT_ATTRIBUTE).getName().trim();
            }
            AbstractBeanDefinition bd = createBeanDefinition(className, parent);

            parseBeanDefinitionAttributes(element, beanName, containingBean, bd);
           // bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

//            parseMetaElements(ele, bd);
//            parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
//            parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

            parseConstructorArgElements(element, bd);
            parsePropertyElements(element, bd);
            //parseQualifierElements(ele, bd);

            bd.setResource(this.readerContext.getResource());
            //bd.setSource(extractSource(element));

            return bd;
        }
        catch (ClassNotFoundException ex) {
            throw new BeanDefinitionStoreException("Bean class [" + className + "] not found",ex);
        }
        catch (NoClassDefFoundError err) {
            throw new BeanDefinitionStoreException("Class that bean class [" + className + "] depends on not found",  err);
        }
        catch (Throwable ex) {
            throw new BeanDefinitionStoreException("Unexpected failure during bean definition parsing",ex);
        }
    }

    /**
     * Parse constructor-arg sub-elements of the given bean element.
     */
    public void parseConstructorArgElements(Element beanEle, BeanDefinition bd) {
        Iterator<Element> iterator = beanEle.elementIterator();
        while(iterator.hasNext()){
            Element node = iterator.next();
            if(node.getName().equals(CONSTRUCTOR_ARG_ELEMENT)){
                parseConstructorArgElement((Element) node, bd);
            }
        }
    }

    /**
     * Parse a constructor-arg element.
     */
    public void parseConstructorArgElement(Element ele, BeanDefinition bd) {
//        String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
//        String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
//        String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
//        if (StringUtils.hasLength(indexAttr)) {
//            try {
//                int index = Integer.parseInt(indexAttr);
//                if (index < 0) {
//                    error("'index' cannot be lower than 0", ele);
//                }
//                else {
//                    try {
//                        this.parseState.push(new ConstructorArgumentEntry(index));
//                        Object value = parsePropertyValue(ele, bd, null);
//                        ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
//                        if (StringUtils.hasLength(typeAttr)) {
//                            valueHolder.setType(typeAttr);
//                        }
//                        if (StringUtils.hasLength(nameAttr)) {
//                            valueHolder.setName(nameAttr);
//                        }
//                        valueHolder.setSource(extractSource(ele));
//                        if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(index)) {
//                            error("Ambiguous constructor-arg entries for index " + index, ele);
//                        }
//                        else {
//                            bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
//                        }
//                    }
//                    finally {
//                        this.parseState.pop();
//                    }
//                }
//            }
//            catch (NumberFormatException ex) {
//                error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
//            }
//        }
//        else {
//            try {
//                this.parseState.push(new ConstructorArgumentEntry());
//                Object value = parsePropertyValue(ele, bd, null);
//                ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
//                if (StringUtils.hasLength(typeAttr)) {
//                    valueHolder.setType(typeAttr);
//                }
//                if (StringUtils.hasLength(nameAttr)) {
//                    valueHolder.setName(nameAttr);
//                }
//                valueHolder.setSource(extractSource(ele));
//                bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
//            }
//            finally {
//                this.parseState.pop();
//            }
//        }
    }

    /**
     * Parse property sub-elements of the given bean element.
     */
    public void parsePropertyElements(Element beanEle, BeanDefinition bd) {
        Iterator<Element> iterator = beanEle.elementIterator();
        while(iterator.hasNext()){
            Element node = iterator.next();
            if(node.getName().equals(PROPERTY_ELEMENT)){
                parsePropertyElement((Element) node, bd);
            }
        }
    }

    /**
     * Parse a property element.
     */
    public void parsePropertyElement(Element ele, BeanDefinition bd) {
        String propertyName = ele.attributeValue(NAME_ATTRIBUTE);
        if (!StringUtils.hasLength(propertyName)) {
            throw new BeanDefinitionStoreException("Tag 'property' must have a 'name' attribute");
        }

        if (bd.getPropertyValues().contains(propertyName)) {
            throw new BeanDefinitionStoreException("Multiple 'property' definitions for property '" + propertyName + "'");
        }
        Object val = parsePropertyValue(ele, bd, propertyName);
        PropertyValue pv = new PropertyValue(propertyName, val);
        //parseMetaElements(ele, pv);
        //pv.setSource(extractSource(ele));
        bd.getPropertyValues().addPropertyValue(pv);

    }

    /**
     * Get the value of a property element. May be a list etc.
     * Also used for constructor arguments, "propertyName" being null in this case.
     */
    public Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
        String elementName = (propertyName != null) ?
                "<property> element for property '" + propertyName + "'" :
                "<constructor-arg> element";
        Element subElement=null;
        if(ele.elements()!=null && !ele.elements().isEmpty()){
            subElement = (Element) ele.elements().get(0);
        }
        //TODO 加强验证<property>的子标签
        //value和Ref只能同时含有一种
        boolean hasRefAttribute = ele.attribute(REF_ATTRIBUTE)!=null;
        boolean hasValueAttribute = ele.attribute(VALUE_ATTRIBUTE)!=null;
        if (hasRefAttribute && hasValueAttribute){
            throw new BeanDefinitionStoreException(elementName +
                    " is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element");
        }
        //如果是<ref>
        if (hasRefAttribute) {
            String refName = ele.attributeValue(REF_ATTRIBUTE);
            if (!StringUtils.hasText(refName)) {
                throw  new BeanDefinitionStoreException(elementName + " contains empty 'ref' attribute");
            }
            RuntimeBeanReference ref = new RuntimeBeanReference(refName);
            //ref.setSource(extractSource(ele));
            return ref;
        }
        else if (hasValueAttribute) {
            TypedStringValue valueHolder = new TypedStringValue(ele.attributeValue(VALUE_ATTRIBUTE));
            //valueHolder.setSource(extractSource(ele));
            return valueHolder;
        }
        else if (subElement != null) {//内连的<ref>和<value>
            return parsePropertySubElement(subElement, bd);
        }
        else {
            // Neither child element nor "ref" or "value" attribute found.
            throw new BeanDefinitionStoreException(elementName + " must specify a ref or value");
        }
    }

    public Object parsePropertySubElement(Element ele, BeanDefinition bd) {
        return parsePropertySubElement(ele, bd, null);
    }

    /**
     * Parse a value, ref or collection sub-element of a property or
     * constructor-arg element.
     */
    public Object parsePropertySubElement(Element ele, BeanDefinition bd, String defaultValueType) {
//        if (!isDefaultNamespace(ele)) {
//            return parseNestedCustomElement(ele, bd);
//        }
        if (nodeNameEquals(ele, BEAN_ELEMENT)) {//处理内联<Bean></Bean>
            BeanDefinitionHolder nestedBd = parseBeanDefinitionElement(ele, bd);
//            if (nestedBd != null) {
//                nestedBd = decorateBeanDefinitionIfRequired(ele, nestedBd, bd);
//            }
            return nestedBd;
        }
        else if (nodeNameEquals(ele, REF_ELEMENT)) {
           String refName = ele.attributeValue("local");
            if (!StringUtils.hasText(refName)) {
                throw new BeanDefinitionStoreException("<ref> element contains empty target attribute");
            }
            RuntimeBeanReference ref = new RuntimeBeanReference(refName);
            //ref.setSource(extractSource(ele));
            return ref;
        }
//        else if (nodeNameEquals(ele, IDREF_ELEMENT)) {
//            return parseIdRefElement(ele);
//        }
        else if (nodeNameEquals(ele, VALUE_ELEMENT)) {
            return parseValueElement(ele, defaultValueType);
        }
//        else if (nodeNameEquals(ele, NULL_ELEMENT)) {
//            // It's a distinguished null value. Let's wrap it in a TypedStringValue
//            // object in order to preserve the source location.
//            TypedStringValue nullHolder = new TypedStringValue(null);
//            nullHolder.setSource(extractSource(ele));
//            return nullHolder;
//        }
//        else if (nodeNameEquals(ele, ARRAY_ELEMENT)) {
//            return parseArrayElement(ele, bd);
//        }
//        else if (nodeNameEquals(ele, LIST_ELEMENT)) {
//            return parseListElement(ele, bd);
//        }
//        else if (nodeNameEquals(ele, SET_ELEMENT)) {
//            return parseSetElement(ele, bd);
//        }
//        else if (nodeNameEquals(ele, MAP_ELEMENT)) {
//            return parseMapElement(ele, bd);
//        }
//        else if (nodeNameEquals(ele, PROPS_ELEMENT)) {
//            return parsePropsElement(ele);
//        }
        else {
            throw new BeanDefinitionStoreException("Unknown property sub-element: [" + ele.getName() + "]");
        }
    }

    /**
     * Return a typed String value Object for the given value element.
     */
    public Object parseValueElement(Element ele, String defaultTypeName) {
        // It's a literal value.
        String value = ele.getText().trim();
        String specifiedTypeName="";
//        String specifiedTypeName = ele.getAttribute(TYPE_ATTRIBUTE);
        String typeName = specifiedTypeName;
        if (!StringUtils.hasText(typeName)) {
            typeName = defaultTypeName;
        }
        try {
            TypedStringValue typedValue = buildTypedStringValue(value, typeName);
            //typedValue.setSource(extractSource(ele));
            typedValue.setSpecifiedTypeName(specifiedTypeName);
            return typedValue;
        }
        catch (ClassNotFoundException ex) {
            throw new BeanDefinitionStoreException("Type class [" + typeName + "] not found for <value> element");
        }
    }

    /**
     * Build a typed String value Object for the given raw value.
     */
    protected TypedStringValue buildTypedStringValue(String value, String targetTypeName)
            throws ClassNotFoundException {

        ClassLoader classLoader = this.readerContext.getBeanClassLoader();
        TypedStringValue typedValue;
        if (!StringUtils.hasText(targetTypeName)) {
            typedValue = new TypedStringValue(value);
        }
        else if (classLoader != null) {
            Class<?> targetType = ClassUtils.forName(targetTypeName, classLoader);
            typedValue = new TypedStringValue(value, targetType);
        }
        else {
            typedValue = new TypedStringValue(value, targetTypeName);
        }
        return typedValue;
    }


    /**
     * Apply the attributes of the given bean element to the given bean * definition.
     */
    public AbstractBeanDefinition parseBeanDefinitionAttributes(Element element, String beanName,
                                                                BeanDefinition containingBean, AbstractBeanDefinition bd) {

        if (element.attribute(SCOPE_ATTRIBUTE)!=null) {
            bd.setScope(element.attributeValue(SCOPE_ATTRIBUTE));
        }
        else if (containingBean != null) {
            // Take default from containing bean in case of an inner bean definition.
            bd.setScope(containingBean.getScope());
        }
        //abstract
        if (element.attribute(ABSTRACT_ATTRIBUTE)!=null) {
            bd.setAbstract(TRUE_VALUE.equals(element.attributeValue(ABSTRACT_ATTRIBUTE)));
        }
        String lazyInit ="";
        if(element.attribute(LAZY_INIT_ATTRIBUTE)!=null){
            lazyInit = element.attributeValue(LAZY_INIT_ATTRIBUTE);
            if (DEFAULT_VALUE.equals(lazyInit)) {
                lazyInit = this.defaults.getLazyInit();
            }
        }
        bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

        if(element.attribute(AUTOWIRE_ATTRIBUTE)!=null){
            bd.setAutowireMode(getAutowireMode(element.attributeValue(AUTOWIRE_ATTRIBUTE)));
        }
        if(element.attribute(DEPENDENCY_CHECK_ATTRIBUTE)!=null){
            bd.setDependencyCheck(getDependencyCheck(element.attributeValue(DEPENDENCY_CHECK_ATTRIBUTE)));
        }

        if (element.attribute(DEPENDS_ON_ATTRIBUTE)!=null) {
            String dependsOn = element.attributeValue(DEPENDS_ON_ATTRIBUTE);
            bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, MULTI_VALUE_ATTRIBUTE_DELIMITERS));
        }

//        String autowireCandidate = element.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE);
//        if ("".equals(autowireCandidate) || DEFAULT_VALUE.equals(autowireCandidate)) {
//            String candidatePattern = this.defaults.getAutowireCandidates();
//            if (candidatePattern != null) {
//                String[] patterns = StringUtils.commaDelimitedListToStringArray(candidatePattern);
//                bd.setAutowireCandidate(PatternMatchUtils.simpleMatch(patterns, beanName));
//            }
//        }
//        else {
//            bd.setAutowireCandidate(TRUE_VALUE.equals(autowireCandidate));
//        }

//        if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
//            bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
//        }

        if (element.attribute(INIT_METHOD_ATTRIBUTE)!=null) {
            String initMethodName = element.attributeValue(INIT_METHOD_ATTRIBUTE);
            if (!"".equals(initMethodName)) {
                bd.setInitMethodName(initMethodName);
            }
        }
        else {
            if (this.defaults.getInitMethod() != null) {
                bd.setInitMethodName(this.defaults.getInitMethod());
                bd.setEnforceInitMethod(false);
            }
        }

        if (element.attribute(DESTROY_METHOD_ATTRIBUTE)!=null) {
            String destroyMethodName = element.attributeValue(DESTROY_METHOD_ATTRIBUTE);
            if (!"".equals(destroyMethodName)) {
                bd.setDestroyMethodName(destroyMethodName);
            }
        }
        else {
            if (this.defaults.getDestroyMethod() != null) {
                bd.setDestroyMethodName(this.defaults.getDestroyMethod());
                bd.setEnforceDestroyMethod(false);
            }
        }

//        if (element.attribute(FACTORY_METHOD_ATTRIBUTE)) {
//            bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
//        }
//        if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
//            bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
//        }

        return bd;
    }

    /**
     * Create a bean definition for the given class name and parent name.
     * @param className the name of the bean class
     * @param parentName the name of the bean's parent bean
     * @return the newly created bean definition
     * @throws ClassNotFoundException if bean class resolution was attempted but failed
     */
    protected AbstractBeanDefinition createBeanDefinition(String className, String parentName)
            throws ClassNotFoundException {

        return BeanDefinitionReaderUtils.createBeanDefinition(
                parentName, className, this.readerContext.getBeanClassLoader());
    }


    /**
     * Validate that the specified bean name and aliases have not been used already
     * within the current level of beans element nesting.
     */
    protected void checkNameUniqueness(String beanName, List<String> aliases, Element beanElement) {
        String foundName = null;

        if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
            foundName = beanName;
        }
        if (foundName == null) {
            foundName = CollectionUtils.findFirstMatch(this.usedNames, aliases);
        }
        if (foundName != null) {
            throw  new BeanDefinitionStoreException("Bean name '" + foundName + "' is already used in this <beans> element");
        }

        this.usedNames.add(beanName);
        this.usedNames.addAll(aliases);
    }

    @SuppressWarnings("deprecation")
    public int getAutowireMode(String attValue) {
        String att = attValue;
        if (DEFAULT_VALUE.equals(att)) {
            att = this.defaults.getAutowire();
        }
        int autowire = AbstractBeanDefinition.AUTOWIRE_NO;
        if (AUTOWIRE_BY_NAME_VALUE.equals(att)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_BY_NAME;
        }
        else if (AUTOWIRE_BY_TYPE_VALUE.equals(att)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
        }
        else if (AUTOWIRE_CONSTRUCTOR_VALUE.equals(att)) {
            autowire = AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
        }
        // Else leave default value.
        return autowire;
    }

    public int getDependencyCheck(String attValue) {
        String att = attValue;
        if (DEFAULT_VALUE.equals(att)) {
            att = this.defaults.getDependencyCheck();
        }
        if (DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE.equals(att)) {
            return AbstractBeanDefinition.DEPENDENCY_CHECK_ALL;
        }
        else if (DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE.equals(att)) {
            return AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS;
        }
        else if (DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE.equals(att)) {
            return AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
        }
        else {
            return AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;
        }
    }

    private boolean nodeNameEquals(Element ele, String beanElement) {
        return ele.getName().equals(beanElement);
    }
}
