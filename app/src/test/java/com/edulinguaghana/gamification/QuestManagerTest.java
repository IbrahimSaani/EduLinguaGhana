package com.edulinguaghana.gamification;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class QuestManagerTest {

    @Test
    public void defaultDailyQuests_generateAndHaveExpectedIds() {
        List<Quest> list = QuestManager.generateDefaultDailyQuests();
        assertNotNull(list);
        assertTrue(list.size() >= 3);
        boolean foundPractice = false;
        boolean foundQuiz = false;
        boolean foundChallenge = false;
        for (Quest q : list) {
            if (q.id.equals("daily_practice")) foundPractice = true;
            if (q.id.equals("daily_quiz")) foundQuiz = true;
            if (q.id.equals("daily_challenge")) foundChallenge = true;
        }
        assertTrue(foundPractice && foundQuiz && foundChallenge);
    }

    @Test
    public void markQuestCompletedInList_works() {
        List<Quest> list = QuestManager.generateDefaultDailyQuests();
        boolean changed = QuestManager.markQuestCompletedInList(list, "daily_quiz");
        assertTrue(changed);
        Quest q = null;
        for (Quest it : list) if (it.id.equals("daily_quiz")) q = it;
        assertNotNull(q);
        assertTrue(q.completed);

        // Calling again should return false because it is already completed
        boolean changed2 = QuestManager.markQuestCompletedInList(list, "daily_quiz");
        assertFalse(changed2);
    }
}

