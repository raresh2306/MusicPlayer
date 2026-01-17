# 🔥 Configurare Firebase - Ghid Pas cu Pas

Aplicația ta folosește acum **Firebase** pentru autentificare și baza de date online (gratuit!).

## 📋 Ce ai nevoie:

1. Un cont Google
2. Acces la [Firebase Console](https://console.firebase.google.com/)

## 🚀 Pași de configurare:

### Pasul 1: Creează un proiect Firebase

1. Mergi la [Firebase Console](https://console.firebase.google.com/)
2. Click pe **"Add project"** sau **"Create a project"**
3. Introdu un nume pentru proiect (ex: "MusicPlayer")
4. Acceptă termenii și continuă
5. **Dezactivează** Google Analytics (opțional, pentru simplitate)
6. Click **"Create project"**

### Pasul 2: Adaugă aplicația Android

1. În proiectul Firebase, click pe iconița **Android** (sau "Add app")
2. Completează:
   - **Android package name**: `com.example.musicplayer`
   - **App nickname** (opțional): MusicPlayer
   - **Debug signing certificate SHA-1** (opțional, lasă gol pentru moment)
3. Click **"Register app"**

### Pasul 3: Descarcă google-services.json

1. Firebase îți va oferi un fișier `google-services.json`
2. **IMPORTANT**: Copiază acest fișier în folderul `app/` al proiectului tău:
   ```
   MusicPlayer/
   └── app/
       └── google-services.json  ← AICI!
   ```

### Pasul 4: Activează Authentication

1. În Firebase Console, mergi la **Authentication** (în meniul din stânga)
2. Click pe **"Get started"**
3. Mergi la tab-ul **"Sign-in method"**
4. Click pe **"Email/Password"**
5. **Activează** "Email/Password" (toggle ON)
6. Click **"Save"**

### Pasul 5: Activează Firestore Database

1. În Firebase Console, mergi la **Firestore Database**
2. Click pe **"Create database"**
3. Alege **"Start in test mode"** (pentru început)
4. Alege o locație (ex: `europe-west1` pentru Europa)
5. Click **"Enable"**

⚠️ **IMPORTANT**: În test mode, baza de date este accesibilă pentru 30 de zile. După aceea, va trebui să configurezi reguli de securitate.

### Pasul 6: Configurează regulile Firestore (IMPORTANT!)

⚠️ **ACEST PAS ESTE OBLIGATORIU** pentru ca aplicația să funcționeze corect!

1. Mergi la **Firestore Database** → **Rules**
2. Înlocuiește regulile cu:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Utilizatorii pot citi/scrie doar propriile date
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Melodiile cloud - utilizatorii pot adăuga/șterge doar propriile melodii
    match /cloud_songs/{songId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Playlist-uri - utilizatorii pot gestiona doar propriile playlist-uri
    match /playlists/{playlistId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```
3. Click **"Publish"**

⚠️ **IMPORTANT**: Fără aceste reguli, vei primi eroarea "permission_denied" când încerci să adaugi melodii cloud!

## ✅ Verificare

După ce ai adăugat `google-services.json`, aplicația ar trebui să funcționeze!

## 📊 Limitele planului gratuit Firebase:

- **Authentication**: 50,000 utilizatori activi/lună
- **Firestore**: 
  - 1 GB storage
  - 50,000 citiri/zi
  - 20,000 scrieri/zi
  - 20,000 ștergeri/zi

Pentru o aplicație personală, aceste limite sunt mai mult decât suficiente! 🎉

## 🆘 Probleme comune:

**Eroare: "google-services.json not found"**
- Asigură-te că fișierul este în folderul `app/`, nu în root
- Rebuild aplicația (Build → Rebuild Project)

**Eroare: "FirebaseApp not initialized"**
- Verifică că ai adăugat plugin-ul Google Services în `build.gradle.kts`
- Sync project (File → Sync Project with Gradle Files)

**Nu se conectează la Firebase**
- Verifică că ai permisiunea INTERNET în AndroidManifest.xml (deja adăugată)
- Verifică conexiunea la internet

## 📝 Note:

- Baza de date Room (locală) este încă folosită pentru melodiile din aplicație
- Utilizatorii sunt sincronizați online prin Firebase
- Datele sunt stocate în cloud și accesibile de pe orice dispozitiv
