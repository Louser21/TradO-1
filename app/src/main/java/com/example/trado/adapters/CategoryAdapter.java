package com.example.trado.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trado.Category;
import com.example.trado.databinding.ItemCategoryBinding;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final List<Category> list;

    public CategoryAdapter(List<Category> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryBinding b;
        public ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            b = binding;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Category c = list.get(pos);
        h.b.imgCategory.setImageResource(c.getImageResId());
        h.b.txtCategory.setText(c.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
