package com.edulinguaghana.social.service;

import com.edulinguaghana.social.Friend;
import com.edulinguaghana.social.SocialRepository;

import java.util.List;

public class FriendService {
    private final SocialRepository repo;

    public FriendService(SocialRepository repo) {
        this.repo = repo;
    }

    public Friend sendFriendRequest(String fromUserId, String toUserId) {
        return repo.addFriend(fromUserId, toUserId);
    }

    public Friend acceptFriendRequest(String userId, String requesterId) {
        return repo.acceptFriend(userId, requesterId);
    }

    public boolean unfriend(String userId, String friendId) {
        return repo.removeFriend(userId, friendId);
    }

    public List<Friend> getFriends(String userId) {
        return repo.getFriends(userId);
    }

    public List<Friend> getFriendRequests(String userId) {
        return repo.getFriendRequests(userId);
    }
}

