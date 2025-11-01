package com.example.trado;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.trado.adapters.AdAdapter;
import com.example.trado.databinding.FragmentMyAdsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class MyAdsFragment extends Fragment {

    private FragmentMyAdsBinding b;
    private AdAdapter adapter;
    private ArrayList<Ad> myAds;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;

    public MyAdsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentMyAdsBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("ads");

        myAds = new ArrayList<>();
        adapter = new AdAdapter(getContext(), myAds);
        b.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        b.recyclerView.setAdapter(adapter);

        loadMyAds();

        return b.getRoot();
    }

    private void loadMyAds() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myAds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Ad ad = ds.getValue(Ad.class);
                    if (ad != null && uid.equals(ad.getOwnerId())) {
                        myAds.add(ad);
                    }
                }
                adapter.notifyDataSetChanged();
                if (myAds.isEmpty()) {
                    Toast.makeText(getContext(), "No ads uploaded yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
