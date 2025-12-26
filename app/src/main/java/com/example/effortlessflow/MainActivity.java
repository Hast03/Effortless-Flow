package com.example.effortlessflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.effortlessflow.databinding.ActivityMainBinding;
import com.example.effortlessflow.utils.TaskNotificationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private TaskNotificationManager notificationManager;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check authentication
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // TODO: If needed, redirect to Login screen here
            // startActivity(new Intent(this, Login.class));
            // finish();
            // return;
        }

        setupNavigation();
        setupNotifications();
        requestNotificationPermission();
    }

    private void setupNavigation() {
        try {
            BottomNavigationView navView = binding.navView;

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_task_list,
                    R.id.navigation_insights,
                    R.id.navigation_settings
            ).build();

            // Get NavController using NavHostFragment approach (more reliable)
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_activity_main);

            if (navHostFragment == null) {
                Log.e("MainActivity", "NavHostFragment not found!");
                return;
            }

            NavController navController = navHostFragment.getNavController();

            // Link ActionBar + BottomNavigationView with NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);

        } catch (Exception e) {
            Log.e("MainActivity", "Navigation setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNotifications() {
        notificationManager = new TaskNotificationManager(this);
        notificationManager.scheduleOverdueCheck();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupNotifications();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_activity_main);

            if (navHostFragment == null) {
                return super.onSupportNavigateUp();
            }

            NavController navController = navHostFragment.getNavController();
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_task_list,
                    R.id.navigation_insights,
                    R.id.navigation_settings
            ).build();

            return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
        } catch (Exception e) {
            Log.e("MainActivity", "Navigation up failed: " + e.getMessage());
            return super.onSupportNavigateUp();
        }
    }
}