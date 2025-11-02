package com.example.trado.filters;

import android.widget.Filter;

import com.example.trado.adapters.AdapterChat;
import com.example.trado.models.ModelChats;

import java.util.ArrayList;

public class FilterChats extends Filter {
    private final AdapterChat adapter;
    private final ArrayList<ModelChats> filterList;

    public FilterChats(AdapterChat adapter, ArrayList<ModelChats> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            String search = constraint.toString().toLowerCase().trim();
            ArrayList<ModelChats> filteredModels = new ArrayList<>();
            for (ModelChats model : filterList) {
                if (model.getName() != null && model.getName().toLowerCase().contains(search)) {
                    filteredModels.add(model);
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.chatsArrayList = (ArrayList<ModelChats>) results.values;
        adapter.notifyDataSetChanged();
    }
}
