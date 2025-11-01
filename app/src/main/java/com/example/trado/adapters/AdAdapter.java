package com.example.trado.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trado.AdDetailsActivity;
import com.example.trado.databinding.ItemAdBinding;

import java.util.List;

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.AdViewHolder> {

    private final Context context;
    private final List<Ad> ads;

    public AdAdapter(Context context, List<Ad> ads) {
        this.context = context;
        this.ads = ads;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdBinding binding = ItemAdBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AdViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder h, int pos) {
        Ad ad = ads.get(pos);

        h.binding.adTitle.setText(ad.getTitle() != null ? ad.getTitle() : "No Title");
        h.binding.adDesc.setText(ad.getDesc() != null ? ad.getDesc() : "No Description");
        h.binding.adPrice.setText(ad.getPrice() != null ? "₹" + ad.getPrice() : "₹0");

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, AdDetailsActivity.class);
            i.putExtra("adId", ad.getId());
            context.startActivity(i);
        });


        if (ad.getImageUrl() != null && !ad.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(ad.getImageUrl())
                    .placeholder(com.example.trado.R.drawable.ic_placeholder)
                    .into(h.binding.adImage);
        } else {
            h.binding.adImage.setImageResource(com.example.trado.R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return ads.size();
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        ItemAdBinding binding;

        public AdViewHolder(ItemAdBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
