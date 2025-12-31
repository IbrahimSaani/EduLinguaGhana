package com.edulinguaghana.social;

import java.util.List;

public interface SocialRepository {
    List<Friend> getFriends(String userId);
    Friend addFriend(String requesterId, String friendId);
    boolean removeFriend(String userId, String friendId);
    Friend acceptFriend(String userId, String requesterId);
    List<Friend> getFriendRequests(String userId);

    Challenge createChallenge(Challenge challenge);
    List<Challenge> getChallengesForUser(String userId);
    Challenge updateChallenge(Challenge challenge);

    List<LeaderboardEntry> getLeaderboard(String quizId, int limit);

    AchievementShare addAchievementShare(AchievementShare share);
}

