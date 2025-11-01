package com.example.trado;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.UUID;

public class AddAdActivity extends AppCompatActivity {

    private EditText titleEt, descEt, priceEt, imageUrlEt;
    private ImageView imageIv;
    private Button uploadBtn;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ad);

        titleEt = findViewById(R.id.titleEt);
        descEt = findViewById(R.id.descEt);
        priceEt = findViewById(R.id.priceEt);
        imageIv = findViewById(R.id.imageIv);
        uploadBtn = findViewById(R.id.uploadBtn);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("ads");

        uploadBtn.setOnClickListener(v -> uploadAd());
    }

    private void uploadAd() {
        String title = titleEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String priceStr = priceEt.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.show();

        String adId = UUID.randomUUID().toString();
        String ownerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";

        // Dummy image URL for testing (can change anytime)
        String imageUrl = "https://picsum.photos/300";

        HashMap<String, Object> adData = new HashMap<>();
        adData.put("id", adId);
        adData.put("title", title);
        adData.put("desc", desc);
        adData.put("price", priceStr);
        adData.put("imageUrl", imageUrl);
        adData.put("ownerId", ownerId);

        dbRef.child(adId).setValue(adData)
                .addOnSuccessListener(a -> {
                    pd.dismiss();
                    Toast.makeText(this, "Ad uploaded successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
