package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextView tvError;
    private Button btnSignUp, btnLogin;
    private FirebaseAuthHelper authHelper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authHelper = new FirebaseAuthHelper();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvError = findViewById(R.id.tvError);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> attemptSignUp());
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignUp() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Disable button during signup
        btnSignUp.setEnabled(false);
        tvError.setVisibility(View.GONE);

        // Check if username already exists
        authHelper.checkUsernameExists(username, task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                btnSignUp.setEnabled(true);
                showError("Username already exists");
                return;
            }

            // Check if email already exists
            authHelper.checkEmailExists(email, emailTask -> {
                if (emailTask.isSuccessful() && emailTask.getResult() != null && !emailTask.getResult().isEmpty()) {
                    btnSignUp.setEnabled(true);
                    showError("Email already registered");
                    return;
                }

                // Create account with Firebase
                authHelper.signUp(email, password, username, new FirebaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(String userId, String username) {
                        SessionManager sessionManager = new SessionManager(SignUpActivity.this);
                        sessionManager.createLoginSession(userId, username);
                        
                        Toast.makeText(SignUpActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        btnSignUp.setEnabled(true);
                        showError(errorMessage);
                    }
                });
            });
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
