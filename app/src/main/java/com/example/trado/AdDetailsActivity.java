package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AdDetailsActivity extends AppCompatActivity {

    // UI elements declarations
    private ImageView adImage, sellerProfileIv;
    private TextView titleTv, priceTv, descTv, categoryTv, addressTv, dateTv, statusTv;
    private TextView sellerNameTv, memberSinceTv, sellerDescLabelTv;
    private ImageButton backBtn, editBtn, deleteBtn, favBtn;
    private Button chatBtn1, chatBtn2; // Correctly uses chatBtn1 and chatBtn2
    private LinearLayout sellerLayout;

    private DatabaseReference adsRef, usersRef;
    private String adId, currentUserId, ownerId;
    private Object chatBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_details);

        // --- 1. Initialize UI elements (FindViewById calls are correct) ---
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        favBtn = findViewById(R.id.favBtn);

        adImage = findViewById(R.id.adImage);
        priceTv = findViewById(R.id.priceTv);
        statusTv = findViewById(R.id.statusTv);
        dateTv = findViewById(R.id.dateTv);
        categoryTv = findViewById(R.id.categoryTv);
        titleTv = findViewById(R.id.titleTv);
        descTv = findViewById(R.id.descTv);
        addressTv = findViewById(R.id.addressTv);

        sellerDescLabelTv = findViewById(R.id.sellerDescLabelTv);
        sellerLayout = findViewById(R.id.sellerLayout);
        sellerProfileIv = findViewById(R.id.sellerProfileIv);
        sellerNameTv = findViewById(R.id.sellerNameTv);
        memberSinceTv = findViewById(R.id.memberSinceTv);

        // These IDs MUST exist in your activity_ad_details.xml
        // This line is the error
        chatBtn = findViewById(R.id.chatBtn);




        // --- 2. Firebase Setup (Correct) ---
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


        // --- 3. Load Data & Set Listeners (The core functionality) ---
        loadAdDetails();

        backBtn.setOnClickListener(v -> finish());
        editBtn.setOnClickListener(v -> openEditDialog());
        deleteBtn.setOnClickListener(v -> deleteAd());

        favBtn.setOnClickListener(v -> toggleFavorite());

        // This is the implementation for the clickable seller profile area
        sellerLayout.setOnClickListener(v -> viewSellerProfile());

        chatBtn1.setOnClickListener(v -> startChat());
        chatBtn2.setOnClickListener(v -> startChat());
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

                // *** CRUCIAL: Ad class must be defined with getters/constructor ***
                Ad ad = snapshot.getValue(Ad.class);
                if (ad == null) return;

                // --- Populate Ad Details ---
                titleTv.setText(ad.getTitle());
                priceTv.setText("₹" + ad.getPrice());
                descTv.setText(ad.getDesc());



                Glide.with(AdDetailsActivity.this)
                        .load(ad.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(adImage);

                ownerId = ad.getOwnerId();
                loadSellerDetails(ownerId);

                // --- Owner Visibility Logic (Correct) ---
                boolean isOwner = currentUserId.equals(ownerId);
                if (isOwner) {
                    editBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                    favBtn.setVisibility(View.GONE);

                    sellerDescLabelTv.setVisibility(View.GONE);
                    sellerLayout.setVisibility(View.GONE);
                    chatBtn1.setVisibility(View.GONE);
                    chatBtn2.setVisibility(View.GONE);
                } else {
                    editBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
                    favBtn.setVisibility(View.VISIBLE);

                    sellerDescLabelTv.setVisibility(View.VISIBLE);
                    sellerLayout.setVisibility(View.VISIBLE);
                    chatBtn1.setVisibility(View.VISIBLE);
                    chatBtn2.setVisibility(View.VISIBLE);
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

                sellerNameTv.setText(fullName != null ? fullName : "Seller Name");
                memberSinceTv.setText(memberSince != null ? "Member Since " + memberSince : "Member Since N/A");

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(AdDetailsActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_circle)
                            .error(R.drawable.ic_person_circle)
                            .into(sellerProfileIv);
                } else {
                    sellerProfileIv.setImageResource(R.drawable.ic_person_circle);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // --- Action Methods ---

    // Correct Intent to launch the new activity
    private void viewSellerProfile() {
        Intent intent = new Intent(this, SellerProfileActivity.class);
        intent.putExtra("userId", ownerId);
        startActivity(intent);
        Toast.makeText(this, "Opening Seller Profile for ID: " + ownerId, Toast.LENGTH_SHORT).show();
    }

    private void startChat() {
        Toast.makeText(this, "Starting chat with seller: " + ownerId, Toast.LENGTH_SHORT).show();
    }

    private void startCall() {
        Toast.makeText(this, "Calling seller...", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        Toast.makeText(this, "Favorite status changed!", Toast.LENGTH_SHORT).show();
    }

    private void openEditDialog() {
        // ... (existing code for edit dialog)
    }

    private void deleteAd() {
        // ... (existing code for delete dialog)
    }
}