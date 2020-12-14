package com.example.droxbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.droxbox.pojo.User;
import com.example.droxbox.singletons.AuthAPI;
import com.example.droxbox.singletons.FirestoreAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private AuthAPI mAuthAPI;
    private FirestoreAPI mFirestoreAPI;
    private UserSingleton mUserSingleton;
    private ConstraintLayout mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuthAPI = AuthAPI.getInstance();
        mFirestoreAPI = FirestoreAPI.getInstance();
        mUserSingleton = UserSingleton.getInstance();

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnSubmit = findViewById(R.id.btn_login);
        mView = findViewById(R.id.login_view);
        TextView tvRegisterNow = findViewById(R.id.tv_register_action);


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( checkCredentials(etEmail, etPassword).size() > 0 ){
                    ArrayList<String> credentials = checkCredentials(etEmail, etPassword);
                     login( credentials.get(0), credentials.get(1) );
                }
            }
        });

        tvRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private ArrayList<String> checkCredentials(EditText etEmail, EditText etPassword ){
        String email = "";
        String password = "";
        ArrayList<String> credentials = new ArrayList<>();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if ( etEmail.getText().toString().trim().isEmpty() ){
            etEmail.setError("Email address is required");
        }else{
            if( !etEmail.getText().toString().trim().matches(emailPattern) ) {
                etEmail.setError("Email address is required");
            }else{
                email = etEmail.getText().toString().trim();
            }
        }

        if ( etPassword.getText().toString().trim().isEmpty() ){
            etPassword.setError("Password is required");
        }else{
            password = etPassword.getText().toString().trim();
        }

        if ( !email.isEmpty() && !password.isEmpty() ){
            credentials.add(email);
            credentials.add(password);
        }

        return credentials;
    }

    private void login( String email, String password ){
        mAuthAPI.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if ( task.isSuccessful() ){
                            mFirestoreAPI.getUserByUid(mAuthAPI.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if ( task.getResult() != null && task.getResult().exists() ){
                                        configUser(task.getResult().toObject(User.class));
                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(getApplicationContext(),getApplicationContext().getString( R.string.login_error_message), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else if ( task.getException() != null){
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                Snackbar.make(mView, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    private void configUser( User  user ){
        if ( mUserSingleton != null ) {
            if ( user != null ) {
                mUserSingleton.getUser().setUid(user.getUid());
                mUserSingleton.getUser().setFullName(user.getFullName());
                mUserSingleton.getUser().setEmail(user.getEmail());
                mUserSingleton.getUser().setUsername(user.getUsername());
                mUserSingleton.getUser().setFiles(user.getFiles());
            }
        }
    }

}