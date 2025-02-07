package com.example.droxbox.loginModule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.droxbox.homeModule.HomeActivity;
import com.example.droxbox.R;
import com.example.droxbox.singletons.AuthAPI;
import com.example.droxbox.singletons.FirestoreAPI;
import com.example.droxbox.singletons.UserSingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {
    private AuthAPI mAuthAPI;
    private FirestoreAPI mFirestoreAPI;
    private UserSingleton mUserSingleton;
    private ConstraintLayout mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuthAPI = AuthAPI.getInstance();
        mFirestoreAPI = FirestoreAPI.getInstance();
        mUserSingleton = UserSingleton.getInstance();

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etFullName = findViewById(R.id.et_fullName);

        Button btnSubmit = findViewById(R.id.btn_signUp);
        mView = findViewById(R.id.login_view);
        TextView tvSignIn = findViewById(R.id.tv_register_action);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( checkCredentials(etEmail, etPassword, etFullName).size() == 3 ) {
                   ArrayList<String> credentials = checkCredentials(etEmail, etPassword, etFullName);
                    signUp(credentials.get(0), credentials.get(1), credentials.get(2));

                }
            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    private ArrayList<String> checkCredentials(EditText etEmail, EditText etPassword, EditText etFullName ){
        String email = "";
        String password = "";
        String fullName = "";
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

        if ( etFullName.getText().toString().trim().isEmpty() ){
            etFullName.setError("Your name is required");
        }else{
            fullName = etFullName.getText().toString().trim();
        }

        if ( !email.isEmpty() && !password.isEmpty() && !fullName.isEmpty() ){
            credentials.add(fullName);
            credentials.add(email);
            credentials.add(password);
        }

        return credentials;
    }

    private void signUp(String fullName, String email, String password){
        if ( mAuthAPI != null && mFirestoreAPI != null ) {
            mAuthAPI.getFirebaseAuth().createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    mAuthAPI.getCurrentUser().setFullName(fullName);
                    mFirestoreAPI.getUserCollection().document(mAuthAPI.getCurrentUser()
                            .getUid()).set(mAuthAPI.getCurrentUser())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mUserSingleton.setUser(mAuthAPI.getCurrentUser());
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }




}