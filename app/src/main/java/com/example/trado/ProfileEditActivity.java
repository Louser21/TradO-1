package com.example.trado;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.example.trado.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private String myUserType = "";
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());

        binding.pickImageFab.setOnClickListener(v -> imagePickDialog());

        binding.updateBtn.setOnClickListener(v -> validateData());
    }

    private String name = "", email = "", phoneCode = "", phoneNumber = "", dob = "";

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();

        if (imageUri == null) updateProfileDb(null);
        else uploadProfileImageStorage();
    }

    private void uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ");
        progressDialog.setMessage("Uploading profile image...");
        progressDialog.show();

        String filePathAndName = "UserImages/" + "profile_" + firebaseAuth.getUid();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);

        ref.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploading profile image: " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "onSuccess: Uploaded");
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnSuccessListener(uri -> updateProfileDb(uri.toString()));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                    progressDialog.dismiss();
                    Utils.toast(ProfileEditActivity.this, "Failed to upload image: " + e.getMessage());
                });
    }

    private void updateProfileDb(String imageUrl) {
        progressDialog.setMessage("Updating user info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("dob", dob);

        if (imageUrl != null) hashMap.put("profileImageUrl", imageUrl);

        if (!myUserType.equalsIgnoreCase("Email") && !myUserType.equalsIgnoreCase("Google")) {
            hashMap.put("email", email);
        } else if (myUserType.equalsIgnoreCase("Phone")) {
            hashMap.put("phoneCode", phoneCode);
            hashMap.put("phoneNumber", phoneNumber);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Utils.toast(ProfileEditActivity.this, "Profile updated successfully!");
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(ProfileEditActivity.this, "Update failed: " + e.getMessage());
                });
    }

    private void loadMyInfo() {
        Log.d(TAG, "loadMyInfo:");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dob = "" + snapshot.child("dob").getValue();
                String email = "" + snapshot.child("email").getValue();
                String name = "" + snapshot.child("name").getValue();
                String phoneCode = "" + snapshot.child("phoneCode").getValue();
                String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                myUserType = "" + snapshot.child("userType").getValue();

                if (myUserType.equalsIgnoreCase("Email") || myUserType.equalsIgnoreCase("Google")) {
                    binding.emailEt.setEnabled(false);
                } else {
                    binding.phoneNumberEt.setEnabled(false);
                    binding.countryCodePicker.setEnabled(false);
                }

                binding.emailEt.setText(email);
                binding.nameEt.setText(name);
                binding.phoneNumberEt.setText(phoneNumber);
                binding.dobEt.setText(dob);

                try {
                    int codeInt = Integer.parseInt(phoneCode.replace("+", ""));
                    binding.countryCodePicker.setCountryForPhoneCode(codeInt);
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: invalid code " + e);
                }

                try {
                    Glide.with(ProfileEditActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv);
                } catch (Exception e) {
                    Log.e(TAG, "onDataChange: ", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadMyInfo: ", error.toException());
            }
        });
    }

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.pickImageFab);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                } else {
                    requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            } else if (item.getItemId() == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery();
                } else {
                    requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            return true;
        });
        popupMenu.show();
    }

    private final ActivityResultLauncher<String[]> requestCameraPermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean allGranted = true;
                        for (boolean granted : result.values()) {
                            allGranted &= granted;
                        }
                        if (allGranted) pickImageCamera();
                        else Utils.toast(this, "Camera and Storage permissions are required!");
                    });

    private final ActivityResultLauncher<String> requestStoragePermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) pickImageGallery();
                        else Utils.toast(this, "Storage permission is required!");
                    });

    private void pickImageCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp_Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Glide.with(this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileIv);
                        } else Utils.toast(this, "Image capture failed.");
                    });

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            Glide.with(this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_person_white)
                                    .into(binding.profileIv);
                        } else Utils.toast(this, "Image pick failed.");
                    });
}
