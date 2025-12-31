package com.edulinguaghana.social.service;

import com.edulinguaghana.social.Friend;
import com.edulinguaghana.social.impl.InMemorySocialRepository;
import org.junit.Test;

import static org.junit.Assert.*;

public class FriendServiceTest {

    @Test
    public void sendAndAccept() {
        InMemorySocialRepository repo = new InMemorySocialRepository();
        FriendService svc = new FriendService(repo);

        Friend r = svc.sendFriendRequest("A", "B");
        assertNotNull(r);
        assertEquals(Friend.Status.PENDING, r.status);

        Friend accepted = svc.acceptFriendRequest("B", "A");
        assertNotNull(accepted);
        assertEquals(Friend.Status.ACCEPTED, accepted.status);

        assertEquals(1, svc.getFriends("A").size());
    }
}

