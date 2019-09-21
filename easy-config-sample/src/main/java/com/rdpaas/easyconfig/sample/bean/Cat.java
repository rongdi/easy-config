package com.rdpaas.easyconfig.sample.bean;

public class Cat {

    private Person person;

    public Cat() {
    }

    public Cat(Person person) {
        this.person = person;
    }

    public String getName() {
        return "我是猫，我的主人是:" + person.getName();
    }

    @Override
    public String toString() {
        return "Cat{" +
            "person=" + person +
            '}';
    }
}
