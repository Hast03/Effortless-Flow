package com.example.effortlessflow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    public static final long Splash_Screen_DisplayLen = 3000;
    public static final long Logo_Animation_Duration = 800;
    public static final long App_Name_Animation_Duration = 600;
    public static final long App_Tag_Animation_Duration = 200;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        mAuth = FirebaseAuth.getInstance();
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView appTag = findViewById(R.id.splash_app_tag);
        logo.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                .setDuration(Logo_Animation_Duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        appName.animate().translationY(0f).alpha(1.0f)
                .setDuration(App_Name_Animation_Duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(Logo_Animation_Duration)
                .start();

        appTag.animate().translationY(0f).alpha(1.0f)
                .setDuration(App_Tag_Animation_Duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(Logo_Animation_Duration + App_Name_Animation_Duration)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            Intent intent;

            if (currentUser != null) {
                if (currentUser.isEmailVerified()) {
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, Login.class);
                    mAuth.signOut();
                }
            } else {
                intent = new Intent(SplashScreen.this, Registration.class);
            }

            startActivity(intent);
            finish();
        }, Splash_Screen_DisplayLen);
    }
}
