package com.alexco.simplevertexservice;

public class User {
    String uuid;
    String name;
    String email;
    int age;

    public User(String uuid, String name, String email, int age) {
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }
}
