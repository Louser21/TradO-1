package com.example.trado;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trado.adapters.AdapterChat;
import com.example.trado.databinding.FragmentChatsBinding;
import com.example.trado.models.ModelChats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;

public class ChatsFragment extends Fragment {
    private FragmentChatsBinding binding;
    private static final String TAG = "CHATS_TAG";
    private FirebaseAuth firebaseAuth;
    private String myUid;
    private Context mContext;
    private ArrayList<ModelChats> chatsArrayList;
    private AdapterChat adapterChats;

    public ChatsFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        loadChats();

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterChats.getFilter().filter(s.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Search error: ", e);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadChats() {
        chatsArrayList = new ArrayList<>();
        adapterChats = new AdapterChat(mContext, chatsArrayList, myUid);
        binding.chatsRv.setAdapter(adapterChats);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String chatKey = ds.getKey();
                    if (chatKey != null && chatKey.contains(myUid)) {
                        ModelChats modelChats = new ModelChats();
                        modelChats.setChatKey(chatKey);

                        long lastTime = 0;
                        String lastMsg = "";
                        String otherUid = chatKey.replace(myUid, "").replace("_", "");

                        for (DataSnapshot msgSnap : ds.getChildren()) {
                            Long ts = msgSnap.child("timestamp").getValue(Long.class);
                            String msg = msgSnap.child("message").getValue(String.class);
                            if (ts != null && ts > lastTime) {
                                lastTime = ts;
                                lastMsg = msg != null ? msg : "";
                            }
                        }

                        modelChats.setTimestamp(lastTime);
                        modelChats.setLastMessage(lastMsg);
                        modelChats.setToUid(otherUid);

                        // fetch other user details async
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUid);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnap) {
                                String name = userSnap.child("name").getValue(String.class);
                                String profile = userSnap.child("profileImage").getValue(String.class);

                                modelChats.setName(name != null ? name : "Unknown");
                                modelChats.setProfileImageUrl(profile != null ? profile : "");
                                chatsArrayList.add(modelChats);
                                sortChats();
                                adapterChats.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void sortChats() {
        new Handler().postDelayed(() -> {
            Collections.sort(chatsArrayList,
                    (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            adapterChats.notifyDataSetChanged();
        }, 1000);
    }
}
