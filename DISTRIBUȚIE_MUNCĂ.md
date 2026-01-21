# Distribuție Muncă - MusicPlayer Project

## Prezentare Generală
Acest document prezintă împărțirea echitabilă a muncii între cei 3 membri ai echipei pentru proiectul MusicPlayer Android.

---

## 👤 **Persoana 1: Autentificare și Gestionare Utilizatori**

### Responsabilități principale:
- **Sistem de autentificare complet**
- **Gestionare profil utilizator**
- **Integrare Firebase Authentication**

### Fișiere și funcționalități dezvoltate:

#### 1. **Autentificare (Login/SignUp)**
- `LoginActivity.java` - Ecran de login cu validare
- `SignUpActivity.java` - Ecran de înregistrare
- `FirebaseAuthHelper.java` - Helper pentru operațiuni Firebase Auth
  - Sign up cu email/parolă
  - Sign in cu email sau username
  - Validare credențiale
  - Gestionare erori

#### 2. **Gestionare Sesiune**
- `SessionManager.java` - Manager pentru sesiunea utilizatorului
  - Salvare/încărcare sesiune
  - Logout
  - Verificare stare autentificare

#### 3. **Profil Utilizator**
- `ProfileActivity.java` - Ecran complet de profil
  - Afișare informații utilizator (username, email)
  - Schimbare parolă
  - Upload/gestiune poză de profil (stocare locală)
  - Logout

#### 4. **Baza de Date - Utilizatori**
- `database/User.java` - Entitate utilizator
- `database/UserDao.java` - DAO pentru operațiuni utilizatori

### Funcționalități implementate:
✅ Login cu email sau username  
✅ Sign up cu validare  
✅ Schimbare parolă securizată  
✅ Upload și salvare poză de profil  
✅ Gestionare sesiune persistentă  
✅ Logout cu confirmare  

### Tehnologii folosite:
- Firebase Authentication
- Firebase Firestore (pentru date utilizator)
- Room Database (pentru stocare locală)
- SharedPreferences (pentru sesiune)

---

## 👤 **Persoana 2: Player Muzical și Interfață Principală**

### Responsabilități principale:
- **Motor de redare muzică**
- **Interfață player principal**
- **Gestionare playlist-uri de redare**
- **UI/UX pentru redare**

### Fișiere și funcționalități dezvoltate:

#### 1. **Motor de Redare**
- `MusicPlayerManager.java` - Manager central pentru redare
  - Redare melodii locale și cloud
  - Control play/pause/next/previous
  - Shuffle și Repeat
  - History stack pentru navigare înapoi
  - Listeneri pentru actualizare UI
  - Gestionare MediaPlayer

#### 2. **Interfață Player Principal**
- `MainActivity.java` - Ecran player complet
  - Afișare informații melodie (titlu, artist, album art)
  - SeekBar pentru navigare în melodie
  - Butoane control (play/pause, next, previous)
  - Shuffle și Repeat
  - Adăugare la playlist din player
  - Like/Unlike melodii
  - Swipe down pentru închidere

#### 3. **Interfață de Bază**
- `BaseActivity.java` - Activitate de bază pentru toate ecranele
  - Mini player persistent (bottom bar)
  - Bottom navigation
  - Gestionare temă (dark/light mode)
  - Funcționalități comune

#### 4. **UI Components**
- `OnSwipeTouchListener.java` - Gesturi swipe
- `ArtistImageHelper.java` - Gestionare imagini artiști
- Layout-uri XML pentru player și componente

### Funcționalități implementate:
✅ Redare melodii locale și cloud (streaming)  
✅ Control complet (play, pause, next, previous)  
✅ Shuffle și Repeat  
✅ History stack pentru navigare inteligentă  
✅ SeekBar cu sincronizare timp real  
✅ Mini player persistent în toate ecranele  
✅ Bottom navigation  
✅ Dark/Light mode  
✅ Swipe gestures  

### Tehnologii folosite:
- Android MediaPlayer
- Handler și Runnable pentru actualizări UI
- Glide pentru încărcare imagini
- Edge-to-edge UI

---

## 👤 **Persoana 3: Bibliotecă Muzicală și Organizare**

### Responsabilități principale:
- **Gestionare bibliotecă muzicală**
- **Sistem de playlist-uri**
- **Căutare și filtrare**
- **Cloud songs management**

### Fișiere și funcționalități dezvoltate:

#### 1. **Bibliotecă Muzicală**
- `MusicLibrary.java` - Manager pentru biblioteca muzicală
  - Inițializare baza de date cu melodii
  - Obținere toate melodiile
  - Filtrare pe artist
  - Filtrare pe gen
  - Integrare cu cloud songs

#### 2. **Ecrane de Navigare**
- `HomeActivity.java` - Ecran principal cu opțiuni
- `LibraryActivity.java` - Ecran bibliotecă completă
  - Afișare toate melodiile
  - Filtrare pe gen
  - Filtrare pe playlist
  - Adăugare melodii la playlist
  - Redare playlist
