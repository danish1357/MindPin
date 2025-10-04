# MindPin

MindPin is a modern Android note companion that keeps a persistent notification so you can jot down thoughts from anywhere. Notes support optional reminders and colorful tags, and can be browsed, sorted, edited, or deleted from the app.

## Features

- **Always-on quick capture** – A foreground notification with inline reply and quick actions lets you save notes without opening the app.
- **Structured organization** – Tag notes with curated defaults or create your own custom labels on the fly.
- **Smart reminders** – Attach reminder times to notes and receive actionable notifications to mark them complete.
- **Dynamic sorting** – Switch between newest, oldest, or tag-based ordering to surface what matters.
- **Modern UI** – Built entirely with Jetpack Compose and Material 3, supporting dynamic color and responsive layouts.

## Tech stack

- Kotlin + Jetpack Compose
- Room for local persistence
- DataStore for user preferences
- AlarmManager backed reminders
- Foreground service & notification actions for quick capture

## Getting started

1. Open the project in Android Studio (Giraffe or newer recommended).
2. Sync Gradle and build the project.
3. Run the `app` configuration on a device or emulator running Android 8.0 (API 26) or later.
4. Grant the notification permission when prompted so the persistent quick-capture notification can appear.

From the app you can add, edit, or delete notes, manage reminders, and sort by your preferred strategy. The notification allows capturing text instantly and launching a streamlined quick-add flow.
