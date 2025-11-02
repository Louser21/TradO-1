package com.example.trado;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.PopupMenu;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.trado.adapters.AdapterChat;
import com.example.trado.adapters.AdapterMessage;
import com.example.trado.databinding.ActivityChatBinding;
import com.example.trado.models.ModelChats;
import com.example.trado.utils.FcmV1Sender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "CHAT_TAG";
    private ActivityChatBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String receiptUid = "", myUid = "", chatPath = "", myName = "", receiptFcmToken = "";
    private Uri imageUri = null;
    private AdapterMessage adapterMessage;
    private ArrayList<ModelChats> messageList;


    private ActivityResultLauncher<String[]> requestCameraPermissions;
    private ActivityResultLauncher<String> requestStoragePermission;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("ChatDebug", "receiptUid=" + receiptUid + ", myUid=" + myUid);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        receiptUid = getIntent().getStringExtra("receiptUid");
        myUid = firebaseAuth.getUid();
        chatPath = Utils.chatPath(receiptUid, myUid);

        setupActivityResultLaunchers();
        loadMyInfo();
        loadReceiptDetails();
        loadMessages();

        binding.toolbarBackBtn.setOnClickListener(v -> finish());
        binding.sendBtn.setOnClickListener(v -> validateAndSendText());
        binding.attachFab.setOnClickListener(v -> imagePickDialog());
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                myName = "" + snapshot.child("name").getValue();
                Log.d(TAG, "My name: " + myName);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadReceiptDetails() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(receiptUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                receiptFcmToken = "" + snapshot.child("fcmToken").getValue();

                binding.toolbarTitleTv.setText(name);
                Glide.with(ChatActivity.this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_person_grey)
                        .into(binding.toolbarProfileIv);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadMessages() {
        messageList = new ArrayList<>();
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatPath);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChats chat = ds.getValue(ModelChats.class);
                    messageList.add(chat);
                }
                adapterMessage = new AdapterMessage(ChatActivity.this, messageList, myUid);
                binding.chatRv.setAdapter(adapterMessage);
                binding.chatRv.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void validateAndSendText() {
        String msg = binding.messageEt.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            Utils.toast(this, "Enter message to send...");
        } else {
            long timestamp = Utils.getTimestamp();
            sendMessage(Utils.MESSAGE_TYPE_TEXT, msg, timestamp);
            binding.messageEt.setText("");
        }
    }

    private void sendMessage(String messageType, String message, long timestamp) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatPath);
        String messageId = chatRef.push().getKey();
        ModelChats modelChat = new ModelChats(messageId, messageType, message, myUid, receiptUid, timestamp);

        chatRef.child(messageId).setValue(modelChat)
                .addOnSuccessListener(unused -> {
                    prepareNotification(message);
                })
                .addOnFailureListener(e -> Utils.toast(ChatActivity.this, "Failed: " + e.getMessage()));
    }

    private void prepareNotification(String message) {
        try {
            JSONObject notificationJo = new JSONObject();
            JSONObject messageObj = new JSONObject();
            JSONObject notifJo = new JSONObject();
            JSONObject dataJo = new JSONObject();

            notifJo.put("title", myName);
            notifJo.put("body", message);
            notifJo.put("sound", "default");

            dataJo.put("notificationType", Utils.NOTIFICATION_TYPE_NEW_MESSAGE);
            dataJo.put("senderUid", myUid);

            messageObj.put("token", receiptFcmToken);
            messageObj.put("notification", notifJo);
            messageObj.put("data", dataJo);

            notificationJo.put("message", messageObj);

            FcmV1Sender.send(notificationJo);
        } catch (Exception e) {
            Log.e(TAG, "prepareNotification:", e);
        }
    }


    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.attachFab);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                } else {
                    requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                }
            } else if (id == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery();
                } else {
                    requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            return true;
        });
    }

    private void setupActivityResultLaunchers() {
        requestCameraPermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = true;
                    for (Boolean b : result.values()) granted &= b;
                    if (granted) pickImageCamera();
                    else Utils.toast(this, "Camera or Storage permission denied!");
                });

        requestStoragePermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) pickImageGallery();
                    else Utils.toast(this, "Permission denied!");
                });

        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        uploadToFirebaseStorage();
                    } else {
                        Utils.toast(this, "Cancelled!");
                    }
                });

        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        uploadToFirebaseStorage();
                    } else {
                        Utils.toast(this, "Cancelled!");
                    }
                });
    }

    private void pickImageCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "ChatImage");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Chat Image (Camera)");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void uploadToFirebaseStorage() {
        if (imageUri == null) return;
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        String fileName = "ChatImages/" + timestamp;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(fileName);
        storageRef.putFile(imageUri)
                .addOnProgressListener(snapshot -> {
                    double progress = 100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading: " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    sendMessage(Utils.MESSAGE_TYPE_IMAGE, uri.toString(), timestamp);
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(this, "Upload failed: " + e.getMessage());
                });
    }
}
