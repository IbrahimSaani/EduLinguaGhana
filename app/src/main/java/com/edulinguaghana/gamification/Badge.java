package com.edulinguaghana.gamification;

import org.json.JSONException;
import org.json.JSONObject;

public class Badge {
    public String id;
    public String title;
    public String description;
    public String iconName;
    public boolean unlocked;
    public long unlockedAt;

    public Badge() {}

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("title", title);
            o.put("description", description);
            o.put("iconName", iconName);
            o.put("unlocked", unlocked);
            o.put("unlockedAt", unlockedAt);
        } catch (JSONException ignored) {}
        return o;
    }

    public static Badge fromJson(JSONObject o) {
        Badge b = new Badge();
        if (o == null) return b;
        b.id = o.optString("id", "");
        b.title = o.optString("title", "");
        b.description = o.optString("description", "");
        b.iconName = o.optString("iconName", "");
        b.unlocked = o.optBoolean("unlocked", false);
        b.unlockedAt = o.optLong("unlockedAt", 0);
        return b;
    }
}

