package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AdDetailsActivity extends AppCompatActivity {

    private ImageView adImage, sellerProfileIv;
    // UI elements
    private TextView titleTv, priceTv, descTv, categoryTv, addressTv, dateTv, statusTv;
    private TextView sellerNameTv, memberSinceTv;
    // Changed buttons to match the simple header
    private ImageButton backBtn, editBtn, deleteBtn, favBtn;
    // Changed bottom buttons to Chat and Call
    private Button chatBtn, callBtn;
    private LinearLayout sellerLayout;

    private DatabaseReference adsRef, usersRef;
    private String adId, currentUserId, ownerId;
    private String firebaseAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_details);

        // --- 1. Initialize UI elements (Simple mapping to the ConstraintLayout) ---

        // Header Buttons (Note: No Toolbar required with this simple header)
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        favBtn = findViewById(R.id.favBtn);

        // Content Views
        adImage = findViewById(R.id.adImage);
        priceTv = findViewById(R.id.priceTv);
        statusTv = findViewById(R.id.statusTv);
        dateTv = findViewById(R.id.dateTv);
        categoryTv = findViewById(R.id.categoryTv);
        titleTv = findViewById(R.id.titleTv);
        descTv = findViewById(R.id.descTv);
        addressTv = findViewById(R.id.addressTv);

        // Seller Info Views
        sellerLayout = findViewById(R.id.sellerLayout);
        sellerProfileIv = findViewById(R.id.sellerProfileIv);
        sellerNameTv = findViewById(R.id.sellerNameTv);
        memberSinceTv = findViewById(R.id.memberSinceTv);

        // Bottom Action Buttons
        chatBtn = findViewById(R.id.chatBtn);

        // --- 2. Firebase Setup ---
        adId = getIntent().getStringExtra("adId");
        if (adId == null) {
            Toast.makeText(this, "Ad not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adsRef = FirebaseDatabase.getInstance().getReference("ads");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                firebaseAuth=FirebaseAuth.getInstance().getCurrentUser().getUid() : "";


        // --- 3. Load Data & Set Listeners ---
        loadAdDetails();

        backBtn.setOnClickListener(v -> finish());
        editBtn.setOnClickListener(v -> openEditDialog());
        deleteBtn.setOnClickListener(v -> deleteAd());

        favBtn.setOnClickListener(v -> toggleFavorite());
        sellerLayout.setOnClickListener(v -> viewSellerProfile());
        chatBtn.setOnClickListener(v -> startChat());
        callBtn.setOnClickListener(v -> startCall());
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

                // Assuming you have an 'Ad' class that maps to your Firebase data
                Ad ad = snapshot.getValue(Ad.class);
                if (ad == null) return;

                // --- Populate Ad Details ---
                titleTv.setText(ad.getTitle());
                priceTv.setText("₹" + ad.getPrice());
                descTv.setText(ad.getDesc());

                // Set other details (assuming fields exist in Ad model)


                Glide.with(AdDetailsActivity.this)
                        .load(ad.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .into(adImage);

                ownerId = ad.getOwnerId();
                loadSellerDetails(ownerId);

                // --- Owner Visibility Logic ---
                boolean isOwner = currentUserId.equals(ownerId);
                if (isOwner) {
                    editBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                    // Hide chat/call from the owner
                    chatBtn.setVisibility(View.GONE);
                    callBtn.setVisibility(View.GONE);
                } else {
                    editBtn.setVisibility(View.GONE);
                    deleteBtn.setVisibility(View.GONE);
                    chatBtn.setVisibility(View.VISIBLE);
                    callBtn.setVisibility(View.VISIBLE);
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
                // Assuming fields like 'fullName', 'memberSince', and 'profileImageUrl' exist
                String fullName = snapshot.child("fullName").getValue(String.class);
                String memberSince = snapshot.child("memberSince").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                sellerNameTv.setText(fullName != null ? fullName : "Seller Name");
                memberSinceTv.setText(memberSince != null ? "Member Since " + memberSince : "Member Since N/A");

                if (profileImageUrl != null) {
                    Glide.with(AdDetailsActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_circle)
                            .into(sellerProfileIv);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // --- New/Updated Action Methods ---
    private void startCall() {
        // Implementation to start a phone call
        Toast.makeText(this, "Calling seller...", Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        // Implementation for favorite logic
        Toast.makeText(this, "Favorite status changed!", Toast.LENGTH_SHORT).show();
    }

    private void viewSellerProfile() {
        // Implementation to navigate to seller's profile activity
        Toast.makeText(this, "Viewing seller profile...", Toast.LENGTH_SHORT).show();
    }

    private void startChat() {
        // Implementation to start a chat with the seller
        Toast.makeText(this, "Starting chat...", Toast.LENGTH_SHORT).show();
    }

    private void openEditDialog() {
        // Use the existing logic from your original code
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
        // Use the existing logic from your original code
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