package com.example.droxbox;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.droxbox.homeModule.HomeActivity;
import com.example.droxbox.loginModule.LoginActivity;
import com.example.droxbox.singletons.AuthAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class ScrollingActivity extends AppCompatActivity {

    private Button btnStart;
    private TextView tvTitle, tvOffline, tvTryAgain;
    private ImageView ivOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        btnStart = findViewById(R.id.btn_start);
        tvTitle = findViewById(R.id.tv_description);
        tvOffline = findViewById(R.id.tvOffline);
        ivOffline = findViewById(R.id.iv_offline);
        tvTryAgain = findViewById(R.id.tvTryAgain);

        if (!isOnline()) {
            showOffline();
            tvTryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isOnline()) {
                        showOffline();
                        Toast.makeText(ScrollingActivity.this, getString( R.string.offline_message), Toast.LENGTH_SHORT).show();
                    } else {
                        showOnline();
                        checkUserLog();
                    }
                }
            });
        }else{
            showOnline();
            checkUserLog();
        }


    }

    private void checkUserLog(){
        AuthAPI authAPI = AuthAPI.getInstance();
        UserSingleton userSingleton = UserSingleton.getInstance();
        if (authAPI.getCurrentUser().getUid() != null) {
            Log.i("DataFromScroll", authAPI.getCurrentUser().toString());
            userSingleton.getUser().setUid(authAPI.getCurrentUser().getUid());
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
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

    private boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService
                    (Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivityManager != null) {
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    private void showOffline() {
        tvTitle.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        tvTryAgain.setVisibility(View.VISIBLE);
        ivOffline.setVisibility(View.VISIBLE);
        tvOffline.setVisibility(View.VISIBLE);
    }

    private void showOnline(){
        tvTitle.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.VISIBLE);
        tvTryAgain.setVisibility(View.GONE);
        ivOffline.setVisibility(View.GONE);
        tvOffline.setVisibility(View.GONE);
    }

}