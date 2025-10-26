package com.example.trado;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.trado.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "REGISTER_TAG";

    private String name, email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());

        binding.registerBtn.setOnClickListener(v -> validateData());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        confirmPassword = binding.confirmPasswordEt.getText().toString().trim();

        if (name.isEmpty()) {
            binding.nameEt.setError("Name cannot be empty");
            binding.nameEt.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailTil.setError("Invalid Email Format");
            binding.emailEt.requestFocus();
        } else if (!email.matches(".*@(nitw\\.ac\\.in|students\\.nitw\\.ac\\.in)$")) {
            binding.emailTil.setError("Email must be a NITW domain");
            binding.emailEt.requestFocus();
        } else if (password.isEmpty()) {
            binding.passwordEt.setError("Password cannot be empty");
            binding.passwordEt.requestFocus();
        } else if (!password.equals(confirmPassword)) {
            binding.confirmPasswordEt.setError("Passwords do not match");
            binding.confirmPasswordEt.requestFocus();
        } else {
            registerUser();
        }
    }

    private void registerUser() {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "onSuccess: User registered");
                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: " + e.getMessage());
                    Utils.toast(RegisterEmailActivity.this, "Failed due to " + e.getMessage());
                    progressDialog.dismiss();
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving User Info");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""); // You can fill with user input
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", ""); // Optional, empty initially
        hashMap.put("dob", "");
        hashMap.put("userType", "Email");
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);
        hashMap.put("email", registerUserEmail);
        hashMap.put("uid", registerUserUid);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Utils.toast(RegisterEmailActivity.this, "Account Created Successfully");
                    startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(RegisterEmailActivity.this, "Failed to save user info: " + e.getMessage());
                });
    }

}
