package com.example.trado;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.example.trado.adapters.AdAdapter;
import com.example.trado.databinding.ActivitySellerProfileBinding;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SellerProfileActivity extends AppCompatActivity {

    private ActivitySellerProfileBinding b;
    private DatabaseReference usersRef, adsRef;
    private String uid;
    private List<Ad> adList;
    private AdAdapter adAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySellerProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        adsRef = FirebaseDatabase.getInstance().getReference("ads");

        b.toolbar.setNavigationOnClickListener(v -> finish());

        adList = new ArrayList<>();
        adAdapter = new AdAdapter(this, adList);
        b.sellerAdsRv.setLayoutManager(new LinearLayoutManager(this));
        b.sellerAdsRv.setAdapter(adAdapter);

        loadSellerInfo();
        loadSellerAds();
    }

    private void loadSellerInfo() {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                b.sellerNameTv.setText(name != null ? name : "Unknown Seller");

                if (timestamp != null && timestamp > 0) {
                    Date date = new Date(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    b.memberSinceTv.setText("Member Since " + sdf.format(date));
                } else {
                    b.memberSinceTv.setText("Member Since — Not Available");
                }

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(SellerProfileActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_circle)
                            .error(R.drawable.ic_person_circle)
                            .into(b.profileImageIv);
                } else {
                    b.profileImageIv.setImageResource(R.drawable.ic_person_circle);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadSellerAds() {
        adsRef.orderByChild("ownerId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        adList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Ad ad = ds.getValue(Ad.class);
                            if (ad != null) adList.add(ad);
                        }

                        adAdapter.notifyDataSetChanged();

                        if (adList.isEmpty()) {
                            b.noAdsIv.setVisibility(android.view.View.VISIBLE);
                            b.noAdsTv.setVisibility(android.view.View.VISIBLE);
                            b.sellerAdsRv.setVisibility(android.view.View.GONE);
                        } else {
                            b.noAdsIv.setVisibility(android.view.View.GONE);
                            b.noAdsTv.setVisibility(android.view.View.GONE);
                            b.sellerAdsRv.setVisibility(android.view.View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }
}
