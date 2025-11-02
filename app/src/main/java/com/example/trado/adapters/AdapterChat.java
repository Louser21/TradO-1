package com.example.trado.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trado.ChatActivity;
import com.example.trado.R;
import com.example.trado.Utils;
import com.example.trado.filters.FilterChats;
import com.example.trado.models.ModelChats;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.HolderChats> implements Filterable {

    private final Context context;
    public ArrayList<ModelChats> chatsArrayList;
    public ArrayList<ModelChats> filterList;
    private FilterChats filter;
    private final String myUid;

    public AdapterChat(Context context, ArrayList<ModelChats> chatsArrayList, String myUid) {
        this.context = context;
        this.chatsArrayList = chatsArrayList;
        this.filterList = chatsArrayList;
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public HolderChats onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chats, parent, false);
        return new HolderChats(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderChats holder, int position) {
        ModelChats model = chatsArrayList.get(position);

        Glide.with(context)
                .load(model.getProfileImageUrl())
                .placeholder(R.drawable.ic_person_grey)
                .into(holder.profileIv);

        holder.nameTv.setText(model.getName() != null ? model.getName() : "Unknown");
        holder.lastMessageTv.setText(model.getLastMessage() != null ? model.getLastMessage() : "");
        holder.dateTimeTv.setText(Utils.formatTimestampDate(model.getTimestamp()));

        holder.itemView.setOnClickListener(v -> {
            String receiptUid = model.getReceiptUid();

            if (receiptUid == null || receiptUid.isEmpty()) {
                // try to derive from chatKey
                String chatKey = model.getChatKey();
                if (chatKey != null) {
                    String[] ids = chatKey.split("_");
                    if (ids.length == 2) {
                        receiptUid = ids[0].equals(myUid) ? ids[1] : ids[0];
                    }
                }
            }

            if (receiptUid != null && !receiptUid.isEmpty()) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiptUid", receiptUid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterChats(this, filterList);
        }
        return filter;
    }

    static class HolderChats extends RecyclerView.ViewHolder {
        ShapeableImageView profileIv;
        TextView nameTv, lastMessageTv, dateTimeTv;

        public HolderChats(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            dateTimeTv = itemView.findViewById(R.id.dateTimeTv);
        }
    }
}
