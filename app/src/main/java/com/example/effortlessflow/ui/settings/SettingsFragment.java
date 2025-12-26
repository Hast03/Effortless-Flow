package com.example.effortlessflow.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.effortlessflow.Login;
import com.example.effortlessflow.R;
import com.example.effortlessflow.Registration;
import com.example.effortlessflow.utils.PreferencesManager;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SettingsFragment extends Fragment {

    private MaterialSwitch darkModeSwitch;
    private TextView userNameText, userEmailText, versionText;
    private View notificationsSetting, emailVerificationSetting, sendFeedbackSetting, logoutSetting, deleteAccountSetting;
    private PreferencesManager prefsManager;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize preferences manager
        prefsManager = PreferencesManager.getInstance(requireContext());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(root);
        setupClickListeners();
        loadUserData();
        loadSettings();

        return root;
    }

    private void initViews(View root) {
        darkModeSwitch = root.findViewById(R.id.dark_mode_switch);
        userNameText = root.findViewById(R.id.user_name);
        userEmailText = root.findViewById(R.id.user_email);
        versionText = root.findViewById(R.id.version_value);
        notificationsSetting = root.findViewById(R.id.setting_notifications);
        emailVerificationSetting = root.findViewById(R.id.setting_email_verification);
        sendFeedbackSetting = root.findViewById(R.id.setting_send_feedback_clickable);
        logoutSetting = root.findViewById(R.id.setting_logout);
        deleteAccountSetting = root.findViewById(R.id.setting_delete_account);

        // Prevent null text issues
        darkModeSwitch.setText("");
        darkModeSwitch.setTextOn("");
        darkModeSwitch.setTextOff("");
    }

    private void setupClickListeners() {
        // Dark Mode Switch
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.setDarkMode(isChecked);
            applyDarkMode(isChecked);
            Toast.makeText(getContext(),
                    isChecked ? "Dark mode enabled" : "Dark mode disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Notifications Setting
        notificationsSetting.setOnClickListener(v -> {
            boolean currentState = prefsManager.areNotificationsEnabled();
            prefsManager.setNotificationsEnabled(!currentState);
            Toast.makeText(getContext(),
                    !currentState ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        emailVerificationSetting.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Email already verified", Toast.LENGTH_SHORT).show();
        });

        // Navigate to FeedbackFragment
        sendFeedbackSetting.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_settings_to_feedbackFragment);
        });

        // --- Logout ---
        logoutSetting.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        // Sign out from Firebase
                        mAuth.signOut();

                        // Clear all preferences
                        prefsManager.clearAll();

                        // Show success message
                        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to Login activity
                        Intent intent = new Intent(getActivity(), Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        // Finish current activity to prevent going back
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });

        // --- Delete Account ---
        deleteAccountSetting.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This will remove all your data from app's database permanently.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

//    private void deleteUserAccount() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user == null) return;
//
//        String userId = user.getUid();
//
//        // Step 1: Delete all tasks linked to this user
//        db.collection("tasks").whereEqualTo("userId", userId).get()
//                .addOnSuccessListener(querySnapshot -> {
//                    for (QueryDocumentSnapshot doc : querySnapshot) {
//                        doc.getReference().delete();
//                    }
//
//                    // Step 2: Delete user document from "users"
//                    db.collection("users").document(userId).delete()
//                            .addOnSuccessListener(aVoid -> {
//                                // Step 3: Delete FirebaseAuth user
//                                user.delete().addOnCompleteListener(task -> {
//                                    if (task.isSuccessful()) {
//                                        prefsManager.clearAll();
//                                        Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(getActivity(), Registration.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);
//                                    } else {
//                                        Toast.makeText(getContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            })
//                            .addOnFailureListener(e -> Toast.makeText(getContext(),
//                                    "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//                })
//                .addOnFailureListener(e -> Toast.makeText(getContext(),
//                        "Failed to delete tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        // Show progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_progress);
        AlertDialog progressDialog = builder.create();
        progressDialog.show();

        // Create a counter to track deletion progress
        final int[] deletionCount = {0};
        final int totalDeletions = 3; // Number of operations to complete

        // Function to check if all deletions are complete
        Runnable checkCompletion = new Runnable() {
            @Override
            public void run() {
                deletionCount[0]++;
                if (deletionCount[0] == totalDeletions) {
                    // All deletions complete, now delete the auth user
                    user.delete().addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            prefsManager.clearAll();
                            Toast.makeText(getContext(), "Account and all data deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), Registration.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), "Failed to delete authentication: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };

        // Step 1: Delete all tasks linked to this user
        db.collection("tasks").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    // Delete each task document
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    Toast.makeText(getContext(), "Deleted " + querySnapshot.size() + " tasks", Toast.LENGTH_SHORT).show();
                    checkCompletion.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkCompletion.run();
                });

        // Step 2: Delete user document from "users" collection
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User profile deleted", Toast.LENGTH_SHORT).show();
                    checkCompletion.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkCompletion.run();
                });

        // Step 3: Delete any subtasks or other related data
        db.collection("subtasks").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    // Delete each subtask document
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    Toast.makeText(getContext(), "Deleted " + querySnapshot.size() + " subtasks", Toast.LENGTH_SHORT).show();
                    checkCompletion.run();
                })
                .addOnFailureListener(e -> {
                    // If subtasks collection doesn't exist, continue
                    Toast.makeText(getContext(), "No subtasks found or failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    checkCompletion.run();
                });
    }

//    private void loadUserData() {
//        userNameText.setText(prefsManager.getUserName());
//        userEmailText.setText(prefsManager.getUserEmail());
//
//        try {
//            String versionName = requireContext().getPackageManager()
//                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
//            versionText.setText(versionName);
//        } catch (Exception e) {
//            versionText.setText("1.0.0"); // Fallback version
//        }
//    }

    private void loadUserData() {
        // Get current Firebase user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.getEmail() != null) {
            String email = currentUser.getEmail();

            // Extract username from email (text before @)
            String username = email.substring(0, email.indexOf("@"));

            // Set username and email in TextViews
            userNameText.setText(username);
            userEmailText.setText(email);

            // Also save to preferences for consistency
            prefsManager.setUserName(username);
            prefsManager.setUserEmail(email);
        } else {
            // Fallback to preferences if Firebase user is not available
            userNameText.setText(prefsManager.getUserName());
            userEmailText.setText(prefsManager.getUserEmail());
        }

        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            versionText.setText(versionName);
        } catch (Exception e) {
            versionText.setText("1.0.0"); // Fallback version
        }
    }

    private void loadSettings() {
        darkModeSwitch.setChecked(prefsManager.isDarkModeEnabled());
    }

    private void applyDarkMode(boolean enabled) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
