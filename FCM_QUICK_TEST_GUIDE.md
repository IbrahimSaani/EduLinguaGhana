# 🚀 Quick FCM Testing Guide

## ✅ FCM is Already Enabled!

Since you see "Create your first campaign" in Firebase Console, FCM is already set up and ready to use!

---

## 📱 **QUICK TEST - 5 Minutes**

### **Step 1: Run Your App** (1 minute)

1. Open your app on a device or emulator
2. Make sure you're logged in
3. Wait a few seconds for FCM token to generate

### **Step 2: Check Token Generated** (1 minute)

Open Android Studio Logcat and search for:
```
FCM Token:
```

You should see something like:
```
FCMTokenManager: FCM Token: ey...abc123...xyz
FCMTokenManager: FCM token saved to Firebase
```

✅ This means your app is ready to receive notifications!

### **Step 3: Send Test Notification** (3 minutes)

#### **Option A: To All Devices (Easiest)**

1. Open Firebase Console: https://console.firebase.google.com/
2. Select: **EduLinguaGhana**
3. Go to: **Engage** → **Messaging**
4. Click: **"Create your first campaign"** (or **"New campaign"**)
5. Select: **"Firebase Notification messages"**
6. Fill in:
   ```
   Title: Hello!
   Text: Push notifications are working! 🎉
   ```
7. Click **"Next"**
8. Target: **"User segment"** → Select **"All users"**
9. Click **"Next"**
10. Scheduling: **"Now"**
11. Keep clicking **"Next"** until you reach **"Review"**
12. Click **"Publish"**

✅ **Check your device - notification should appear!**

---

#### **Option B: To Your Specific Device (Recommended)**

This is better for testing because it only sends to your device.

1. **Copy your FCM token** from Logcat (from Step 2 above)
2. Open Firebase Console → Messaging
3. Click **"New campaign"** → **"Firebase Notification messages"**
4. Fill in notification details:
   ```
   Title: Test Notification
   Text: This is a test 📱
   ```
5. Look for **"Send test message"** link (usually on the right side)
6. Click **"Send test message"**
7. **Paste your FCM token** in the dialog
8. Click **"+" to add the token**
9. Click **"Test"**

✅ **Notification should appear on your device instantly!**

---

## 🎯 **Test Social Notifications (Advanced)**

To test friend request, challenge, and other social notifications:

### **Get Server Key:**
1. Firebase Console → ⚙️ (Settings) → **Project settings**
2. Go to **"Cloud Messaging"** tab
3. Find **"Server key"** (under Cloud Messaging API)
4. Click **"Copy"** (or show and copy)

### **Send Test Friend Request Notification:**

**Windows PowerShell:**
```powershell
$serverKey = "YOUR_SERVER_KEY_HERE"
$fcmToken = "YOUR_FCM_TOKEN_HERE"

$headers = @{
    "Authorization" = "key=$serverKey"
    "Content-Type" = "application/json"
}

$body = @{
    to = $fcmToken
    data = @{
        type = "friend_request"
        fromUserId = "test_user_123"
        displayName = "Test Friend"
    }
} | ConvertTo-Json

Invoke-RestMethod -Uri "https://fcm.googleapis.com/fcm/send" -Method Post -Headers $headers -Body $body
```

**Or use curl:**
```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "YOUR_FCM_TOKEN",
    "data": {
      "type": "friend_request",
      "fromUserId": "test_user_123",
      "displayName": "Test Friend"
    }
  }'
```

✅ **You should see:** "New Friend Request 👥" notification!

---

## 🔔 **Test All Notification Types:**

### **1. Friend Request:**
```json
{
  "to": "YOUR_FCM_TOKEN",
  "data": {
    "type": "friend_request",
    "fromUserId": "user123",
    "displayName": "John Doe"
  }
}
```
**Expected:** "New Friend Request 👥 - John Doe wants to be your friend!"

### **2. Challenge:**
```json
{
  "to": "YOUR_FCM_TOKEN",
  "data": {
    "type": "challenge",
    "fromUserId": "user123",
    "displayName": "John Doe",
    "quizType": "letters"
  }
}
```
**Expected:** "New Challenge ⚔️ - John Doe challenged you to letters!"

