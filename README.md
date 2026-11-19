A native Android personal assistant application built with Kotlin, Jetpack Compose, and Material 

It combines on-device speech-to-text, text-to-speech, and a local intent parser with the Gemini API and a modular tool registry to execute user commands. 

Key features include:   Voice & Agent Interface: Animated orb (TxOrb), live audio waveform, transcript view, activity status badges, and quick-action chips.   

AI & Tool Execution: TxAgentCoordinator routing between GeminiApiService, LocalIntentParser, and ToolRegistry.   

Productivity Suite: Room database (AppDatabase) storing conversations, tasks, notes, and scheduled reminders.   

System Integration: Background reminder scheduling with reboot persistence (ReminderScheduler, ReminderBroadcastReceiver, BootReceiver) and an installed-app launcher (AppLauncherManager). 

# Run and deploy

This contains everything you need to run your app locally.
App can be run on physical devices.

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device.
