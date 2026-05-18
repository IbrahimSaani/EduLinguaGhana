package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRole;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.core.content.ContextCompat;

public class SignUpActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9002;

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private android.widget.RadioGroup rgGender;
    private MaterialButton btnSignUp, btnGoogleSignUp, btnFacebookSignUp;
    private TextView tvLogin, tvSkip;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

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
        android.view.View formCard = findViewById(R.id.signupFormCard);
        if (formCard != null) {
            android.view.animation.Animation cardAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
            cardAnim.setStartOffset(300);
            formCard.startAnimation(cardAnim);
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgGender = findViewById(R.id.rgGender);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        btnFacebookSignUp = findViewById(R.id.btnFacebookSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        tvSkip = findViewById(R.id.tvSkip);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> signUpWithEmail());
        btnGoogleSignUp.setOnClickListener(v -> signUpWithGoogle());
        btnFacebookSignUp.setOnClickListener(v -> signUpWithFacebook());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        if (tvSkip != null) {
            tvSkip.setOnClickListener(v -> navigateToMain());
        }
    }

    private void signUpWithEmail() {
         String name = etName.getText().toString().trim();
         String email = etEmail.getText().toString().trim();
         String password = etPassword.getText().toString().trim();
         String confirmPassword = etConfirmPassword.getText().toString().trim();

         int selectedGenderId = rgGender.getCheckedRadioButtonId();
         if (selectedGenderId == -1) {
             Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
             return;
         }
         String gender = selectedGenderId == R.id.rbMale ? "Male" : "Female";

         if (!validateInput(name, email, password, confirmPassword)) {
             return;
         }

         btnSignUp.setEnabled(false);
         if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);

         // Check if email already exists before attempting to create account
         checkEmailExists(email, exists -> {
             if (exists) {
                 btnSignUp.setEnabled(true);
                 if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                 etEmail.setError("This email is already registered");
                 etEmail.requestFocus();
                 Toast.makeText(SignUpActivity.this,
                         "Email address is already in use. Please use a different email or log in.",
                         Toast.LENGTH_LONG).show();
                 return;
             }

             // Email doesn't exist, proceed with account creation
             mAuth.createUserWithEmailAndPassword(email, password)
                     .addOnCompleteListener(this, task -> {
                         btnSignUp.setEnabled(true);
                         if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                         if (task.isSuccessful()) {
                             FirebaseUser user = mAuth.getCurrentUser();

                             // Update user profile with name
                             UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                     .setDisplayName(name)
                                     .build();

                             user.updateProfile(profileUpdates)
                                     .addOnCompleteListener(updateTask -> {
                                         if (updateTask.isSuccessful()) {
                                             // Send verification email and show mandatory verification dialog
                                             sendVerificationEmail(user, gender);
                                         }
                                     });
                         } else {
                             Toast.makeText(SignUpActivity.this,
                                     "Sign up failed: " + task.getException().getMessage(),
                                     Toast.LENGTH_LONG).show();
                         }
                     });
         });
     }

     private void sendVerificationEmail(FirebaseUser user, String gender) {
         if (user == null) return;

         user.sendEmailVerification()
                 .addOnCompleteListener(task -> {
                     if (task.isSuccessful()) {
                         showVerificationPendingDialog(user, gender);
                     } else {
                         Toast.makeText(SignUpActivity.this,
                                 "Failed to send verification email: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                 Toast.LENGTH_LONG).show();
                         // Still show the dialog so they can try to resend or check
                         showVerificationPendingDialog(user, gender);
                     }
                 });
     }

     private void showVerificationPendingDialog(FirebaseUser user, String gender) {
         StyledMenuHelper.showStyledConfirmationDialog(
                 SignUpActivity.this,
                 "📧",
                 "Verify Your Email",
                 "A verification link has been sent to " + user.getEmail() +
                         ".\n\nPlease check your inbox and verify your account to continue. This ensures your account is secure.",
                 "I've Verified",
                 "Resend Email",
                 () -> {
                     // Positive Action: Check if verified
                     if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);
                     user.reload().addOnCompleteListener(reloadTask -> {
                         if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                         if (user.isEmailVerified()) {
                             // Success! Now save to database and proceed
                             saveUserToDatabase(user, gender);
                             Toast.makeText(SignUpActivity.this, "Email verified! Welcome aboard.", Toast.LENGTH_SHORT).show();
                             navigateToMain();
                         } else {
                             Toast.makeText(SignUpActivity.this,
                                     "Still not verified. Please click the link in your email.",
                                     Toast.LENGTH_LONG).show();
                             // Show the dialog again
                             showVerificationPendingDialog(user, gender);
                         }
                     });
                 },
                 () -> {
                     // Negative Action: Resend
                     sendVerificationEmail(user, gender);
                 }
         );
     }

     /**
      * Check if an email address already exists in Firebase Authentication
      * Uses Firebase's fetchSignInMethodsForEmail() which returns sign-in methods for the email
      * If the returned list is empty, the email is available
      * If the returned list is not empty, the email is already registered
      */
     private void checkEmailExists(String email, EmailCheckCallback callback) {
         mAuth.fetchSignInMethodsForEmail(email)
                 .addOnCompleteListener(task -> {
                     if (task.isSuccessful()) {
                         // If providers list is empty, email doesn't exist
                         boolean emailExists = !task.getResult().getSignInMethods().isEmpty();
                         callback.onEmailCheckComplete(emailExists);
                     } else {
                         // If there's an error, assume email doesn't exist and allow signup
                         // User will get better error message from createUserWithEmailAndPassword
                         android.util.Log.e("SignUp", "Error checking email: " + 
                             (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                         callback.onEmailCheckComplete(false);
                     }
                 });
     }

     /**
      * Callback interface for email existence check
      */
     @FunctionalInterface
     private interface EmailCheckCallback {
         void onEmailCheckComplete(boolean emailExists);
     }

     private void signUpWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void signUpWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("public_profile")
        );

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(SignUpActivity.this, "Facebook sign up cancelled",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(SignUpActivity.this,
                                "Facebook sign up error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        handleSocialSignUp(user);
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Facebook authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
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
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        handleSocialSignUp(user);
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleSocialSignUp(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(this, "Could not load your account", Toast.LENGTH_SHORT).show();
            return;
        }
        saveUserToDatabase(user, "Not Specified");
        Toast.makeText(SignUpActivity.this,
                "Welcome " + user.getDisplayName() + "!",
                Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    /**
     * Save user profile to Firebase Realtime Database
     */
    private void saveUserToDatabase(FirebaseUser user, String gender) {
        if (user == null) return;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", user.getUid());
        userProfile.put("email", user.getEmail());
        userProfile.put("displayName", user.getDisplayName());
        userProfile.put("username", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
        userProfile.put("gender", gender);
        userProfile.put("lastLogin", System.currentTimeMillis());

        usersRef.child(user.getUid()).updateChildren(userProfile);
    }


    /**
     * Save user profile to Firebase Realtime Database with role
     */
    private void saveUserToDatabase(FirebaseUser user, UserRole role) {
        if (user == null) return;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", user.getUid());
        userProfile.put("email", user.getEmail());
        userProfile.put("displayName", user.getDisplayName());
        userProfile.put("username", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
        userProfile.put("age", "");
        userProfile.put("school", "");
        userProfile.put("studentClass", "");
        userProfile.put("role", role.name());
        userProfile.put("createdAt", System.currentTimeMillis());

        usersRef.child(user.getUid()).setValue(userProfile);

        // Also set role via RoleManager
        RoleManager roleManager = new RoleManager();
        roleManager.setUserRole(this, user.getUid(), role);
    }

    private void navigateToMain() {
        // Navigate to role selection for first-time users
        Intent intent = new Intent(SignUpActivity.this, RoleSelectionActivity.class);
        intent.putExtra("first_time", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
