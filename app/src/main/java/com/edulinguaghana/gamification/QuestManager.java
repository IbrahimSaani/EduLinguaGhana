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

    public static void saveQuests(Context ctx, List<Quest> quests) {
        JSONArray arr = new JSONArray();
        for (Quest q : quests) arr.put(q.toJson());
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(KEY_QUESTS, arr.toString()).apply();
    }

    // Made public so pure unit tests can verify generation logic without Android
    public static List<Quest> generateDefaultDailyQuests() {
        List<Quest> list = new ArrayList<>();
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

        return list;
    }

    public static boolean completeQuest(Context ctx, String questId) {
        List<Quest> list = getDailyQuests(ctx);
        boolean changed = false;
        for (Quest q : list) {
            if (q.id.equals(questId) && !q.completed) {
                q.completed = true;
                changed = true;
                // award xp
                XPManager.awardXP(ctx, q.xpReward, "quest:" + q.id);
            }
        }
        if (changed) saveQuests(ctx, list);
        return changed;
    }

    public static void progressQuest(Context ctx, String questId, int delta) {
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
