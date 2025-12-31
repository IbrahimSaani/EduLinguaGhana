package com.edulinguaghana.gamification;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

public class Quest {
    public String id;
    public String title;
    public String description;
    public int xpReward;
    public boolean completed;
    public long expiresAt;
    public int progress;
    public int target;

    public Quest() {
    }

    public JSONObject toJson() {
        try {
            Gson g = new Gson();
            String s = g.toJson(this);
            return new JSONObject(s);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public static Quest fromJson(JSONObject o) {
        if (o == null) return new Quest();
        try {
            Gson g = new Gson();
            return g.fromJson(o.toString(), Quest.class);
        } catch (Exception e) {
            Quest q = new Quest();
            q.id = o.optString("id", "");
            q.title = o.optString("title", "");
            q.description = o.optString("description", "");
            q.xpReward = o.optInt("xpReward", 0);
            q.completed = o.optBoolean("completed", false);
            q.expiresAt = o.optLong("expiresAt", 0);
            q.progress = o.optInt("progress", 0);
            q.target = o.optInt("target", 1);
            return q;
        }
    }
}
