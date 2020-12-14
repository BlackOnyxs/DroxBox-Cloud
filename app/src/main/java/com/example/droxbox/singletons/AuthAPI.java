package com.example.droxbox.singletons;

import android.util.Log;

import com.example.droxbox.pojo.User;
import com.google.firebase.auth.FirebaseAuth;

public class AuthAPI {
    private FirebaseAuth mFirebaseAuth;

    private static class SingletonHolder{
        private static final AuthAPI INSTANCE = new AuthAPI();
    }

    public static AuthAPI getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private AuthAPI(){
        this.mFirebaseAuth =  FirebaseAuth.getInstance();
    }

    public FirebaseAuth getFirebaseAuth(){
        return this.mFirebaseAuth;
    }

    public User getCurrentUser(){
        User currentUser = new User();
        if( mFirebaseAuth != null && mFirebaseAuth.getCurrentUser() != null ) {
            currentUser.setUid(mFirebaseAuth.getCurrentUser().getUid());
            currentUser.setEmail(mFirebaseAuth.getCurrentUser().getEmail());
            currentUser.setFullName(mFirebaseAuth.getCurrentUser().getDisplayName());
        }

        return currentUser;
    }
}
