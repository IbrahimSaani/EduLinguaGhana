package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRole;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CompleteProfileActivity extends AppCompatActivity {

    public static final String EXTRA_NEXT_STEP = "next_step";
    public static final String NEXT_STEP_ROLE_SELECTION = "role_selection";
    public static final String NEXT_STEP_MAIN = "main";
    public static final String NEXT_STEP_PROFILE = "profile";

    private TextInputEditText etAge;
    private android.widget.RadioGroup rgGender;
    private MaterialAutoCompleteTextView etStudentClass;
    private MaterialButton btnContinue;
    private android.widget.TextView tvDisplayName, tvEmail;
    private android.widget.TextView tvTitle, tvSubtitle;

    private FirebaseUser currentUser;
    private RoleManager roleManager;
    private String[] classOptions;
    private boolean editMode;
    private UserRole currentUserRole = UserRole.STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.complete_profile_missing_user, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        roleManager = new RoleManager();
        editMode = NEXT_STEP_PROFILE.equals(getIntent().getStringExtra(EXTRA_NEXT_STEP));
        classOptions = getResources().getStringArray(R.array.basic_class_options);
        initViews();
        configureModeUi();
        setupClassDropdown();
        bindUserInfo();
        loadCurrentRole();
        loadExistingProfileValues();
        setupListeners();
    }

    private void initViews() {
        etAge = findViewById(R.id.etAge);
        rgGender = findViewById(R.id.rgGender);
        etStudentClass = findViewById(R.id.etStudentClass);
        btnContinue = findViewById(R.id.btnContinue);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvEmail = findViewById(R.id.tvEmail);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
    }

    private void loadCurrentRole() {
        roleManager.getUserRole(this, currentUser.getUid(), new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                currentUserRole = role;
            }

            @Override
            public void onError(String error) {
                currentUserRole = UserRole.STUDENT;
            }
        });
    }

    private void configureModeUi() {
        if (currentUserRole != UserRole.STUDENT) {
            if (findViewById(R.id.tilAge) != null) findViewById(R.id.tilAge).setVisibility(android.view.View.GONE);
            if (findViewById(R.id.tilStudentClass) != null) findViewById(R.id.tilStudentClass).setVisibility(android.view.View.GONE);
            if (tvSubtitle != null) tvSubtitle.setText("Please confirm your details to continue");
        }

        if (tvTitle != null && editMode) {
            tvTitle.setText(R.string.complete_profile_edit_title);
        }
        if (tvSubtitle != null && editMode) {
            tvSubtitle.setText(R.string.complete_profile_edit_subtitle);
        }
        if (btnContinue != null && editMode) {
            btnContinue.setText(R.string.complete_profile_save_changes);
        }
    }

    private void setupClassDropdown() {
        if (etStudentClass == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                classOptions
        );
        etStudentClass.setAdapter(adapter);
        etStudentClass.setOnClickListener(v -> etStudentClass.showDropDown());
    }

    private void bindUserInfo() {
        String displayName = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        if (tvDisplayName != null) {
            tvDisplayName.setText(!TextUtils.isEmpty(displayName) ? displayName : "Learner");
        }
        if (tvEmail != null) {
            tvEmail.setText(!TextUtils.isEmpty(email) ? email : "");
        }
    }

    private void loadExistingProfileValues() {
        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String age = snapshot.child("age").getValue(String.class);
                            String studentClass = snapshot.child("studentClass").getValue(String.class);
                            String gender = snapshot.child("gender").getValue(String.class);

                            if (etAge != null && !TextUtils.isEmpty(age)) {
                                etAge.setText(age);
                            }
                            if (etStudentClass != null && !TextUtils.isEmpty(studentClass)) {
                                etStudentClass.setText(studentClass, false);
                            }
                            if (rgGender != null && !TextUtils.isEmpty(gender)) {
                                if (gender.equalsIgnoreCase("Male")) {
                                    rgGender.check(R.id.rbMale);
                                } else if (gender.equalsIgnoreCase("Female")) {
                                    rgGender.check(R.id.rbFemale);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(CompleteProfileActivity.this,
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> saveProfileAndContinue());
        }
    }

    private void saveProfileAndContinue() {
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }
        String gender = selectedGenderId == R.id.rbMale ? "Male" : "Female";

        if (currentUserRole != UserRole.STUDENT) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            usersRef.child("gender").setValue(gender).addOnCompleteListener(task -> routeAfterCompletion());
            return;
        }
        
        String age = safeText(etAge);
        String studentClass = safeText(etStudentClass);

        if (TextUtils.isEmpty(age)) {
            if (etAge != null) etAge.setError(getString(R.string.complete_profile_required_age));
            if (etAge != null) etAge.requestFocus();
            return;
        }

        int parsedAge;
        try {
            parsedAge = Integer.parseInt(age);
            if (parsedAge <= 0 || parsedAge > 25) {
                if (etAge != null) etAge.setError(getString(R.string.complete_profile_required_age));
                if (etAge != null) etAge.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            if (etAge != null) etAge.setError(getString(R.string.complete_profile_required_age));
            if (etAge != null) etAge.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(studentClass)) {
            if (etStudentClass != null) etStudentClass.setError(getString(R.string.complete_profile_required_class));
            if (etStudentClass != null) etStudentClass.requestFocus();
            return;
        }

        btnContinue.setEnabled(false);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("uid", currentUser.getUid());
        updates.put("email", currentUser.getEmail());
        updates.put("displayName", currentUser.getDisplayName());
        updates.put("username", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());
        updates.put("gender", gender);
        updates.put("age", age);
        updates.put("school", "");
        updates.put("studentClass", studentClass);
        updates.put("lastLogin", System.currentTimeMillis());

        usersRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    btnContinue.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.complete_profile_saved, Toast.LENGTH_SHORT).show();
                        routeAfterCompletion();
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this,
                                getString(R.string.complete_profile_save_failed, message),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void routeAfterCompletion() {
        String nextStep = getIntent().getStringExtra(EXTRA_NEXT_STEP);
        if (NEXT_STEP_PROFILE.equals(nextStep)) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        if (NEXT_STEP_ROLE_SELECTION.equals(nextStep)) {
            Intent intent = new Intent(this, RoleSelectionActivity.class);
            intent.putExtra("first_time", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        if (NEXT_STEP_MAIN.equals(nextStep)) {
            restoreUserProgress(currentUser);
        }

        roleManager.getUserRole(this, currentUser.getUid(), new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                Intent intent = new Intent(CompleteProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Intent intent = new Intent(CompleteProfileActivity.this, RoleSelectionActivity.class);
                intent.putExtra("first_time", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void restoreUserProgress(FirebaseUser user) {
        if (user == null) return;
        String userId = user.getUid();

        FirebaseDatabase.getInstance().getReference("aggregates").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        com.edulinguaghana.tracking.ProgressAggregate aggregate = snapshot.getValue(com.edulinguaghana.tracking.ProgressAggregate.class);
                        if (aggregate != null) {
                            com.edulinguaghana.gamification.XPState xpState = new com.edulinguaghana.gamification.XPState();
                            xpState.totalXp = aggregate.getTotalXP();
                            xpState.level = aggregate.getCurrentLevel();
                            xpState.lastUpdated = aggregate.getLastUpdated();
                            com.edulinguaghana.gamification.XPManager.saveState(CompleteProfileActivity.this, xpState);

                            com.edulinguaghana.ProgressManager.saveAllProgress(CompleteProfileActivity.this,
                                    aggregate.getHighestScore(),
                                    aggregate.getTotalQuizzes(),
                                    aggregate.getTotalCorrectAnswers(),
                                    aggregate.getTotalQuestions());

                            com.edulinguaghana.StreakManager streakManager = new com.edulinguaghana.StreakManager(CompleteProfileActivity.this);
                            streakManager.saveAllStreakData(aggregate.getCurrentStreak(),
                                    aggregate.getLongestStreak(),
                                    aggregate.getDaysActive());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });

        FirebaseDatabase.getInstance().getReference("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.hasChild("badges")) {
                            java.util.List<com.edulinguaghana.gamification.Badge> badges = new java.util.ArrayList<>();
                            for (DataSnapshot badgeSnap : snapshot.child("badges").getChildren()) {
                                badges.add(badgeSnap.getValue(com.edulinguaghana.gamification.Badge.class));
                            }
                            if (!badges.isEmpty()) {
                                com.edulinguaghana.gamification.BadgeManager.saveBadges(CompleteProfileActivity.this, badges);
                            }
                        }

                        if (snapshot.hasChild("quests")) {
                            java.util.List<com.edulinguaghana.gamification.Quest> quests = new java.util.ArrayList<>();
                            for (DataSnapshot questSnap : snapshot.child("quests").getChildren()) {
                                quests.add(questSnap.getValue(com.edulinguaghana.gamification.Quest.class));
                            }
                            if (!quests.isEmpty()) {
                                com.edulinguaghana.gamification.QuestManager.saveQuests(CompleteProfileActivity.this, quests);
                            }
                        }

                        if (snapshot.hasChild("achievements")) {
                            java.util.List<com.edulinguaghana.Achievement> achievements = new java.util.ArrayList<>();
                            for (DataSnapshot achSnap : snapshot.child("achievements").getChildren()) {
                                achievements.add(achSnap.getValue(com.edulinguaghana.Achievement.class));
                            }
                            if (!achievements.isEmpty()) {
                                com.edulinguaghana.AchievementManager achievementManager = new com.edulinguaghana.AchievementManager(CompleteProfileActivity.this);
                                achievementManager.saveAllAchievements(achievements);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private String safeText(android.widget.TextView view) {
        return view != null && view.getText() != null ? view.getText().toString().trim() : "";
    }

}

