package com.edulinguaghana.social;

import com.edulinguaghana.social.impl.LocalJsonSocialRepository;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class LocalJsonSocialRepositoryTest {

    @Test
    public void persistAndLoad_friendAndChallenge() throws Exception {
        File tmp = File.createTempFile("social_repo_test", ".json");
        tmp.deleteOnExit();
        // empty file
        LocalJsonSocialRepository repo = new LocalJsonSocialRepository(tmp);

        Friend f = repo.addFriend("userX", "userY");
        assertNotNull(f);
        List<Friend> reqs = repo.getFriendRequests("userY");
        assertEquals(1, reqs.size());

        Challenge c = new Challenge();
        c.quizId = "quizX";
        c.challengerId = "userX";
        c.challengedId = "userY";
        repo.createChallenge(c);

        // create a new instance backed by same file to ensure load works
        LocalJsonSocialRepository repo2 = new LocalJsonSocialRepository(tmp);
        List<Challenge> challenges = repo2.getChallengesForUser("userX");
        assertEquals(1, challenges.size());
    }
}

