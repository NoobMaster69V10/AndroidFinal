Firebase Setup Instructions:

1. Go to Firebase Console: https://console.firebase.google.com/
2. Create a new project or select existing project
3. Add Android app with package name: com.example.androidfinaltask
4. Download google-services.json file
5. Replace the placeholder file at: app/google-services.json with your actual file
6. Enable Authentication in Firebase Console:
   - Go to Authentication > Sign-in method
   - Enable Email/Password provider
7. Enable Firestore Database:
   - Go to Firestore Database
   - Create database in test mode (for development)
   - Set up security rules if needed

The app will now:
- Save registered users in Firestore
- Authenticate users with Firebase Auth
- Store user profile data in Firestore
- Load real user data for profile screen
- Handle logout functionality

