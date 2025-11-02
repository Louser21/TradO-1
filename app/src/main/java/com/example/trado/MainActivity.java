package com.example.trado;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.trado.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String TAG = "MAIN_TAG";
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // If user not logged in, go to login options
        if (currentUser == null) {
            startLoginOptions();
            finish(); // optional: prevent returning to MainActivity without login
            return;
        }else{
            updateFCMToken();
            askNotificationPermission();
        }


        showHomeFragment();

        binding.bottomNv.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if(itemId == R.id.menu_home) showHomeFragment();
            else if(itemId == R.id.menu_chats) showChatsFragment();
            else if(itemId == R.id.menu_my_ads) showMyAdsFragment();
            else if(itemId == R.id.menu_account) showAccountFragment();
            else return false;
            return true;
        });

        binding.sellFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddAdActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showHomeFragment() {
        binding.toolbarTitleTv.setText("Home");
        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentsFl.getId(), new HomeFragment(), "HomeFragment")
                .commit();
    }

    private void showChatsFragment() {
        binding.toolbarTitleTv.setText("Chats");
        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentsFl.getId(), new ChatsFragment(), "ChatsFragment")
                .commit();
    }

    private void showMyAdsFragment() {
        binding.toolbarTitleTv.setText("My Ads");
        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentsFl.getId(), new MyAdsFragment(), "MyAdsFragment")
                .commit();
    }

    private void showAccountFragment() {
        binding.toolbarTitleTv.setText("Account");
        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentsFl.getId(), new AccountFragment(), "AccountFragment")
                .commit();
    }

    private void startLoginOptions() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }

    private void updateFCMToken() {
        String myUid = firebaseAuth.getUid();
        Log.d(TAG, "My UID: " + myUid);

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "Token: " + token);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("fcmToken", token);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(myUid).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: Token Updated...!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "onFailure: ",e );
                                }
                            });
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                         Log.e(TAG, "Failed to get token: " + e.getMessage());
                    }
                });
    }

    private void askNotificationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED){
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS); 
            }
        }
    }

    private ActivityResultLauncher<String> requestNotificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean o) {
                    Log.d(TAG, "onActivityResult: Notification Permission STATUS: "+o);
                }
            }
    );
}
