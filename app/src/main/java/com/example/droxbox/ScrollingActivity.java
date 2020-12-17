package com.example.droxbox;

import android.content.Intent;
import android.os.Bundle;

import com.example.droxbox.homeModule.HomeActivity;
import com.example.droxbox.loginModule.LoginActivity;
import com.example.droxbox.pojo.User;
import com.example.droxbox.singletons.AuthAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ScrollingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        Button btnStart = findViewById(R.id.btn_start);

        AuthAPI authAPI = AuthAPI.getInstance();
        UserSingleton userSingleton = UserSingleton.getInstance();
        if ( authAPI.getCurrentUser().getUid() != null ){
            Log.i("DataFromScroll", authAPI.getCurrentUser().toString());
            userSingleton.getUser().setUid(authAPI.getCurrentUser().getUid());
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }


    }

}