### **3. Friend Accepted:**
```json
{
  "to": "YOUR_FCM_TOKEN",
  "data": {
    "type": "friend_accepted",
    "fromUserId": "user123",
    "displayName": "John Doe"
  }
}
```
**Expected:** "Friend Request Accepted ✅ - John Doe is now your friend!"

### **4. Challenge Completed (Won):**
```json
{
  "to": "YOUR_FCM_TOKEN",
  "data": {
    "type": "challenge_completed",
    "fromUserId": "user123",
    "displayName": "John Doe",
    "won": "true"
  }
}
```
**Expected:** "Challenge Won! 🏆 - You beat John Doe!"

### **4. Challenge Completed (Lost):**
```json
{
  "to": "YOUR_FCM_TOKEN",
  "data": {
    "type": "challenge_completed",
    "fromUserId": "user123",
    "displayName": "John Doe",
    "won": "false"
  }
}
```
**Expected:** "Challenge Completed - Challenge with John Doe completed!"

---

## ✅ **Verification Checklist**

After testing, verify:

- [ ] FCM token generated and visible in Logcat
- [ ] Token saved to Firebase: `users/{uid}/fcmToken`
- [ ] Test notification received on device
- [ ] Notification can be tapped
- [ ] App opens when notification tapped
- [ ] Friend request notification shows correctly
- [ ] Challenge notification shows correctly
- [ ] All notification types work

---

## 🎉 **Success Indicators**

You'll know it's working when:

1. ✅ Logcat shows: `FCM Token: ey...`
2. ✅ Logcat shows: `FCM token saved to Firebase`
3. ✅ Test notification appears on device
4. ✅ Notification has correct title/text
5. ✅ Tapping notification opens the app
6. ✅ Social notifications show with emojis (👥⚔️✅🏆)

---

## 🚨 Troubleshooting

### **Problem: No token in Logcat**
**Solution:**
- Check internet connection
- Restart app
- Check: `google-services.json` is in `app/` folder

### **Problem: No notification received**
**Solution:**
- Check notification permission granted (Settings → Apps → EduLinguaGhana → Notifications)
- Check device is not in Do Not Disturb mode
- Try "Send test message" method instead of campaign

### **Problem: Can't find Server Key**
**Solution:**
- Firebase Console → ⚙️ Settings → Project settings
- Go to "Cloud Messaging" tab
- If you don't see Server key, you may need to enable Cloud Messaging API:
  - Click "Manage API in Google Cloud Console"
  - Enable "Firebase Cloud Messaging API"

---

## 📊 **What Happens When Notification Arrives**

1. Firebase sends notification to device
2. `MyFirebaseMessagingService.onMessageReceived()` is called
3. App checks notification type (`friend_request`, `challenge`, etc.)
4. `SocialNotificationHelper` creates appropriate notification
5. Android shows notification with emoji and message
6. User taps notification
7. App opens to ProfileActivity

---

## 🎊 **Next Steps After Testing**

Once basic notifications work:

1. **Test with 2 devices:**
   - Device A sends friend request
   - Device B should receive notification
   - (For now, you'll need to manually trigger notifications)

2. **Set up automatic notifications:**
   - Use Firebase Cloud Functions
   - Trigger on database events (friend request created, challenge sent, etc.)
   - No app code changes needed!

3. **Monitor notification delivery:**
   - Firebase Console → Messaging → Reporting dashboard
   - See delivery rates, open rates, etc.

---

## ⏱️ **Expected Timeline**

- **Basic test (Option A):** 5 minutes
- **Specific device test (Option B):** 3 minutes
- **Social notification test:** 10 minutes
- **All notification types:** 15 minutes

---

## 🎉 **You're Ready!**

FCM is enabled, code is implemented, and you're ready to test!

**Start with Option A (To All Devices)** - it's the easiest way to verify everything works.

Then move to **Option B (Specific Device)** for more precise testing.

Finally, test **Social Notifications** to see the custom notifications your app creates!

**Good luck! 🚀**

