package com.edulinguaghana.gamification;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuestManager {
    private static final String PREFS = "gamification_prefs";
    private static final String KEY_QUESTS = "quests";

    public static List<Quest> getDailyQuests(Context ctx) {
        synchronized (QuestManager.class) {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String j = p.getString(KEY_QUESTS, null);
            List<Quest> out = new ArrayList<>();
            if (j == null) {
                // generate default daily quests
                out = generateDefaultDailyQuests();
                saveQuests(ctx, out);
                return out;
            }
            try {
                JSONArray arr = new JSONArray(j);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    out.add(Quest.fromJson(o));
                }
            } catch (JSONException e) {
                out = generateDefaultDailyQuests();
                saveQuests(ctx, out);
            }
            return out;
        }
    }

    public static void saveQuests(Context ctx, List<Quest> quests) {
        synchronized (QuestManager.class) {
            JSONArray arr = new JSONArray();
            for (Quest q : quests) arr.put(q.toJson());
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            // Use commit() instead of apply() for critical quest data to ensure synchronous write
            p.edit().putString(KEY_QUESTS, arr.toString()).commit();
        }
    }

    // Made public so pure unit tests can verify generation logic without Android
    public static List<Quest> generateDefaultDailyQuests() {
        List<Quest> list = new ArrayList<>();

        // Quest 1: Practice once
        Quest q1 = new Quest();
        q1.id = "daily_practice";
        q1.title = "Practice once";
        q1.description = "Complete one practice session today.";
        q1.xpReward = 10;
        q1.completed = false;
        q1.target = 1;
        q1.progress = 0;
        q1.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q1);

        // Quest 2: Do a quiz
        Quest q2 = new Quest();
        q2.id = "daily_quiz";
        q2.title = "Do a quiz";
        q2.description = "Finish at least one quiz.";
        q2.xpReward = 15;
        q2.completed = false;
        q2.target = 1;
        q2.progress = 0;
        q2.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q2);

        // Quest 3: Try a challenge
        Quest q3 = new Quest();
        q3.id = "daily_challenge";
        q3.title = "Try a challenge";
        q3.description = "Attempt one challenge from Challenges tab.";
        q3.xpReward = 20;
        q3.completed = false;
        q3.target = 1;
        q3.progress = 0;
        q3.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q3);

        // Quest 4: Multiple practices ← NEW
        Quest q4 = new Quest();
        q4.id = "practice_streak";
        q4.title = "Practice 3 times";
        q4.description = "Complete 3 practice sessions.";
        q4.xpReward = 30;
        q4.completed = false;
        q4.target = 3;
        q4.progress = 0;
        q4.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q4);

        // Quest 5: Multiple quizzes ← NEW
        Quest q5 = new Quest();
        q5.id = "quiz_multiple";
        q5.title = "Complete 2 quizzes";
        q5.description = "Finish 2 different quiz types.";
        q5.xpReward = 25;
        q5.completed = false;
        q5.target = 2;
        q5.progress = 0;
        q5.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q5);

        // Quest 6: Speed game ← NEW
        Quest q6 = new Quest();
        q6.id = "speed_game";
        q6.title = "Play speed game";
        q6.description = "Try the speed challenge game.";
        q6.xpReward = 35;
        q6.completed = false;
        q6.target = 1;
        q6.progress = 0;
        q6.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q6);

        // Quest 7: Language learning ← NEW
        Quest q7 = new Quest();
        q7.id = "language_explorer";
        q7.title = "Practice 2 languages";
        q7.description = "Practice with 2 different languages.";
        q7.xpReward = 40;
        q7.completed = false;
        q7.target = 2;
        q7.progress = 0;
        q7.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q7);

        // Quest 8: Marathon session ← NEW
        Quest q8 = new Quest();
        q8.id = "marathon_learner";
        q8.title = "Study marathon";
        q8.description = "Complete 5 total activities.";
        q8.xpReward = 50;
        q8.completed = false;
        q8.target = 5;
        q8.progress = 0;
        q8.expiresAt = System.currentTimeMillis() + 24L*60*60*1000L;
        list.add(q8);

        return list;
    }

    public static boolean completeQuest(Context ctx, String questId) {
        synchronized (QuestManager.class) {
            List<Quest> list = getDailyQuests(ctx);
            boolean changed = false;
            for (Quest q : list) {
                if (q.id.equals(questId) && !q.completed) {
                    // Only complete if progress has reached target
                    if (q.progress >= q.target) {
                        q.completed = true;
                        changed = true;
                        // award xp
                        XPManager.awardXP(ctx, q.xpReward, "quest:" + q.id);
                    }
                }
            }
            if (changed) saveQuests(ctx, list);
            return changed;
        }
    }

    public static void progressQuest(Context ctx, String questId, int delta) {
        synchronized (QuestManager.class) {
            List<Quest> list = getDailyQuests(ctx);
            boolean changed = false;
            for (Quest q : list) {
                if (q.id.equals(questId) && !q.completed) {
                    q.progress += delta;
                    if (q.progress >= q.target) {
                        q.completed = true;
                        XPManager.awardXP(ctx, q.xpReward, "quest:" + q.id);
                    }
                    changed = true;
                }
            }
            if (changed) saveQuests(ctx, list);
        }
    }

    // Pure in-memory helper to mark a quest completed in a list (no Android APIs) - useful for unit testing
    public static boolean markQuestCompletedInList(List<Quest> list, String questId) {
        if (list == null) return false;
        boolean changed = false;
        for (Quest q : list) {
            if (q != null && q.id != null && q.id.equals(questId) && !q.completed) {
                q.completed = true;
                changed = true;
            }
        }
        return changed;
    }
}
