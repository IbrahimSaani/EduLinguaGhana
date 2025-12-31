package com.edulinguaghana.social;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.edulinguaghana.ProfileActivity;
import com.edulinguaghana.social.impl.InMemorySocialRepository;
import com.edulinguaghana.social.SocialProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FriendFlowTest {

    @Before
    public void setup() {
        InMemorySocialRepository repo = new InMemorySocialRepository();
        SocialProvider.init(repo);
    }

    @Test
    public void addFriend_buttonCreatesRequest() throws Exception {
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), ProfileActivity.class);
        intent.putExtra("PROFILE_USER_ID", "userB");
        intent.putExtra("TEST_CURRENT_USER_ID", "userA");
        try (ActivityScenario<ProfileActivity> sc = ActivityScenario.launch(intent)) {
            // Click Add Friend
            onView(withId(com.edulinguaghana.R.id.btnAddFriend)).perform(click());

            // Allow UI thread to process
            Thread.sleep(500);

            // Verify repository
            InMemorySocialRepository repo = (InMemorySocialRepository) SocialProvider.get();
            assertNotNull(repo);
            assertEquals(1, repo.getFriendRequests("userB").size());
        }
    }
}

