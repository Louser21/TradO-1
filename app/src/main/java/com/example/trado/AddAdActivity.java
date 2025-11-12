package com.example.trado;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class AddAdActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 1001;
    private static final String CLOUD_NAME = "dhlj92dvw";
    private static final String UPLOAD_PRESET = "trado_app";
    private static final String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private TextInputEditText titleEt, descEt, priceEt, imageUrlEt;
    private ImageView imageIv;
    private MaterialButton selectImgBtn, uploadBtn;
    private Spinner categorySpinner;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private Uri imageUri;
    private OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ad);

        titleEt = findViewById(R.id.titleEt);
        descEt = findViewById(R.id.descEt);
        priceEt = findViewById(R.id.priceEt);
        imageUrlEt = findViewById(R.id.imageUrlEt);
        imageIv = findViewById(R.id.imageIv);
        selectImgBtn = findViewById(R.id.selectImgBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        categorySpinner = findViewById(R.id.categorySpinner);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("ads");

        String[] cats = {"Mobiles","Laptops","Furniture","Vehicles","Books","Other"};
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        categorySpinner.setAdapter(aa);

        selectImgBtn.setOnClickListener(v -> pickImage());
        uploadBtn.setOnClickListener(v -> uploadAd());
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Select Image"), REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                imageIv.setImageURI(imageUri);
                imageUrlEt.setText("");
            }
        }
    }

    private void uploadAd() {
        String title = titleEt.getText() == null ? "" : titleEt.getText().toString().trim();
        String desc = descEt.getText() == null ? "" : descEt.getText().toString().trim();
        String priceStr = priceEt.getText() == null ? "" : priceEt.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadBtn.setEnabled(false);
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.setCancelable(false);
        pd.show();

        String adId = UUID.randomUUID().toString();
        String ownerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";

        if (imageUri != null) {
            byte[] imgBytes = uriToBytes(imageUri);
            if (imgBytes == null) {
                pd.dismiss();
                uploadBtn.setEnabled(true);
                Toast.makeText(this, "Unable to read image", Toast.LENGTH_SHORT).show();
                return;
            }

            MediaType mediaType = MediaType.parse(getContentResolver().getType(imageUri) != null ? getContentResolver().getType(imageUri) : "image/jpeg");
            RequestBody fileBody = RequestBody.create(imgBytes, mediaType);

            String filename = getFileNameFromUri(imageUri);
            if (filename == null) filename = adId + ".jpg";

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", filename, fileBody)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url(CLOUDINARY_UPLOAD_URL)
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> {
                        pd.dismiss();
                        uploadBtn.setEnabled(true);
                        Toast.makeText(AddAdActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (!response.isSuccessful()) {
                            final String msg = "Upload failed: " + response.code();
                            runOnUiThread(() -> {
                                pd.dismiss();
                                uploadBtn.setEnabled(true);
                                Toast.makeText(AddAdActivity.this, msg, Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                        String body = response.body() != null ? response.body().string() : "";
                        JSONObject obj = new JSONObject(body);
                        String imageUrl = obj.optString("secure_url", "");
                        if (imageUrl.isEmpty()) {
                            runOnUiThread(() -> {
                                pd.dismiss();
                                uploadBtn.setEnabled(true);
                                Toast.makeText(AddAdActivity.this, "No URL returned from Cloudinary", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                        saveAdToDatabase(adId, title, desc, priceStr, imageUrl, ownerId, pd);
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            pd.dismiss();
                            uploadBtn.setEnabled(true);
                            Toast.makeText(AddAdActivity.this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } finally {
                        if (response.body() != null) response.close();
                    }
                }
            });

        } else {
            String imageUrl = (imageUrlEt != null && imageUrlEt.getText() != null && !imageUrlEt.getText().toString().trim().isEmpty())
                    ? imageUrlEt.getText().toString().trim()
                    : "https://picsum.photos/300";

            saveAdToDatabase(adId, title, desc, priceStr, imageUrl, ownerId, pd);
        }
    }

    private void saveAdToDatabase(String adId, String title, String desc, String priceStr, String imageUrl, String ownerId, ProgressDialog pd) {
        String category = categorySpinner != null && categorySpinner.getSelectedItem() != null
                ? categorySpinner.getSelectedItem().toString().trim()
                : "Other";

        Ad ad = new Ad(title, desc, priceStr, imageUrl, ownerId, category);
        ad.setId(adId);

        dbRef.child(adId).setValue(ad)
                .addOnSuccessListener(a -> {
                    pd.dismiss();
                    Toast.makeText(this, "Ad uploaded successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    uploadBtn.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
            String[] proj = {OpenableColumns.DISPLAY_NAME};
            android.database.Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                cursor.close();
            }
        } catch (Exception ignored) {}
        return name;
    }
}
