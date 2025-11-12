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
    private final List<Ad> allAds = new ArrayList<>();
    private final List<Ad> filteredAds = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private DatabaseReference dbRef;
    private ValueEventListener adsListener;

    // filters state
    private String currentQuery = "";
    private String selectedCategory = ""; // empty = no category filter

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
        categories.clear();
        categories.add(new Category("Mobiles", R.drawable.ic_mobile));
        categories.add(new Category("Laptops", R.drawable.ic_laptop));
        categories.add(new Category("Furniture", R.drawable.ic_furniture));
        categories.add(new Category("Vehicles", R.drawable.ic_vehicle));
        categories.add(new Category("Books", R.drawable.ic_books));

        categoryAdapter = new CategoryAdapter(categories, category -> {
            if (category == null) return;
            String name = category.getName() == null ? "" : category.getName().trim();
            // toggle selection: click same category again to clear filter
            if (name.equalsIgnoreCase(selectedCategory)) {
                selectedCategory = "";
            } else {
                selectedCategory = name;
            }
            applyFilters();
        });

        b.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        b.categoriesRv.setHasFixedSize(true);
        b.categoriesRv.setAdapter(categoryAdapter);
    }

    private void setupAds() {
        adAdapter = new AdAdapter(requireContext(), filteredAds);
        b.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recyclerView.setHasFixedSize(true);
        b.recyclerView.setAdapter(adAdapter);

        if (adsListener != null && dbRef != null) {
            dbRef.removeEventListener(adsListener);
        }

        adsListener = new ValueEventListener() {
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
                applyFilters(); // update filteredAds based on current filters
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error if needed (log or toast)
            }
        };

        if (dbRef != null) dbRef.addValueEventListener(adsListener);
    }

    private void setupSearch() {
        b.searchEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = (s == null) ? "" : s.toString().trim().toLowerCase();
                applyFilters();
            }
        });
    }

    // central filter that respects both search query and selected category
    private void applyFilters() {
        filteredAds.clear();

        String q = currentQuery == null ? "" : currentQuery;
        String cat = selectedCategory == null ? "" : selectedCategory.trim().toLowerCase();

        for (Ad ad : allAds) {
            String title = ad.getTitle() == null ? "" : ad.getTitle().toLowerCase();
            String desc = ad.getDesc() == null ? "" : ad.getDesc().toLowerCase();
            String adCat = ad.getCategory() == null ? "" : ad.getCategory().toLowerCase();

            boolean matchesQuery = q.isEmpty() || title.contains(q) || desc.contains(q);
            boolean matchesCategory = cat.isEmpty() || adCat.equalsIgnoreCase(cat);

            if (matchesQuery && matchesCategory) {
                filteredAds.add(ad);
            }
        }
        adAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbRef != null && adsListener != null) dbRef.removeEventListener(adsListener);
        adsListener = null;
        b = null;
    }
}
