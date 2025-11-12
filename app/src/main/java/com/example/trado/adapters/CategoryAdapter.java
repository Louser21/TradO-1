package com.example.trado.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trado.Category;
import com.example.trado.databinding.ItemCategoryBinding;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> list;
    private final OnCategoryClick listener;

    public interface OnCategoryClick {
        void onClick(Category category);
    }

    public CategoryAdapter(List<Category> list, OnCategoryClick listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCategoryBinding b;
        public ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            b = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Category c = list.get(pos);
        h.b.imgCategory.setImageResource(c.getImageResId());
        h.b.txtCategory.setText(c.getName());

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}
