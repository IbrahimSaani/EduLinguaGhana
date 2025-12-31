package com.edulinguaghana.gamification;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

public class XPState {
    public int totalXp;
    public int level;
    public int xpIntoLevel;
    public long lastUpdated;

    public XPState() {
        this.totalXp = 0;
        this.level = 1;
        this.xpIntoLevel = 0;
        this.lastUpdated = System.currentTimeMillis();
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

    public static XPState fromJson(JSONObject o) {
        if (o == null) return new XPState();
        try {
            Gson g = new Gson();
            return g.fromJson(o.toString(), XPState.class);
        } catch (Exception e) {
            XPState s = new XPState();
            s.totalXp = o.optInt("totalXp", 0);
            s.level = o.optInt("level", 1);
            s.xpIntoLevel = o.optInt("xpIntoLevel", 0);
            s.lastUpdated = o.optLong("lastUpdated", System.currentTimeMillis());
            return s;
        }
    }
}
