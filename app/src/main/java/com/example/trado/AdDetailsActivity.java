package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AdDetailsActivity extends AppCompatActivity {

    private ImageView adImage;
    private TextView titleTv, priceTv, descTv;
    private Button backBtn, editBtn, deleteBtn;
    private DatabaseReference adsRef;
    private String adId, currentUserId, ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_details);

        adImage = findViewById(R.id.adImage);
        titleTv = findViewById(R.id.titleTv);
        priceTv = findViewById(R.id.priceTv);
        descTv = findViewById(R.id.descTv);
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        // ✅ Firebase Realtime Database key name fix
        adId = getIntent().getStringExtra("adId");
        if (adId == null) {
            Toast.makeText(this, "Ad not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adsRef = FirebaseDatabase.getInstance().getReference("ads"); // lowercase to match HomeFragment
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        loadAdDetails();

        backBtn.setOnClickListener(v -> finish());
        editBtn.setOnClickListener(v -> openEditDialog());
        deleteBtn.setOnClickListener(v -> deleteAd());
    }

    private void loadAdDetails() {
        adsRef.child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AdDetailsActivity.this, "Ad not found!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Ad ad = snapshot.getValue(Ad.class);
                if (ad == null) return;

                titleTv.setText(ad.getTitle());
                priceTv.setText("₹" + ad.getPrice());
                descTv.setText(ad.getDesc());
                Glide.with(AdDetailsActivity.this)
                        .load(ad.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(adImage);

                ownerId = ad.getOwnerId();

                // ✅ Hide edit/delete if not the owner
                if (!currentUserId.equals(ownerId)) {
                    editBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void openEditDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_ad, null);
        EditText titleEt = dialogView.findViewById(R.id.titleEt);
        EditText priceEt = dialogView.findViewById(R.id.priceEt);
        EditText descEt = dialogView.findViewById(R.id.descEt);

        titleEt.setText(titleTv.getText());
        priceEt.setText(priceTv.getText().toString().replace("₹", ""));
        descEt.setText(descTv.getText());

        new android.app.AlertDialog.Builder(this)
                .setTitle("Edit Ad")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = titleEt.getText().toString().trim();
                    String newPrice = priceEt.getText().toString().trim();
                    String newDesc = descEt.getText().toString().trim();

                    if (newTitle.isEmpty() || newPrice.isEmpty()) {
                        Toast.makeText(this, "Title and price required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adsRef.child(adId).child("title").setValue(newTitle);
                    adsRef.child(adId).child("price").setValue(newPrice);
                    adsRef.child(adId).child("desc").setValue(newDesc);

                    titleTv.setText(newTitle);
                    priceTv.setText("₹" + newPrice);
                    descTv.setText(newDesc);

                    Toast.makeText(this, "Ad updated!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAd() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this ad?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    adsRef.child(adId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Ad deleted!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
