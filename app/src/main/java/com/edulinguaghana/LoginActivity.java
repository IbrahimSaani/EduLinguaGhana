package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn, btnFacebookSignIn;
    private TextView tvSignUp, tvForgotPassword, tvSkip;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook SDK
        mCallbackManager = CallbackManager.Factory.create();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initViews();
        setupListeners();
        animateViews();
    }

    private void animateViews() {
        // Animate logo
        android.view.View logo = findViewById(R.id.ivLogo);
        if (logo != null) {
            android.view.animation.Animation logoAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.logo_bounce_in);
            logo.startAnimation(logoAnim);
        }

        // Animate form card
        android.view.View formCard = findViewById(R.id.loginFormCard);
        if (formCard != null) {
            android.view.animation.Animation cardAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
            cardAnim.setStartOffset(300);
            formCard.startAnimation(cardAnim);
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnFacebookSignIn = findViewById(R.id.btnFacebookSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSkip = findViewById(R.id.tvSkip);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnFacebookSignIn.setOnClickListener(v -> signInWithFacebook());

        tvSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        tvForgotPassword.setOnClickListener(v -> resetPassword());

        tvSkip.setOnClickListener(v -> {
            // Skip login and go to MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }

    // Helper to safely get text from TextInputEditText
    private String safeText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }

    private void loginWithEmail() {
        String email = safeText(etEmail);
        String password = safeText(etPassword);

        if (!validateInput(email, password)) {
            return;
        }

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save user to database for friend lookups
                        saveUserToDatabase(user);
                        Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String message = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this, "Login failed: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("email", "public_profile")
        );

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Facebook login cancelled",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        String msg = (error != null && error.getMessage() != null) ? error.getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this,
                                "Facebook login error: " + msg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        if (token == null || token.getToken() == null) {
            Toast.makeText(this, "Invalid Facebook token", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save user to database for friend lookups
                        saveUserToDatabase(user);
                        String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Learner";
                        Toast.makeText(LoginActivity.this,
                                "Welcome " + name + "!",
                                Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String message = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this,
                                "Facebook authentication failed: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = safeText(etEmail);

        if (TextUtils.isEmpty(email)) {
            if (etEmail != null) {
                etEmail.setError("Enter your email address");
                etEmail.requestFocus();
            }
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etEmail != null) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
            }
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        String message = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this,
                                "Failed to send reset email: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            if (etEmail != null) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
            }
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etEmail != null) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
            }
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            if (etPassword != null) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
            }
            return false;
        }

        if (password.length() < 6) {
            if (etPassword != null) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
            }
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Handle Google Sign In result
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account != null ? account.getIdToken() : null);
            } catch (ApiException e) {
                String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Unknown error";
                Toast.makeText(this, "Google sign in failed: " + msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "Google sign in token missing", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save user to database for friend lookups
                        saveUserToDatabase(user);
                        String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Learner";
                        Toast.makeText(LoginActivity.this, "Welcome " + name + "!",
                                Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String message = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + message,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Save user profile to Firebase Realtime Database
     */
    private void saveUserToDatabase(FirebaseUser user) {
        if (user == null) return;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", user.getUid());
        userProfile.put("email", user.getEmail());
        userProfile.put("displayName", user.getDisplayName());
        userProfile.put("lastLogin", System.currentTimeMillis());

        usersRef.child(user.getUid()).updateChildren(userProfile);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }
}
