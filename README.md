# Moto Tap (MVP Foundation)

Android-first driver-mechanic marketplace scaffold for Nairobi using Kotlin + Firebase.

## Implemented in this pass

- Jetpack Compose navigation with three MVP tabs:
  - Driver: create job request + view own jobs in real time
  - Mechanic: view open jobs + update status
  - Overview: current scope and next milestones
- MVVM layering with repository interfaces and Firebase-backed implementations
- Firestore persistence enabled for low-connectivity behavior
- Firestore rules + composite index starter files under `firebase/`
- Swahili resource starter (`values-sw/strings.xml`)

## Project structure (new parts)

- `app/src/main/java/com/example/mototap/core/model/`
- `app/src/main/java/com/example/mototap/core/repository/`
- `app/src/main/java/com/example/mototap/core/data/firebase/`
- `app/src/main/java/com/example/mototap/features/`
- `app/src/main/java/com/example/mototap/navigation/`
- `firebase/firestore.rules`
- `firebase/firestore.indexes.json`

## Run locally

1. Ensure `app/google-services.json` points to your Firebase project.
2. Build and run tests:

```bash
./gradlew testDebugUnitTest
```

3. Start app from Android Studio on a device/emulator.

## Firebase notes

- Auth currently uses anonymous sign-in to keep flows runnable while OTP is being wired.
- Jobs are written to Firestore `jobs/{jobId}` and streamed via snapshot listeners.
- Next backend step: add Cloud Functions for matching, FCM fanout, and M-PESA STK flow.

## Immediate next tasks

1. Replace anonymous auth with Firebase Phone OTP.
2. Add driver-mechanic chat (`chats/{jobId}/messages`).
3. Implement Cloud Function job matching (distance + rating + availability).
4. Add M-PESA payment initiation and callback handling.

