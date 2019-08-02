package com.usa.passwordmanager;

public class UserInfo {
    public String name,gender,age;

public UserInfo(){}


    public UserInfo(String name, String gender, String age){
        this.name = name;
        this.gender = gender;
        this.age = age;

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setgender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() { return age; }

    public void setAge(String age) {
        this.age = age;
    }
}
