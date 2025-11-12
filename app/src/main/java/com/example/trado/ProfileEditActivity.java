package com.example.trado;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import androidx.appcompat.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.trado.databinding.ActivityProfileEditBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "PROFILE_EDIT_TAG";
    private static final int TIMEOUT_SECONDS = 60;
    private static final String CLOUD_NAME = "dhlj92dvw";
    private static final String UPLOAD_PRESET = "trado_app";
    private static final String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private ActivityProfileEditBinding binding;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference dbRef;
    private ProgressDialog progressDialog;
    private String myUserType = "";
    private Uri imageUri = null;

    private String name = "", email = "", phoneCode = "", phoneNumber = "", dob = "";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());
        binding.pickImageFab.setOnClickListener(v -> imagePickDialog());
        binding.updateBtn.setOnClickListener(v -> validateData());
    }

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();

        if (imageUri == null) updateProfileDb(null);
        else uploadProfileImageToCloudinary();
    }

    private void uploadProfileImageToCloudinary() {
        progressDialog.setMessage("Uploading profile image...");
        progressDialog.show();

        byte[] imgBytes = uriToBytes(imageUri);
        if (imgBytes == null) {
            progressDialog.dismiss();
            Utils.toast(this, "Failed to read image");
            return;
        }

        String filename = getFileNameFromUri(imageUri);
        if (filename == null) filename = firebaseAuth.getUid() + "_profile.jpg";

        String mime = getContentResolver().getType(imageUri);
        MediaType mediaType = MediaType.parse(mime != null ? mime : "image/jpeg");

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filename, RequestBody.create(imgBytes, mediaType))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("resource_type", "image")
                .build();

        Request request = new Request.Builder()
                .url(CLOUDINARY_UPLOAD_URL)
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final java.io.IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Utils.toast(ProfileEditActivity.this, "Image upload failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                String respBody = "";
                try {
                    if (response.body() != null) respBody = response.body().string();
                } catch (Exception e) {
                    respBody = "";
                }

                if (!response.isSuccessful()) {
                    String errMsg = "Upload failed: HTTP " + response.code();
                    try {
                        JSONObject errObj = new JSONObject(respBody);
                        if (errObj.has("error")) {
                            JSONObject err = errObj.optJSONObject("error");
                            if (err != null && err.has("message")) errMsg += " - " + err.optString("message");
                        } else {
                            errMsg += " - " + respBody;
                        }
                    } catch (Exception ignored) {
                        errMsg += " - " + respBody;
                    }
                    final String finalErr = errMsg;
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, finalErr);
                    });
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(respBody);
                    String imageUrl = obj.optString("secure_url", "");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        String debug = "No secure_url returned: " + respBody;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Utils.toast(ProfileEditActivity.this, debug);
                        });
                        return;
                    }
                    runOnUiThread(() -> updateProfileDb(imageUrl));
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Upload parse error: " + e.getMessage());
                    });
                } finally {
                    if (response.body() != null) response.close();
                }
            }
        });
    }

    private void updateProfileDb(String imageUrl) {
        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("dob", dob);
        if (imageUrl != null) data.put("profileImageUrl", imageUrl);

        if (!myUserType.equalsIgnoreCase("Email") && !myUserType.equalsIgnoreCase("Google")) {
            data.put("email", email);
        } else if (myUserType.equalsIgnoreCase("Phone")) {
            data.put("phoneCode", phoneCode);
            data.put("phoneNumber", phoneNumber);
        }

        dbRef.child(firebaseAuth.getUid())
                .updateChildren(data)
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
        dbRef.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String dob = snapshot.child("dob").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);
                            String name = snapshot.child("name").getValue(String.class);
                            String phoneCode = snapshot.child("phoneCode").getValue(String.class);
                            String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                            String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                            myUserType = snapshot.child("userType").getValue(String.class);

                            binding.nameEt.setText(name);
                            binding.emailEt.setText(email);
                            binding.dobEt.setText(dob);
                            binding.phoneNumberEt.setText(phoneNumber);
                            if (phoneCode != null && !phoneCode.isEmpty() && phoneCode.startsWith("+")) {
                                try {
                                    int code = Integer.parseInt(phoneCode.replace("+", ""));
                                    binding.countryCodePicker.setCountryForPhoneCode(code);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid phone code: " + phoneCode);
                                }
                            } else binding.countryCodePicker.setDefaultCountryUsingNameCode("IN");

                            if (profileImageUrl != null && !profileImageUrl.isEmpty())
                                Glide.with(ProfileEditActivity.this).load(profileImageUrl).into(binding.profileIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "DB load error", error.toException());
                    }
                });
    }

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.pickImageFab);
        popupMenu.getMenu().add("Camera");
        popupMenu.getMenu().add("Gallery");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Camera")) pickImageCamera();
            else pickImageGallery();
            return true;
        });
        popupMenu.show();
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(binding.profileIv);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Glide.with(this).load(imageUri).into(binding.profileIv);
                }
            });

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Profile Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Camera Image");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private byte[] uriToBytes(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) >= 0) baos.write(buf, 0, n);
            is.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String name = null;
        try {
            String[] proj = {MediaStore.MediaColumns.DISPLAY_NAME};
            android.database.Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                cursor.close();
            }
        } catch (Exception ignored) {}
        return name;
    }
}
