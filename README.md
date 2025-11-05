# 🔔 Notifyer

A modern Android notification management app built with Jetpack Compose and Material 3 design. Notifyer
allows users to create, manage, and schedule notifications with a clean and intuitive user interface.

[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)](https://img.shields.io/badge/License-GPL%20v3-yellow.svg)

## Features

- 🚀 **Modern UI**: Built with Jetpack Compose and Material 3 for a beautiful, responsive interface
- 📝 **Create Notifications**: Easily create new notifications with custom titles and messages
- 🔄 **Manage Notifications**: View, update, and delete notifications with simple gestures
- 🔄 **Persistent Storage**: Notifications are saved locally and persist between app sessions
- 🎨 **Theming**: Supports both light and dark themes
- 📱 **Responsive Design**: Works on various screen sizes and orientations
- 🔄 **Shuffle Notifications**: Quick action to reorder notifications randomly
- 🗑️ **Bulk Actions**: Clear all notifications or delete them in batches

## Screenshots

*Screenshots will be added soon*

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt (planned)
- **Data Storage**: DataStore for persistent storage
- **Asynchronous**: Kotlin Coroutines & Flow
- **Material Design**: Material 3 components and theming
- **Build System**: Gradle with Kotlin DSL

## Prerequisites

- Android Studio Flamingo (2022.2.1) or newer
- Android SDK 33 (API 33) or higher
- Gradle 8.0 or higher
- Kotlin 1.8.0 or higher

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/abdalmoniem/notifyer.git
   ```

2. **Open the project in Android Studio**
    - Select "Open an Existing Project"
    - Navigate to the cloned repository and select the project root

3. **Build and run**
    - Connect an Android device or start an emulator
    - Click the "Run" button in Android Studio or press `Shift + F10`

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/hifnawy/compose/notify/notifyer/
│   │   │   ├── dataStore/       # DataStore implementation for local storage
│   │   │   ├── model/           # Data models and repositories
│   │   │   ├── ui/              # UI components and screens
│   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   └── theme/       # App theming
│   │   │   └── viewModel/       # ViewModels for UI logic
│   │   └── res/                 # Resources (drawables, strings, etc.)
│   └── test/                    # Unit tests
```

## Architecture

Notifyer follows the MVVM (Model-View-ViewModel) architecture pattern with the following key components:

- **UI Layer**: Built with Jetpack Compose, following Material 3 design guidelines
- **ViewModel**: Manages UI-related data and business logic
- **Repository**: Handles data operations (currently using DataStore for local storage)
- **Model**: Data classes representing the app's domain model

## Data Persistence

Notifications are persisted using Android's DataStore, providing a modern, coroutine-based solution for
data storage. The app uses:

- **Proto DataStore**: For type-safe storage of notifications
- **Kotlin Serialization**: For serializing/deserializing notification data

## Permissions

The app requires the following permissions:

- `POST_NOTIFICATIONS`: To show notifications on Android 13 (API 33) and above

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3 Design](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android Developers Documentation](https://developer.android.com/)

## Contact

[//]: # ([AbdAlMoniem AlHifnawy] - [@your_twitter]&#40;https://twitter.com/your_twitter&#41;)

Project Link: [https://github.com/abdalmoniem/notifyer](https://github.com/abdalmoniem/notifyer)
