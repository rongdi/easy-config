package com.rdpaas.easyconfig.sample.bean;

public class Dog {

    private Food food;

    public Dog() {
    }

    public Dog(Food food) {
        this.food = food;
    }

    public String getName() {
        return "我是狗，我喜欢吃:" + food.getName();
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    @Override
    public String toString() {
        return "Dog{" +
            "food=" + food +
            '}';
    }
}
