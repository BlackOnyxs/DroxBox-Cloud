package com.example.droxbox.singletons;

import com.example.droxbox.pojo.User;

public class UserSingleton {

    private User user;
    private static final UserSingleton instance = new UserSingleton();


    public static UserSingleton getInstance(){
        return instance;
    }

    private UserSingleton(){
        this.user = new User();
    }

    public User getUser() {
        return this.user;
    }
    public void  setUser(User user){
        this.user = user;
    }

    public void reset(){
        this.user = null;
    }

}
