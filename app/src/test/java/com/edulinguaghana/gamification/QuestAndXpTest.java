package com.edulinguaghana.gamification;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class QuestAndXpTest {

    @Test
    public void questJsonRoundtrip() {
        Quest q = new Quest();
        q.id = "q1";
        q.title = "Test Quest";
        q.description = "Do something";
        q.xpReward = 12;
        q.completed = false;
        q.target = 1;

        Gson g = new Gson();
        String json = g.toJson(q);
        Quest q2 = g.fromJson(json, Quest.class);

        assertEquals(q.id, q2.id);
        assertEquals(q.title, q2.title);
        assertEquals(q.description, q2.description);
        assertEquals(q.xpReward, q2.xpReward);
        assertEquals(q.completed, q2.completed);
        assertEquals(q.target, q2.target);
    }

    @Test
    public void xpStateJsonRoundtrip() {
        XPState s = new XPState();
        s.totalXp = 250;
        s.level = 3;
        s.xpIntoLevel = 50;

        Gson g = new Gson();
        String json = g.toJson(s);
        XPState s2 = g.fromJson(json, XPState.class);

        assertEquals(s.totalXp, s2.totalXp);
        assertEquals(s.level, s2.level);
        assertEquals(s.xpIntoLevel, s2.xpIntoLevel);
    }
}
