package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SellerProfileActivity extends AppCompatActivity {

    private String sellerUserId;

    // UI elements
    private ImageButton backBtn;
    private ImageView profileImageIv;
    private TextView sellerNameTv, memberSinceTv;
    private RecyclerView sellerAdsRv;

    // Firebase
    private DatabaseReference usersRef, adsRef;
    private SellerAdsAdapter sellerAdsAdapter;
    private List<Ad> adList; // Assuming you have an 'Ad' data model

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        // --- 1. Get Seller ID from Intent ---
        sellerUserId = getIntent().getStringExtra("userId");
        if (sellerUserId == null || sellerUserId.isEmpty()) {
            Toast.makeText(this, "Seller ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- 2. Initialize UI elements ---
        backBtn = findViewById(R.id.backBtn);
        sellerNameTv = findViewById(R.id.sellerNameTv);
        memberSinceTv = findViewById(R.id.memberSinceTv);
        sellerAdsRv = findViewById(R.id.sellerAdsRv);

        // --- 3. Firebase Setup ---
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        adsRef = FirebaseDatabase.getInstance().getReference("ads");

        // --- 4. RecyclerView Setup ---
        adList = new ArrayList<>();
        sellerAdsAdapter = new SellerAdsAdapter(this, adList);
        sellerAdsRv.setLayoutManager(new LinearLayoutManager(this));
        sellerAdsRv.setAdapter(sellerAdsAdapter);

        // --- 5. Load Seller Data and Ads ---
        loadSellerDetails();
        loadSellerAds();

        // --- 6. Set Listeners ---
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadSellerDetails() {
        usersRef.child(sellerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String memberSince = snapshot.child("memberSince").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    sellerNameTv.setText(fullName != null ? fullName : "Unknown Seller");
                    memberSinceTv.setText(memberSince != null ? "Member Since " + memberSince : "Member Since N/A");

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(SellerProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_person_circle)
                                .error(R.drawable.ic_person_circle)
                                .into(profileImageIv);
                    } else {
                        profileImageIv.setImageResource(R.drawable.ic_person_circle);
                    }
                } else {
                    Toast.makeText(SellerProfileActivity.this, "Seller profile not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SellerProfileActivity.this, "Failed to load seller details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSellerAds() {
        adsRef.orderByChild("ownerId").equalTo(sellerUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        adList.clear();
                        sellerAdsAdapter.notifyDataSetChanged();
                        if (adList.isEmpty()) {
                            Toast.makeText(SellerProfileActivity.this, "This seller has no ads.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SellerProfileActivity.this, "Failed to load seller's ads: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

//SellerProfileActivity