package com.edulinguaghana.social;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suggests friends based on common interests and learning patterns
 */
public class FriendSuggestionEngine {

    public interface SuggestionCallback {
        void onSuggestionsReady(List<UserSuggestion> suggestions);
        void onError(String error);
    }

    public static class UserSuggestion {
        public String userId;
        public String username;
        public String email;
        public int matchScore;
        public List<String> commonInterests;

        public UserSuggestion(String userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.matchScore = 0;
            this.commonInterests = new ArrayList<>();
        }
    }

    /**
     * Get friend suggestions for a user based on:
     * - Common favorite languages
     * - Similar XP levels
     * - Similar learning progress
     */
    public static void getSuggestions(String currentUserId, SuggestionCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // First, get current user's profile
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot currentUserSnapshot) {
                if (!currentUserSnapshot.exists()) {
                    callback.onError("User profile not found");
                    return;
                }

                // Get current user's interests/stats
                String currentUserLanguage = currentUserSnapshot.child("favoriteLanguage").getValue(String.class);
                Integer currentUserLevel = currentUserSnapshot.child("level").getValue(Integer.class);

                // Now get all other users
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot allUsersSnapshot) {
                        List<UserSuggestion> suggestions = new ArrayList<>();

                        for (DataSnapshot userSnapshot : allUsersSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey();

                            // Skip current user
                            if (userId.equals(currentUserId)) continue;

                            String username = userSnapshot.child("username").getValue(String.class);
                            String displayName = userSnapshot.child("displayName").getValue(String.class);
                            String email = userSnapshot.child("email").getValue(String.class);
                            String favoriteLanguage = userSnapshot.child("favoriteLanguage").getValue(String.class);
                            Integer level = userSnapshot.child("level").getValue(Integer.class);

                            String name = displayName != null ? displayName :
                                        (username != null ? username : email);

                            UserSuggestion suggestion = new UserSuggestion(userId, name, email);

                            // Calculate match score
                            int matchScore = 0;

                            // Same favorite language (+30 points)
                            if (currentUserLanguage != null && currentUserLanguage.equals(favoriteLanguage)) {
                                matchScore += 30;
                                suggestion.commonInterests.add("Learning " + favoriteLanguage);
                            }

                            // Similar level (+20 points if within 3 levels)
                            if (currentUserLevel != null && level != null) {
                                int levelDiff = Math.abs(currentUserLevel - level);
                                if (levelDiff <= 3) {
                                    matchScore += 20 - (levelDiff * 5);
                                    suggestion.commonInterests.add("Level " + level);
                                }
                            }

                            // Add random variety (+10 points base for any user)
                            matchScore += 10;

                            suggestion.matchScore = matchScore;

                            // Only suggest users with match score > 15
                            if (matchScore > 15) {
                                suggestions.add(suggestion);
                            }
                        }

                        // Sort by match score (highest first)
                        java.util.Collections.sort(suggestions, (a, b) -> Integer.compare(b.matchScore, a.matchScore));

                        // Limit to top 10
                        if (suggestions.size() > 10) {
                            suggestions = suggestions.subList(0, 10);
                        }

                        callback.onSuggestionsReady(suggestions);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}

