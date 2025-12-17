package com.edulinguaghana;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class OfflineManager {

    private Context context;

    public OfflineManager(Context context) {
        this.context = context;
    }

    /**
     * Check if device is connected to internet
     */
    public boolean isOnline() {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        // Check Firebase Authentication status
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser user = auth.getCurrentUser();
        return user != null;
    }

    /**
     * Features available offline
     */
    public boolean canAccessOffline(String feature) {
        switch (feature) {
            case "ALPHABET":
            case "NUMBERS":
            case "RECITAL":
            case "PRACTICE":
            case "QUIZ":
                return true; // Core learning features work offline

            case "ACHIEVEMENTS":
            case "BADGES":
            case "LEADERBOARD":
            case "SYNC":
                return false; // Require online + login

            default:
                return true;
        }
    }

    /**
     * Check if feature requires login
     */
    public boolean requiresLogin(String feature) {
        switch (feature) {
            case "ACHIEVEMENTS":
            case "BADGES":
            case "LEADERBOARD":
            case "CLOUD_SYNC":
                return true;

            default:
                return false;
        }
    }

    /**
     * Get offline status message
     */
    public String getOfflineMessage() {
        return "You're offline. Some features require internet connection.";
    }

    /**
     * Get login required message
     */
    public String getLoginRequiredMessage(String feature) {
        return "Login required to access " + feature + ". Create an account or sign in to unlock this feature!";
    }
}

