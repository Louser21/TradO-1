package com.example.trado;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.trado.databinding.FragmentAccountBinding;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.android.gms.tasks.*;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private static final String TAG = "ACCOUNT_TAG";
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private ProgressDialog progressDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.logoutCv.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(mContext, MainActivity.class));
            requireActivity().finishAffinity();
        });

        binding.editProfileCv.setOnClickListener(v ->
                startActivity(new Intent(mContext, ProfileEditActivity.class))
        );

        binding.verifyAccountCv.setOnClickListener(v -> verifyAccount());

        binding.changePasswordCv.setOnClickListener(v -> showChangePasswordDialog());
        binding.deleteAccountCv.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        String uid = firebaseAuth.getUid();
        if (uid == null) {
            Utils.toast(mContext, "User not logged in");
            return;
        }

        ref.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dob = "" + snapshot.child("dob").getValue();
                String email = "" + snapshot.child("email").getValue();
                String name = "" + snapshot.child("name").getValue();
                String phoneCode = "" + snapshot.child("phoneCode").getValue();
                String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                String timestamp = "" + snapshot.child("timestamp").getValue();
                String userType = "" + snapshot.child("userType").getValue();

                String phone = phoneCode + phoneNumber;

                if (timestamp.equals("null") || timestamp.isEmpty()) timestamp = "0";

                String formattedDate = Utils.formatTimestampDate(Long.parseLong(timestamp));
                binding.nameTv.setText(name);
                binding.emailTv.setText(email);
                binding.dobTv.setText(dob);
                binding.phoneTv.setText(phone);
                binding.memberSinceTv.setText(formattedDate);

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (userType.equals("Email")) {
                    if (user != null && user.isEmailVerified()) {
                        binding.verifyAccountCv.setVisibility(View.GONE);
                        binding.verificationTv.setText("Verified");
                    } else {
                        binding.verifyAccountCv.setVisibility(View.VISIBLE);
                        binding.verificationTv.setText("Not Verified");
                    }
                } else {
                    binding.verifyAccountCv.setVisibility(View.GONE);
                    binding.verificationTv.setText("Verified");
                }

                try {
                    Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv);
                } catch (Exception e) {
                    Log.e(TAG, "onDataChange: ", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void verifyAccount() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Utils.toast(mContext, "No user logged in!");
            return;
        }

        progressDialog.setMessage("Sending account verification instructions...");
        progressDialog.show();

        user.sendEmailVerification()
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Utils.toast(mContext, "Verification email sent successfully!");
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(mContext, "Failed: " + e.getMessage());
                });
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Utils.toast(mContext, "No user logged in!");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Change Password");

        View dialogView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, null);
        EditText currentPassEt = new EditText(mContext);
        currentPassEt.setHint("Enter current password");
        currentPassEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText newPassEt = new EditText(mContext);
        newPassEt.setHint("Enter new password (min 6 chars)");
        newPassEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        ViewGroup layout = new ViewGroup(mContext) {
            @Override
            protected void onLayout(boolean b, int i, int i1, int i2, int i3) {}
        };
        layout.addView(currentPassEt);
        layout.addView(newPassEt);
        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPass = currentPassEt.getText().toString().trim();
            String newPass = newPassEt.getText().toString().trim();

            if (currentPass.isEmpty() || newPass.length() < 6) {
                Utils.toast(mContext, "Enter valid passwords");
                return;
            }

            progressDialog.setMessage("Verifying...");
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.setMessage("Updating password...");
                        user.updatePassword(newPass)
                                .addOnSuccessListener(unused -> {
                                    progressDialog.dismiss();
                                    Utils.toast(mContext, "Password updated successfully!");
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Utils.toast(mContext, "Update failed: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Utils.toast(mContext, "Reauthentication failed: " + e.getMessage());
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteAccountDialog() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Utils.toast(mContext, "No user logged in!");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Delete Account")
                .setMessage("Please enter your password to confirm deletion:");

        final EditText passwordEt = new EditText(mContext);
        passwordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordEt);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String password = passwordEt.getText().toString().trim();
            if (password.isEmpty()) {
                Utils.toast(mContext, "Enter your password first!");
                return;
            }

            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null || currentUser.getEmail() == null) {
                Utils.toast(mContext, "User not logged in properly!");
                return;
            }

            progressDialog.setMessage("Verifying...");
            progressDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

            currentUser.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(currentUser.getUid()).removeValue()
                                .addOnSuccessListener(unused -> {
                                    currentUser.delete()
                                            .addOnSuccessListener(aVoid2 -> {
                                                progressDialog.dismiss();
                                                Utils.toast(mContext, "Account deleted successfully!");
                                                startActivity(new Intent(mContext, MainActivity.class));
                                                requireActivity().finishAffinity();
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.dismiss();
                                                Utils.toast(mContext, "Failed: " + e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Utils.toast(mContext, "Database error: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Utils.toast(mContext, "Reauthentication failed: " + e.getMessage());
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
