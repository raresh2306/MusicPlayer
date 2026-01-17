# 🔧 Fix: Eroare "permission_denied" pentru Cloud Songs

## ❌ Problema:
Când încerci să adaugi o melodie cloud, primești eroarea:
```
Failed to add song: permission_denied: Missing or insufficient permissions
```

## ✅ Soluția:

Trebuie să actualizezi regulile Firestore pentru a permite utilizatorilor să adauge melodii cloud.

### Pași:

1. **Deschide Firebase Console**
   - Mergi la [https://console.firebase.google.com/](https://console.firebase.google.com/)
   - Selectează proiectul tău

2. **Mergi la Firestore Rules**
   - În meniul din stânga, click pe **Firestore Database**
   - Click pe tab-ul **Rules** (deasupra listei de colecții)

3. **Înlocuiește regulile existente** cu:
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
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Playlist-uri - utilizatorii pot citi/scrie DOAR propriile playlist-uri
    match /playlists/{playlistId} {
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```

4. **Publică regulile**
   - Click pe butonul **"Publish"** (sus, în dreapta)
   - Așteaptă confirmarea că regulile au fost publicate

5. **Testează din nou**
   - Reîncearcă să adaugi o melodie cloud în aplicație
   - Ar trebui să funcționeze acum! ✅

## 🔒 Ce fac aceste reguli:

- **`users`**: Utilizatorii pot citi/scrie doar propriile date de profil
- **`cloud_songs`**: 
  - Utilizatorii autentificați pot citi toate melodiile cloud
  - Utilizatorii pot crea doar melodii cu propriul lor `userId`
  - Utilizatorii pot actualiza/șterge doar propriile melodii
- **`playlists`**: 
  - Utilizatorii autentificați pot citi toate playlist-urile
  - Utilizatorii pot crea doar playlist-uri cu propriul lor `userId`
  - Utilizatorii pot actualiza/șterge doar propriile playlist-uri

## ⚠️ Dacă încă nu funcționează:

1. **Verifică că ești logat** în aplicație
2. **Verifică că regulile au fost publicate** (ar trebui să vezi "Rules published successfully")
3. **Așteaptă câteva secunde** - regulile pot dura puțin să se propage
4. **Reîncearcă aplicația** (închide și deschide din nou)

## 📝 Notă:

Dacă ai ales "Start in test mode" când ai creat Firestore, regulile ar trebui să permită totul pentru 30 de zile. Dacă ai trecut de acea perioadă sau ai configurat manual reguli restrictive, trebuie să le actualizezi conform instrucțiunilor de mai sus.
