package com.example.uberapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DriverMainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.driverHome);
    }
    DriverHomeFragment driverHomeFragment = new DriverHomeFragment();
    DriverInboxFragment driverInboxFragment = new DriverInboxFragment();
    DriverHistoryFragment driverHistoryFragment = new DriverHistoryFragment();
    DriverAccountFragment driverAccountFragment = new DriverAccountFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.driverHome:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, driverHomeFragment).commit();
                return true;
            case R.id.driverInbox:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, driverInboxFragment).commit();
                return true;
            case R.id.driverHistory:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, driverHistoryFragment).commit();
                return true;
            case R.id.driverAccount:
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, driverAccountFragment).commit();
                return true;
        }
        return false;
    }
}