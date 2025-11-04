package com.example.trado;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trado.databinding.ActivityLoginEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginEmailActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private static final String TAG = "LOGIN_TAG";
    private FirebaseAuth firebaseAuth;
    private ActivityLoginEmailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);



        binding.toolbarBackBtn.setOnClickListener(view -> onBackPressed());

        binding.loginBtn.setOnClickListener(view -> validateData());

        binding.forgotPasswordTv.setOnClickListener(v -> {
            String email = binding.emailEt.getText().toString().trim();
            forgotPassword(email);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void forgotPassword(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Utils.toast(this, "Enter a valid email first");
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> Utils.toast(this, "Reset email sent to " + email))
                .addOnFailureListener(e -> Utils.toast(this, "Failed: " + e.getMessage()));
    }


    private String email, password;

    private void validateData(){
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTil.setError("Invalid Email Format");
            binding.emailEt.requestFocus();
        }else if (!email.matches("^[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+\\.)?nitw\\.ac\\.in$")) {
            binding.emailTil.setError("Email must end with nitw.ac.in domain");
            binding.emailEt.requestFocus();
        }
        else if(password.isEmpty()){
            binding.passwordEt.setError("Password cannot be empty");
            binding.passwordEt.requestFocus();
        } else {
            loginUser();
        }
    }


    private void loginUser(){
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "onSuccess: Logged in");
                    progressDialog.dismiss();
                    startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: " + e);
                    Utils.toast(LoginEmailActivity.this, "Failed due to " + e.getMessage());
                    progressDialog.dismiss();
                });
    }
}