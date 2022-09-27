package com.griddynamics.kafka;

import java.util.HashSet;
import java.util.Set;

public class Test {


    public static void main(String[] args) {
        someClass obj = new someClass(5);
        Set<someClass> collection = new HashSet<>();

        collection.add(obj);
        System.out.println(collection.size());
        collection.add(obj);
        System.out.println(collection.size());
        collection.add(obj);
        System.out.println(collection.size());
        collection.add(obj);
        System.out.println(collection.size());
        collection.add(obj);
        System.out.println(collection.size());
    }
}

class someClass {
    private int number;

    someClass(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}