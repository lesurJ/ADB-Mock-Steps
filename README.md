# ADB Mock Steps 👟

[![Android Release Build](https://github.com/lesurJ/ADB-Mock-Steps/actions/workflows/release.yaml/badge.svg)](https://github.com/lesurJ/ADB-Mock-Steps/actions/workflows/release.yaml)

This simple app allows writing mock step counts to Android's **Health Connect** via ADB broadcasts. While Health Connect provides its own testing APIs, this app offers a straightforward command-line method for developers and QA testers to inject step data for automating tests without modifying their app's production code.

## Table of Contents

- [ADB Mock Steps 👟](#adb-mock-steps-)
  - [Table of Contents](#table-of-contents)
  - [Important Notes](#important-notes)
  - [Prerequisites](#prerequisites)
  - [Usage](#usage)
    - [Example](#example)
  - [Testing With Other Apps](#testing-with-other-apps)
  - [Troubleshooting](#troubleshooting)
    - [Common Issues](#common-issues)
  - [Understanding the FLAG_INCLUDE_STOPPED_PACKAGES](#understanding-the-flag_include_stopped_packages)

## Important Notes

⚠️ This app's broadcast receiver uses an explicit intent, but it's crucial to ensure your test environment is secure.

- The app must remain installed and not force-stopped to receive broadcasts.
- Each command writes a new `StepsRecord` to Health Connect for a given duration. It does not overwrite previous data.
- Always verify the result in a Health Connect-compatible (e.g Google Fit) app to confirm the data was written successfully.

## Prerequisites

- 📱 **Health Connect Installed**: The [Health Connect (Android)](https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata) app must be installed on your device from the Play Stor.

- 🔓 **Developer Options Enabled**: Your device must have Developer Options enabled.

- **USB Debugging**: Enable USB debugging in Developer Options.

- 📲 **Install the App**:

  This app is not available on the app store. You can find a prebuilt APK in the Releases section of this repo.

  1. From your phone, download the `.apk` from the `Releases` section.
  2. Install it.

- 💪 **Grant Health Connect Permissions**:
  1. Open the `ADB Mock Steps` app.
  2. If Health Connect is not installed or needs an update, click the "Install/Updat" button that will redirect you to PlayStore.
  3. If the required permissions to read and write steps are not granted, tap the "Grant Permissions" button.
  4. This will open the standard Health Connect permissions screen.
  5. Enable the permissions for "Steps".
  6. Return to the app. The status should now show "permissions granted ✅".

## Usage

To write steps to Health Connect using adb, use the following broadcast command, replacing `NUMBER` and `DURATION` with your desired step count and duration, respectively:

```bash
adb shell am broadcast -a com.adbmocksteps.SET_STEPS --es steps "NUMBER" --es steps "DURATION" -f 0x01000000
```

- `--es steps "NUMBER"`: Passes the number of steps as an extra string (required, integer, unitless).
- `--es duration "DURATION"`: Passes the duration during which the steps were recorded as an extra string (required, integer, **unit: minutes**).
- `-f 0x01000000`: This is the `FLAG_INCLUDE_STOPPED_PACKAGES` flag, which is crucial for the command to work when the app is in the background.

⚠️ **Note**: The `-f 0x01000000` flag is crucial! It sets `FLAG_INCLUDE_STOPPED_PACKAGES`, allowing the broadcast to reach your app even when it's not actively running. See [Understanding the FLAG_INCLUDE_STOPPED_PACKAGES](#understanding-the-flag_include_stopped_packages).

### Example

Write a record of 123 steps and 5 minutes:

```bash
adb shell am broadcast -a com.adbmocksteps.SET_STEPS --es steps "123" --es duration "5" -f 0x01000000
```

## Testing With Other Apps

Once you've sent the command, you can verify the steps were recorded in:

- 💪 The official **Health Connect** app (under _Data and access_ -> _See all data_ -> _Steps_).
- 🏃 **Google Fit** or any other fitness app that is configured to read data from Health Connect.
- ✅ Your own application that integrates with the Health Connect SDK.

## Troubleshooting

**Check if the app is installed:**

```bash
adb shell pm list packages | grep adbmocksteps
```

**Check if the broadcast receiver is registered:**

```bash
adb shell dumpsys package com.adbmocksteps | grep -A 5 -B 5 Receiver
```

### Common Issues

1. **"Steps not being written" or "permission not granted" error in logcat**

   - Ensure you have granted the app permissions within Health Connect as described in the [Prerequisites](#prerequisites) section.
   - Make sure the Health Connect app itself is installed and up-to-date.

2. **Broadcast sent, but no steps appear in Health Connect**
   - Verify your ADB connection with `adb devices`.
   - Check the logcat for error messages from the app: `adb logcat -s ADBMockSteps`.
   - Ensure the broadcast action and extra key match exactly: `com.adbmocksteps.SET_STEPS` and `steps`.
   - Health Connect filters out some data if it not feasible or incoherent (e.g doing 10 000 steps in 1 minute). So make sure to have a coherent/feasible broadcast.

## Understanding the FLAG_INCLUDE_STOPPED_PACKAGES

The `-f 0x01000000` flag is essential because:

- **Android Security**: Since Android 3.1, apps in a "stopped" state do not receive implicit broadcasts.
- **Stopped State**: Apps are considered "stopped" when not actively running or recently used.
- **The Flag**: `FLAG_INCLUDE_STOPPED_PACKAGES` (0x01000000) bypasses this restriction.
- **Without Flag**: `flg=0x400000` (excludes stopped packages).
- **With Flag**: `flg=0x1400000` (this includes both `FLAG_INCLUDE_STOPPED_PACKAGES` and `FLAG_RECEIVER_FOREGROUND`).
