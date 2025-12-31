package com.edulinguaghana.social;

public class SocialProvider {
    private static SocialRepository INSTANCE;

    public static void init(SocialRepository repo) {
        INSTANCE = repo;
    }

    public static SocialRepository get() {
        return INSTANCE;
    }
}

