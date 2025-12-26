//package com.example.effortlessflow;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.effortlessflow.databinding.RegistrationBinding;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//public class Registration extends AppCompatActivity {
//
//    private RegistrationBinding binding;
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = RegistrationBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//        mAuth = FirebaseAuth.getInstance();
//        setupTextWatchers();
//        setupClickListeners();
//    }
//    private void setupTextWatchers() {
//        binding.registrationEmailEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                validateEmail(s.toString());
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//        binding.registrationPwdEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                validatePassword(s.toString());
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//        binding.registrationConfPwdEt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                validateConfirmPassword(s.toString());
//            }
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//    }
//    private void setupClickListeners() {
//        binding.registrationBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isFormValid()) {
//                    registerUser();
//                }
//            }
//        });
//        binding.loginLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Registration.this, Login.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//    }
//    private boolean isFormValid() {
//        boolean isEmailValid = validateEmail(binding.registrationEmailEt.getText().toString().trim());
//        boolean isPasswordValid = validatePassword(binding.registrationPwdEt.getText().toString().trim());
//        boolean isConfirmPasswordValid = validateConfirmPassword(binding.registrationConfPwdEt.getText().toString().trim());
//        return isEmailValid && isPasswordValid && isConfirmPasswordValid;
//    }
//    private boolean validateEmail(String email) {
//        if (email.isEmpty()) {
//            binding.eRegistrationLayout.setError("Email is required");
//            return false;
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.eRegistrationLayout.setError("Please enter a valid email");
//            return false;
//        }
//        binding.eRegistrationLayout.setError(null);
//        return true;
//    }
//    private boolean validatePassword(String password) {
//        if (password.isEmpty()) {
//            binding.pwdRegistrationLayout.setError("Password is required");
//            return false;
//        }
//        if (password.length() < 8) {
//            binding.pwdRegistrationLayout.setError("Password must be at least 8 characters");
//            return false;
//        }
//        if(!password.matches(".*[A-Z].*")) {
//            binding.pwdRegistrationLayout.setError("Password must contain at least one uppercase letter");
//            return false;
//        }
//        if(!password.matches(".*[a-z].*")) {
//            binding.pwdRegistrationLayout.setError("Password must contain at least one lowercase letter");
//            return false;
//        }
//        if(!password.matches(".*[@#$%^&+=].*")) {
//            binding.pwdRegistrationLayout.setError("Password must contain at least one special character");
//            return false;
//        }
//        binding.pwdRegistrationLayout.setError(null);
//        return true;
//    }
//    private boolean validateConfirmPassword(String confirmPassword) {
//        String password = binding.registrationPwdEt.getText().toString().trim();
//        if (confirmPassword.isEmpty()) {
//            binding.confPwdRegistrationLayout.setError("Confirm password is required");
//            return false;
//        }
//        if (!password.equals(confirmPassword)) {
//            binding.confPwdRegistrationLayout.setError("Passwords do not match");
//            return false;
//        }
//        binding.confPwdRegistrationLayout.setError(null);
//        return true;
//    }
//    private void registerUser() {
//        String email = binding.registrationEmailEt.getText().toString().trim();
//        String password = binding.registrationPwdEt.getText().toString().trim();
//
//        binding.registrationBtn.setEnabled(false);
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> emailTask) {
//                                    if(emailTask.isSuccessful()) {
//                                        Toast.makeText(Registration.this, "Registration successful. Verification email sent to " + user.getEmail() + ". Please verify your email to log in.", Toast.LENGTH_LONG).show();
//                                        Intent intent = new Intent(Registration.this, Login.class);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                    else {
//                                        Toast.makeText(Registration.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_LONG).show();
//                                        binding.registrationBtn.setEnabled(true);
//                                    }
//                                }
//                            });
//                        }
//                        else {
//                            Toast.makeText(Registration.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                            binding.registrationBtn.setEnabled(true);
//                        }
//                    }
//                });
//    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        binding = null;
//    }
//}





package com.example.effortlessflow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.effortlessflow.databinding.RegistrationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registration extends AppCompatActivity {

    private RegistrationBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = RegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        setupTextWatchers();
        setupClickListeners();
        setupPasswordVisibilityToggles();
    }

    @SuppressLint("ClickableViewAccessibility")
//    private void setupPasswordVisibilityToggles() {
//        // Password field toggle
//        binding.registrationPwdEt.setOnTouchListener((v, event) -> {
//            final int DRAWABLE_RIGHT = 2;
//            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
//                if (event.getRawX() >= (binding.registrationPwdEt.getRight() - binding.registrationPwdEt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                    v.performClick();
//                    togglePasswordVisibility(binding.registrationPwdEt);
//                    return true;
//                }
//            }
//            return false;
//        });
//
//        // Confirm password field toggle
//        binding.registrationConfPwdEt.setOnTouchListener((v, event) -> {
//            final int DRAWABLE_RIGHT = 2;
//            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
//                if (event.getRawX() >= (binding.registrationConfPwdEt.getRight() - binding.registrationConfPwdEt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                    v.performClick();
//                    togglePasswordVisibility(binding.registrationConfPwdEt);
//                    return true;
//                }
//            }
//            return false;
//        });
//    }

    private void setupPasswordVisibilityToggles() {
        // Password field toggle
        binding.registrationPwdEt.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.registrationPwdEt.getRight() - binding.registrationPwdEt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    v.performClick();
                    togglePasswordVisibility(binding.registrationPwdEt);
                    return true;
                }
            }
            return false;
        });

        // Add proper click handling for accessibility
        binding.registrationPwdEt.setOnClickListener(v -> {
            // This handles click events from accessibility services
            togglePasswordVisibility(binding.registrationPwdEt);
        });

        // Confirm password field toggle
        binding.registrationConfPwdEt.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.registrationConfPwdEt.getRight() - binding.registrationConfPwdEt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    v.performClick();
                    togglePasswordVisibility(binding.registrationConfPwdEt);
                    return true;
                }
            }
            return false;
        });

        // Add proper click handling for accessibility
        binding.registrationConfPwdEt.setOnClickListener(v -> {
            // This handles click events from accessibility services
            togglePasswordVisibility(binding.registrationConfPwdEt);
        });
    }

    private void togglePasswordVisibility(EditText editText) {
        int selection = editText.getSelectionEnd();
        if (editText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_visibility_off, 0);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_visibility, 0);
        }
        editText.setSelection(selection);
    }

    private void setupTextWatchers() {
        binding.registrationEmailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.registrationPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        binding.registrationConfPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        binding.registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFormValid()) {
                    registerUser();
                }
            }
        });
        binding.loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isFormValid() {
        boolean isEmailValid = validateEmail(binding.registrationEmailEt.getText().toString().trim());
        boolean isPasswordValid = validatePassword(binding.registrationPwdEt.getText().toString().trim());
        boolean isConfirmPasswordValid = validateConfirmPassword(binding.registrationConfPwdEt.getText().toString().trim());
        return isEmailValid && isPasswordValid && isConfirmPasswordValid;
    }

    private boolean validateEmail(String email) {
        RelativeLayout emailLayout = binding.eRegistrationLayout;
        TextView errorText = emailLayout.findViewById(R.id.email_error_text);

        if (email.isEmpty()) {
            errorText.setText("Email is required");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorText.setText("Please enter a valid email");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        errorText.setVisibility(View.GONE);
        return true;
    }

    private boolean validatePassword(String password) {
        RelativeLayout passwordLayout = binding.pwdRegistrationLayout;
        TextView errorText = passwordLayout.findViewById(R.id.password_error_text);

        if (password.isEmpty()) {
            errorText.setText("Password is required");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if (password.length() < 8) {
            errorText.setText("Password must be at least 8 characters");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if(!password.matches(".*[A-Z].*")) {
            errorText.setText("Password must contain at least one uppercase letter");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if(!password.matches(".*[a-z].*")) {
            errorText.setText("Password must contain at least one lowercase letter");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if(!password.matches(".*[@#$%^&+=].*")) {
            errorText.setText("Password must contain at least one special character");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        errorText.setVisibility(View.GONE);
        return true;
    }

    private boolean validateConfirmPassword(String confirmPassword) {
        RelativeLayout confirmPasswordLayout = binding.confPwdRegistrationLayout;
        TextView errorText = confirmPasswordLayout.findViewById(R.id.confirm_password_error_text);
        String password = binding.registrationPwdEt.getText().toString().trim();

        if (confirmPassword.isEmpty()) {
            errorText.setText("Confirm password is required");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        if (!password.equals(confirmPassword)) {
            errorText.setText("Passwords do not match");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        errorText.setVisibility(View.GONE);
        return true;
    }

    private void registerUser() {
        String email = binding.registrationEmailEt.getText().toString().trim();
        String password = binding.registrationPwdEt.getText().toString().trim();

        binding.registrationBtn.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> emailTask) {
                                    if(emailTask.isSuccessful()) {
                                        Toast.makeText(Registration.this, "Registration successful. Verification email sent to " + user.getEmail() + ". Please verify your email to log in.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Registration.this, Login.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(Registration.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        binding.registrationBtn.setEnabled(true);
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(Registration.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            binding.registrationBtn.setEnabled(true);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}