package com.edulinguaghana;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.FirebaseApp;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase (no-op if already initialized by google-services)
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception ignored) {
        }

        // Initialize Facebook SDK
        try {
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(this);
        } catch (Exception ignored) {
        }
    }
}

