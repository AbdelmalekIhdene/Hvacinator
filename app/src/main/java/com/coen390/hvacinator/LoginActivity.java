package com.coen390.hvacinator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    EditText psw, mail;
    Button newaccount, login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();

        psw = (EditText) findViewById(R.id.psw);
        mail = (EditText) findViewById(R.id.email);

        newaccount = (Button) findViewById(R.id.newaccount);
        login = (Button) findViewById(R.id.login);
        newaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent s = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(s);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String _mail = mail.getText().toString();
                System.out.println(_mail);
                if(_mail.length() == 0) {
                    Toast.makeText(LoginActivity.this, "Mail field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                String _password = psw.getText().toString();
                if(_password.length() == 0) {
                    Toast.makeText(LoginActivity.this, "Password field is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(_mail, _password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent s = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(s);
                                    updateUI(null);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });

            }
        });
    }
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            findViewById(R.id.login).setVisibility(View.GONE);
        } else {
            findViewById(R.id.login).setVisibility(View.VISIBLE);
        }
    }
}