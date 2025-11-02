package com.example.trado;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        progressDialog.setTitle("Please wait");
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
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void verifyAccount() {
        Log.d(TAG, "verifyAccount: ");
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            Utils.toast(mContext, "No user logged in!");
            return;
        }

        progressDialog.setMessage("Sending account verification instructions to your email...");
        progressDialog.show();

        user.sendEmailVerification()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSuccess: Email sent");
                    progressDialog.dismiss();
                    Utils.toast(mContext, "Verification email sent successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    progressDialog.dismiss();
                    Utils.toast(mContext, "Failed: " + e.getMessage());
                });
    }
}
