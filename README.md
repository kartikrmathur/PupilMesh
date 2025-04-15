# PupilMesh Android App

A mobile application implementing Clean Architecture + MVVM, using Jetpack Navigation with Single Activity Architecture.

## Features

### 1. User Authentication

- Sign in screen with email/password authentication
- Automatic account creation for new users
- Secure credential storage in Room Database
- Session persistence across app restarts

### 2. Manga Browser

- Browse manga from the MangaVerse API
- Pagination support for efficient loading
- Offline caching using Room Database
- Detailed manga information page
- Pull-to-refresh functionality

### 3. Face Recognition

- Real-time face detection using MediaPipe
- Front camera integration
- Visual feedback when face is correctly positioned
- Reference rectangle that changes color (green/red) based on face position

## Architecture

This app follows modern Android development best practices:

- **Clean Architecture**: Separation of concerns with layers for data, domain, and presentation
- **MVVM Pattern**: ViewModels for business logic, Views for UI concerns
- **Single Activity Architecture**: One main activity with multiple fragments for different screens
- **Jetpack Navigation**: Fragment-based navigation with actions and arguments
- **View Binding**: Type-safe access to UI elements

## Libraries Used

- **AndroidX**

  - Navigation Component
  - Room Database
  - Lifecycle Components
  - ViewModel
  - RecyclerView

- **Media & Imaging**

  - MediaPipe Face Detection
  - CameraX
  - Glide for image loading

- **Networking**

  - OkHttp for API calls
  - Gson for JSON parsing

- **User Interface**
  - Material Design Components
  - SwipeRefreshLayout

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Set your RapidAPI key in `strings.xml` for the MangaVerse API
4. Build and run the app

## Requirements

- Android SDK 24+
- Android Studio Arctic Fox or newer
