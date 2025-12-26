package com.example.effortlessflow.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    private static FirebaseFirestore db;
    private static FirebaseAuth auth;

    public static FirebaseFirestore getFirestore() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static String getCurrentUserId() {
        FirebaseAuth auth = getAuth();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public static boolean isUserLoggedIn() {
        return getCurrentUserId() != null;
    }
}