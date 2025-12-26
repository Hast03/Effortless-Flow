package com.example.effortlessflow;

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

import com.example.effortlessflow.databinding.LoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private LoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        setupTextWatchers();
        setupClickListeners();
        setupPasswordVisibilityToggle();
    }

    private void setupPasswordVisibilityToggle() {
        binding.loginPwdEt.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.loginPwdEt.getRight() - binding.loginPwdEt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    togglePasswordVisibility(binding.loginPwdEt);
                    return true;
                }
            }
            return false;
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
        binding.loginEmailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        binding.loginPwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupClickListeners() {
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFormValid()) {
                    loginUser();
                }
            }
        });
        binding.registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isFormValid() {
        boolean isEmailValid = validateEmail(binding.loginEmailEt.getText().toString().trim());
        boolean isPasswordValid = validatePassword(binding.loginPwdEt.getText().toString().trim());
        return isEmailValid && isPasswordValid;
    }

    private boolean validateEmail(String email) {
        RelativeLayout emailLayout = binding.eLoginLayout;
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
        RelativeLayout passwordLayout = binding.pwdLoginLayout;
        TextView errorText = passwordLayout.findViewById(R.id.password_error_text);

        if (password.isEmpty()) {
            errorText.setText("Password is required");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        errorText.setVisibility(View.GONE);
        return true;
    }

    private void loginUser() {
        String email = binding.loginEmailEt.getText().toString().trim();
        String password = binding.loginPwdEt.getText().toString().trim();
        binding.loginBtn.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                binding.loginBtn.setEnabled(true);
                            }
                        }
                        else {
                            Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            binding.loginBtn.setEnabled(true);
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
