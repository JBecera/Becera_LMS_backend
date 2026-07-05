# Becera LMS Mobile

This Android app is a Kotlin + Jetpack Compose mobile companion to the existing Spring Boot + React web app.

## What is included
- Material 3 UI styled to match the current web branding
- MVVM architecture with ViewModel and Compose navigation
- Functional registration and login flows wired to the Spring Boot backend endpoint at http://10.0.2.2:8080/api/ members
- Dashboard screen for post-auth navigation

## Open in Android Studio
1. Open the mobile folder in Android Studio.
2. Let Gradle sync complete.
3. Run the app on an emulator or physical device.

## Backend note
The app expects the Spring Boot backend to be running locally on port 8080. The Android emulator uses 10.0.2.2 for localhost.
