package com.example.trado;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.trado.adapters.AdAdapter;
import com.example.trado.adapters.CategoryAdapter;
import com.example.trado.databinding.FragmentHomeBinding;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding b;
    private AdAdapter adAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Ad> allAds = new ArrayList<>();
    private List<Ad> filteredAds = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentHomeBinding.inflate(inflater, container, false);

        dbRef = FirebaseDatabase.getInstance().getReference("ads");

        setupCategories();
        setupAds();
        setupSearch();

        return b.getRoot();
    }

    private void setupCategories() {
        categories.add(new Category("Mobiles", R.drawable.ic_mobile));
        categories.add(new Category("Laptops", R.drawable.ic_laptop));
        categories.add(new Category("Furniture", R.drawable.ic_furniture));
        categories.add(new Category("Vehicles", R.drawable.ic_vehicle));
        categories.add(new Category("Books", R.drawable.ic_books));

        categoryAdapter = new CategoryAdapter(categories);
        b.categoriesRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        b.categoriesRv.setAdapter(categoryAdapter);
    }

    private void setupAds() {
        adAdapter = new AdAdapter(getContext(), filteredAds);
        b.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        b.recyclerView.setAdapter(adAdapter);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Ad ad = ds.getValue(Ad.class);
                    if (ad != null) {
                        ad.setId(ds.getKey());
                        allAds.add(ad);
                    }
                }
                filteredAds.clear();
                filteredAds.addAll(allAds);
                adAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSearch() {
        b.searchEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                filteredAds.clear();
                for (Ad ad : allAds) {
                    if (ad.getTitle().toLowerCase().contains(query) || ad.getDesc().toLowerCase().contains(query)) {
                        filteredAds.add(ad);
                    }
                }
                adAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
