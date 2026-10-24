# MathSprint Android Quiz — APK Build Guide

This package contains the complete Android Studio (Kotlin / Jetpack Compose) project for the **12-Loop Arithmetic & Prime Numbers Quiz**.

## Option 1: Build APK in Android Studio (On Your Computer)
1. Unzip `MathSprint_Android_Project.zip`.
2. Open **Android Studio** -> **Open** -> select the unzipped folder.
3. Wait for Gradle Sync to finish, then click **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
4. Your installable `app-debug.apk` will be generated in `app/build/outputs/apk/debug/app-debug.apk`.

## Option 2: Build APK Online Without Installing Android Studio (GitHub Actions)
1. Create a free repository on GitHub and upload the unzipped files.
2. Go to the **Actions** tab in your repository and click **Build Android APK -> Run workflow**.
3. In ~2 minutes, download `MathSprint-debug-apk` (`app-debug.apk`) directly to your phone or PC!
