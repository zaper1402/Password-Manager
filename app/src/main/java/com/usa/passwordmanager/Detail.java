package com.usa.passwordmanager;

public class Detail {
    public String userName;
    public String title;
    public String password;
    public String comment;

    public Detail(){}

    public Detail(String title, String userName, String password, String comment) {
        this.title = title;
        this.userName = userName;
        this.password = password;
        this.comment = comment;
    }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
