package com.example.trado.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.trado.R;
import com.example.trado.models.ModelChats;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.HolderMessage> {

    private Context context;
    private ArrayList<ModelChats> messageList;
    private String myUid;

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    public AdapterMessage(Context context, ArrayList<ModelChats> messageList, String myUid) {
        this.context = context;
        this.messageList = messageList;
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public HolderMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
        }
        return new HolderMessage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMessage holder, int position) {
        ModelChats model = messageList.get(position);
        String type = model.getMessageType();
        String msg = model.getMessage();

        if (type.equalsIgnoreCase("TEXT")) {
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(msg);
        } else if (type.equalsIgnoreCase("IMAGE")) {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(msg)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.messageIv);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getFromUid().equals(myUid)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderMessage extends RecyclerView.ViewHolder {
        TextView messageTv;
        ImageView messageIv;
        LinearLayout messageLayout;

        HolderMessage(@NonNull View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.messageTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
