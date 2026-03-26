# Moto Tap - App Distribution Guide

## Option 1: Firebase App Distribution (Recommended) ⭐

Firebase App Distribution makes it easy to share your app with testers.

### Step 1: Set Up Firebase CLI
```bash
npm install -g firebase-tools
firebase login
```

### Step 2: Add Testers' Emails
Edit `app/build.gradle.kts` (at the end) and add tester email addresses:

```kotlin
firebaseAppDistribution {
    testerEmails = "tester1@example.com,tester2@example.com"
    releaseNotes = "MVP Release - Version 1.0"
    releaseName = "v1.0"
}
```

### Step 3: Build & Distribute

**For Debug APK (fastest for testing):**
```bash
cd /run/media/emobilis/CCC6-E80E/MOTO\ TAP
./gradlew appDistributionUploadDebug
```

**For Release APK (signed):**
```bash
./gradlew appDistributionUploadRelease
```

### Step 4: Users Receive Invitation
- Testers will receive an email invitation
- They click the link to install the app
- App updates automatically when new builds are distributed

---

## Option 2: Direct APK Distribution

If Firebase Distribution isn't set up, build and share an APK directly:

### Build Debug APK:
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK (requires signing):
```bash
./gradlew assembleRelease
```

### Share the APK:
- Email it to users
- Upload to Google Drive or Dropbox
- Host on GitHub Releases
- Users download and install manually

---

## Option 3: Google Play Store (Production)

For public release:

1. **Create a Google Play Developer account** ($25 one-time)
2. **Sign your release APK:**
   ```bash
   ./gradlew bundleRelease
   ```
3. **Upload to Play Console** and review process
4. Users can find it on Google Play Store

---

## Current Setup Status

✅ Firebase App Distribution plugin added to `app/build.gradle.kts`
✅ Both Debug and Release configurations configured
⚠️ **TODO: Add your testers' emails to `build.gradle.kts`**

---

## Quick Start Commands

```bash
# Navigate to project
cd /run/media/emobilis/CCC6-E80E/MOTO\ TAP

# Login to Firebase
firebase login

# Build & upload debug build to Firebase App Distribution
./gradlew appDistributionUploadDebug

# Or build debug APK to share manually
./gradlew assembleDebug
```

---

## Troubleshooting

### "Authentication required"
```bash
firebase logout
firebase login
```

### "Build failed"
```bash
./gradlew clean
./gradlew assembleDebug
```

### "APK not found"
Check `app/build/outputs/apk/debug/` directory exists

---

## Next Steps for Production

1. **Set up Firebase Hosting** for any web component
2. **Enable Firebase Authentication** properly (currently anonymous)
3. **Test M-PESA integration** with Safaricom
4. **Implement Cloud Functions** for job matching
5. **Add FCM notifications** for real-time updates

