package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.trado.databinding.ActivityAdDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdDetailsActivity extends AppCompatActivity {

    private ActivityAdDetailsBinding b;
    private DatabaseReference adsRef, usersRef;
    private String adId, currentUserId, ownerId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAdDetailsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        adId = getIntent().getStringExtra("adId");
        if (adId == null) {
            Toast.makeText(this, "Ad not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adsRef = FirebaseDatabase.getInstance().getReference("ads");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        loadAdDetails();

        b.backBtn.setOnClickListener(v -> finish());
        b.editBtn.setOnClickListener(v -> openEditDialog());
        b.deleteBtn.setOnClickListener(v -> deleteAd());
        b.favBtn.setOnClickListener(v -> toggleFavorite());
        b.chatBtn.setOnClickListener(v -> startChat());
        b.sellerLayout.setOnClickListener(v -> {
            if (ownerId != null && !ownerId.isEmpty()) {
                viewSellerProfile(ownerId);
            } else {
                Toast.makeText(this, "Seller info not available yet!", Toast.LENGTH_SHORT).show();
            }
        });
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

                b.titleTv.setText(ad.getTitle());
                b.priceTv.setText("₹" + ad.getPrice());
                b.descTv.setText(ad.getDesc());

                Glide.with(AdDetailsActivity.this)
                        .load(ad.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(b.adImage);

                ownerId = ad.getOwnerId();
                if (ownerId != null && !ownerId.isEmpty()) loadSellerDetails(ownerId);

                boolean isOwner = currentUserId.equals(ownerId);
                b.editBtn.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                b.deleteBtn.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                b.favBtn.setVisibility(isOwner ? View.GONE : View.VISIBLE);
                b.sellerDescLabelTv.setVisibility(isOwner ? View.GONE : View.VISIBLE);
                b.sellerLayout.setVisibility(isOwner ? View.GONE : View.VISIBLE);
                b.chatBtn.setVisibility(isOwner ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdDetailsActivity.this, "Failed to load ad: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSellerDetails(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                b.sellerNameTv.setText(name != null ? name : "Seller Name");

                if (timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    String date = sdf.format(new Date(timestamp));
                    b.memberSinceTv.setText("Member Since " + date);
                } else {
                    b.memberSinceTv.setText("Member Since — Not Available");
                }

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(AdDetailsActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_circle)
                            .error(R.drawable.ic_person_circle)
                            .into(b.sellerProfileIv);
                } else {
                    b.sellerProfileIv.setImageResource(R.drawable.ic_person_circle);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void viewSellerProfile(String userId) {
        Intent intent = new Intent(this, SellerProfileActivity.class);
        intent.putExtra("uid", userId); // ✅ FIXED key
        startActivity(intent);
    }


    private void startChat() {
        if (ownerId != null && !ownerId.isEmpty()) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiptUid", ownerId); // 👈 FIXED KEY
            startActivity(intent);
        } else {
            Toast.makeText(this, "Seller info not loaded yet!", Toast.LENGTH_SHORT).show();
        }
    }



    private void toggleFavorite() {
        Toast.makeText(this, "Favorite status changed!", Toast.LENGTH_SHORT).show();
    }

    private void openEditDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_ad, null);
        EditText titleEt = dialogView.findViewById(R.id.titleEt);
        EditText priceEt = dialogView.findViewById(R.id.priceEt);
        EditText descEt = dialogView.findViewById(R.id.descEt);

        titleEt.setText(b.titleTv.getText().toString());
        priceEt.setText(b.priceTv.getText().toString().replace("₹", ""));
        descEt.setText(b.descTv.getText().toString());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Ad")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleEt.getText().toString().trim();
                    String price = priceEt.getText().toString().trim();
                    String desc = descEt.getText().toString().trim();

                    if (title.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                        Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    adsRef.child(adId).child("title").setValue(title);
                    adsRef.child(adId).child("price").setValue(price);
                    adsRef.child(adId).child("desc").setValue(desc)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Ad updated!", Toast.LENGTH_SHORT).show();
                                b.titleTv.setText(title);
                                b.priceTv.setText("₹" + price);
                                b.descTv.setText(desc);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAd() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this ad?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    adsRef.child(adId).removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Ad deleted!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
