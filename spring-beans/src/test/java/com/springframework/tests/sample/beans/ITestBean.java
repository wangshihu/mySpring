package com.springframework.tests.sample.beans;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public interface ITestBean {
    int getAge();

    void setAge(int age);

    String getName();

    void setName(String name);

    ITestBean getSpouse();

    void setSpouse(ITestBean spouse);
}