- `GenreActivity.java` - Ecran genuri muzicale
- `ArtistActivity.java` - Ecran artiști și melodiile lor
- `SearchActivity.java` - Căutare în bibliotecă

#### 3. **Gestionare Playlist-uri**
- `PlaylistsActivity.java` - Ecran playlist-uri utilizator
  - Afișare toate playlist-urile
  - Creare playlist nou
  - Redare playlist
  - Vizualizare melodii playlist
  - Adăugare melodii la playlist
- `PlaylistHelper.java` - Helper pentru operațiuni playlist
  - Obținere melodii din playlist
  - Gestionare playlist-uri locale și cloud
- `Playlist.java` - Model pentru playlist
- `LikedSongsHelper.java` - Gestionare melodii apreciate

#### 4. **Cloud Songs**
- `CloudSongManager.java` - Manager pentru melodii cloud
  - Adăugare melodii din cloud (Google Drive, Dropbox)
  - Obținere melodii cloud utilizator
  - Ștergere melodii cloud
  - Conversie link-uri share în link-uri directe

#### 5. **Baza de Date**
- `database/AppDatabase.java` - Baza de date Room
- `database/SongEntity.java` - Entitate melodie
- `database/SongDao.java` - DAO pentru operațiuni melodii

### Funcționalități implementate:
✅ Bibliotecă completă cu 67+ melodii pre-instalate  
✅ Filtrare pe genuri muzicale  
✅ Filtrare pe artiști  
✅ Căutare în timp real  
✅ Creare și gestionare playlist-uri personalizate  
✅ Adăugare melodii la playlist-uri  
✅ Playlist "Liked Songs" automat  
✅ Adăugare melodii din cloud (Google Drive, Dropbox)  
✅ Gestionare melodii cloud și locale în același sistem  
✅ Baza de date locală cu Room  

### Tehnologii folosite:
- Room Database (SQLite)
- Firebase Firestore (pentru playlist-uri și cloud songs)
- RecyclerView/ListView pentru liste
- Custom adapters

---

## 📊 **Statistici Contribuții**

### Linii de cod (aproximativ):
- **Persoana 1**: ~800-900 linii (Auth + Profile)
- **Persoana 2**: ~600-700 linii (Player + UI)
- **Persoana 3**: ~1200-1300 linii (Library + Playlists + Cloud)

### Fișiere Java:
- **Persoana 1**: 5-6 fișiere principale
- **Persoana 2**: 4-5 fișiere principale
- **Persoana 3**: 8-9 fișiere principale

### Complexitate:
- **Persoana 1**: Medie (Firebase integration, security)
- **Persoana 2**: Medie-Înaltă (MediaPlayer, threading, UI sync)
- **Persoana 3**: Înaltă (Database, complex data management, cloud integration)

---

## 🎯 **Puncte Cheie pentru Prezentare**

### Persoana 1 - Autentificare:
- "Am implementat sistemul complet de autentificare, permițând utilizatorilor să se înregistreze și să se autentifice securizat folosind Firebase."
- "Am dezvoltat interfața de profil unde utilizatorii pot gestiona contul lor, schimba parola și încărca o poză de profil."
- "Am implementat gestionarea sesiunii pentru a menține utilizatorii autentificați între sesiuni."

### Persoana 2 - Player:
- "Am construit motorul de redare muzică care suportă atât melodii locale cât și streaming din cloud."
- "Am dezvoltat interfața completă a player-ului cu controale avansate precum shuffle, repeat și history stack."
- "Am implementat mini player-ul persistent care permite utilizatorilor să controleze muzica din orice ecran al aplicației."

### Persoana 3 - Bibliotecă:
- "Am creat sistemul complet de bibliotecă muzicală cu peste 67 de melodii pre-instalate și funcții de filtrare."
- "Am implementat sistemul de playlist-uri personalizate cu suport pentru melodii locale și cloud."
- "Am dezvoltat funcționalitatea de cloud songs care permite utilizatorilor să adauge propriile melodii din servicii precum Google Drive."

---

## 📝 **Note pentru Prezentare**

1. **Fiecare persoană** poate demonstra funcționalitățile pe care le-a implementat
2. **Integrarea** între modulele fiecăruia este evidențiată (ex: player folosește biblioteca, playlist-urile folosesc autentificarea)
3. **Tehnologiile** folosite de fiecare sunt diferite și complementare
4. **Complexitatea** este distribuită echitabil, fiecare având provocări tehnice specifice

---

## 🔗 **Dependențe și Integrare**

- **Persoana 1** → Furnizează autentificare pentru Persoana 2 și 3
- **Persoana 2** → Folosește biblioteca de la Persoana 3 pentru redare
- **Persoana 3** → Folosește autentificarea de la Persoana 1 pentru playlist-uri cloud

---

*Document creat pentru evidențierea contribuțiilor echitabile în proiectul MusicPlayer.*
