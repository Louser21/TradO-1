package com.example.trado;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.trado.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
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
        }

        // Network check (off main thread)
        new Thread(() -> {
            try {
                URL url = new URL("https://www.google.com");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    runOnUiThread(() -> Toast.makeText(this, "Internet working ✅", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "No internet ❌", Toast.LENGTH_SHORT).show());
            }
        }).start();

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
}
