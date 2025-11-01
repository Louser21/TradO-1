package com.example.trado;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SellerAdsAdapter extends RecyclerView.Adapter<SellerAdsAdapter.SellerAdViewHolder> {

    private Context context;
    private List<Ad> adList; // Assuming 'Ad' is your data model for an advertisement

    public SellerAdsAdapter(Context context, List<Ad> adList) {
        this.context = context;
        this.adList = adList;
    }

    @NonNull
    @Override
    public SellerAdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seller_ad, parent, false);
        return new SellerAdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerAdViewHolder holder, int position) {
        Ad ad = adList.get(position);

        holder.adTitleTv.setText(ad.getTitle());
        holder.adPriceTv.setText("₹" + ad.getPrice()); // Assuming price is a string or convert it

        Glide.with(context)
                .load(ad.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.adImageIv);

        // Optional: Handle favorite button visibility/state if needed
        // holder.adFavBtn.setVisibility(View.VISIBLE);
        // holder.adFavBtn.setOnClickListener(v -> Toast.makeText(context, "Favorite " + ad.getTitle(), Toast.LENGTH_SHORT).show());

        holder.itemView.setOnClickListener(v -> {
            // When an ad is clicked, open its details
            Intent intent = new Intent(context, AdDetailsActivity.class);
            intent.putExtra("adId", ad.getId()); // Assuming Ad object has an getId() method
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    // ViewHolder class
    public static class SellerAdViewHolder extends RecyclerView.ViewHolder {
        ImageView adImageIv;
        TextView adTitleTv, adPriceTv;
        ImageButton adFavBtn;

        public SellerAdViewHolder(@NonNull View itemView) {
            super(itemView);
            adImageIv = itemView.findViewById(R.id.adImageIv);
            adTitleTv = itemView.findViewById(R.id.adTitleTv);
            adPriceTv = itemView.findViewById(R.id.adPriceTv);
            adFavBtn = itemView.findViewById(R.id.adFavBtn);
        }
    }
}