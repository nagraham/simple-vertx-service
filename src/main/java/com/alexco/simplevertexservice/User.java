package com.alexco.simplevertexservice;

public class User {
    String uuid;
    String name;
    int age;

    public User() {
        // no op default constructor
    }

    public User(String uuid, String name, int age) {
        this.uuid = uuid;
        this.name = name;
        this.age = age;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String toString() {
        return String.format("User: {id: %s, name: %s, age: %d}", uuid, name, age);
    }
}
