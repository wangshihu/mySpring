package com.springframework.tests.sample.beans;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public class TestBean implements ITestBean {

    private ITestBean spouse;
    private int age;
    private String name;


    @Override
    public ITestBean getSpouse() {
        return spouse;
    }

    @Override
    public void setSpouse(ITestBean spouse) {
        this.spouse = spouse;
    }

    @Override
    public int getAge() {
        return age;
    }



    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
