package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.trado.databinding.ActivityAdDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

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
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        loadAdDetails();

        b.backBtn.setOnClickListener(v -> finish());
        b.editBtn.setOnClickListener(v -> openEditDialog());
        b.deleteBtn.setOnClickListener(v -> deleteAd());
        b.favBtn.setOnClickListener(v -> toggleFavorite());
        b.sellerLayout.setOnClickListener(v -> viewSellerProfile());
        b.chatBtn.setOnClickListener(v -> startChat());
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
                loadSellerDetails(ownerId);

                boolean isOwner = currentUserId.equals(ownerId);
                if (isOwner) {
                    b.editBtn.setVisibility(View.VISIBLE);
                    b.deleteBtn.setVisibility(View.VISIBLE);
                    b.favBtn.setVisibility(View.GONE);
                    b.sellerDescLabelTv.setVisibility(View.GONE);
                    b.sellerLayout.setVisibility(View.GONE);
                    b.chatBtn.setVisibility(View.GONE);
                } else {
                    b.editBtn.setVisibility(View.GONE);
                    b.deleteBtn.setVisibility(View.GONE);
                    b.favBtn.setVisibility(View.VISIBLE);
                    b.sellerDescLabelTv.setVisibility(View.VISIBLE);
                    b.sellerLayout.setVisibility(View.VISIBLE);
                    b.chatBtn.setVisibility(View.VISIBLE);
                }
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
                String fullName = snapshot.child("fullName").getValue(String.class);
                String memberSince = snapshot.child("memberSince").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                b.sellerNameTv.setText(fullName != null ? fullName : "Seller Name");
                b.memberSinceTv.setText(memberSince != null ? "Member Since " + memberSince : "Member Since N/A");

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

    private void viewSellerProfile() {
        Intent intent = new Intent(this, SellerProfileActivity.class);
        intent.putExtra("userId", ownerId);
        startActivity(intent);
    }

    private void startChat() {
        Toast.makeText(this, "Starting chat with seller: " + ownerId, Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        Toast.makeText(this, "Favorite status changed!", Toast.LENGTH_SHORT).show();
    }

    private void openEditDialog() {}
    private void deleteAd() {}
}
