package com.example.trado;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.trado.databinding.FragmentAccountBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private Context mContext;
    private ProgressDialog progressDialog;
    private static final String TAG = "ACCOUNT_TAG";

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
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

        binding.editProfileCv.setOnClickListener(v -> startActivity(new Intent(mContext, ProfileEditActivity.class)));
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
                } catch (Exception ignored) {}
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

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_change_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        TextInputEditText currentEt = view.findViewById(R.id.currentPasswordEt);
        TextInputEditText newEt = view.findViewById(R.id.newPasswordEt);
        TextInputEditText confirmEt = view.findViewById(R.id.confirmPasswordEt);
        View changeBtn = view.findViewById(R.id.changeBtn);

        changeBtn.setOnClickListener(v -> {
            String curr = currentEt.getText().toString().trim();
            String newP = newEt.getText().toString().trim();
            String conf = confirmEt.getText().toString().trim();

            if (curr.isEmpty() || newP.isEmpty() || conf.isEmpty()) {
                Utils.toast(mContext, "All fields required!");
                return;
            }
            if (!newP.equals(conf)) {
                Utils.toast(mContext, "Passwords don't match!");
                return;
            }

            dialog.dismiss();
            changePassword(curr, newP);
        });
    }

    private void changePassword(String currentPass, String newPass) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Utils.toast(mContext, "User email not found!");
            return;
        }

        progressDialog.setMessage("Updating password...");
        progressDialog.show();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> user.updatePassword(newPass)
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            Utils.toast(mContext, "Password updated successfully!");
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Utils.toast(mContext, "Failed: " + e.getMessage());
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(mContext, "Reauthentication failed: " + e.getMessage());
                });
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

        final View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, null);
        final android.widget.EditText passwordEt = new android.widget.EditText(mContext);
        passwordEt.setHint("Enter password");
        passwordEt.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
