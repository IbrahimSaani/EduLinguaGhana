package com.edulinguaghana.social;

import com.edulinguaghana.social.impl.InMemorySocialRepository;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class InMemorySocialRepositoryTest {

    @Test
    public void addAndAcceptFriend_flow() {
        InMemorySocialRepository repo = new InMemorySocialRepository();
        Friend req = repo.addFriend("userA", "userB");
        assertNotNull(req);
        assertEquals(Friend.Status.PENDING, req.status);

        List<Friend> requests = repo.getFriendRequests("userB");
        assertEquals(1, requests.size());

        Friend accepted = repo.acceptFriend("userB", "userA");
        assertNotNull(accepted);
        assertEquals(Friend.Status.ACCEPTED, accepted.status);

        List<Friend> friendsOfA = repo.getFriends("userA");
        List<Friend> friendsOfB = repo.getFriends("userB");
        assertEquals(1, friendsOfA.size());
        assertEquals(1, friendsOfB.size());
        assertEquals("userB", friendsOfA.get(0).friendUserId);
    }

    @Test
    public void removeFriend_works() {
        InMemorySocialRepository repo = new InMemorySocialRepository();
        repo.addFriend("userA", "userB");
        repo.acceptFriend("userB", "userA");
        boolean removed = repo.removeFriend("userA", "userB");
        assertTrue(removed);
        assertEquals(0, repo.getFriends("userA").size());
    }

    @Test
    public void createChallenge_andRetrieve() {
        InMemorySocialRepository repo = new InMemorySocialRepository();
        Challenge c = new Challenge();
        c.quizId = "quiz1";
        c.challengerId = "userA";
        c.challengedId = "userB";
        Challenge created = repo.createChallenge(c);
        assertNotNull(created.id);
        List<Challenge> listA = repo.getChallengesForUser("userA");
        assertEquals(1, listA.size());
    }
}
