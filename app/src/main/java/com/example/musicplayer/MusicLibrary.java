package com.example.musicplayer;

import android.content.Context;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.SongDao;
import com.example.musicplayer.database.SongEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MusicLibrary {
    private static boolean initialized = false;

    // Initialize database with songs if empty
    public static void initializeDatabase(Context context) {
        if (initialized) return;

        try {
            AppDatabase database = AppDatabase.getInstance(context);
            SongDao songDao = database.songDao();

            if (songDao.getSongCount() == 0) {
                List<SongEntity> songs = new ArrayList<>();
                songs.add(new SongEntity(R.raw.al, "Al'", "Omu Gnom", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.gaia, "Gaia", "Deliric", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.iris, "Iris", "Goo Goo Dolls", "Rock"));
                songs.add(new SongEntity(R.raw.numb, "Numb", "Linkin Park", "Rock"));
                songs.add(new SongEntity(R.raw.pyro, "Pyro", "Kings Of Leon", "Alternative"));
                songs.add(new SongEntity(R.raw.razi, "Razi", "Byron", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.creep, "Creep", "Radiohead", "Alternative"));
                songs.add(new SongEntity(R.raw.scrum, "Scrum", "Petre Stefan", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.adrese, "Adrese", "Petre Stefan", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.change, "Change", "Deftones", "Alternative"));
                songs.add(new SongEntity(R.raw.closer, "Closer", "Kings Of Leon", "Rock"));
                songs.add(new SongEntity(R.raw.gollum, "Gollum", "Deliric", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.in_vis, "In vis", "The Kryptonite Sparks", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.zombie, "Zombie", "Yungblud", "Alternative"));
                songs.add(new SongEntity(R.raw.heretic, "Heretic", "Ren", "Alternative"));
                songs.add(new SongEntity(R.raw.history, "History", "Dave", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.liniste, "Liniste", "Sami G", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.sextape, "Sextape", "Deftones", "Alternative"));
                songs.add(new SongEntity(R.raw.diazepam, "Diazepam", "Ren", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.let_down, "Let Down", "Radiohead", "Alternative"));
                songs.add(new SongEntity(R.raw.trecator, "Trecator", "Deliric", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.cateodata, "Cateodata", "Petre Stefan", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.metehne_2, "Metehne 2", "Omu Gnom", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.nemuritor, "Nemuritori", "The Mono Jacks", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.raindance, "Raindance", "Dave", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.strada_ta, "Strada Ta", "Iris", "Romanian Rock"));
                songs.add(new SongEntity(R.raw._1000_de_da, "1000 De Da", "The Mono Jacks", "Romanian Rock"));
                songs.add(new SongEntity(R.raw._11_minutes, "11 Minutes", "Yungblud", "Alternative"));
                songs.add(new SongEntity(R.raw.apa_si_cer, "Apa si Cer", "Byron", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.marvellous, "Marvellous", "Dave", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.somn_bizar, "Somn bizar", "Iris", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.zburatorul, "Zburatorul", "Emaa", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.caleidoscop, "Caleidoscop", "The Mono Jacks", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.daca_ploaia, "Daca Ploaia S-ar Opri", "Cargo", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.sex_on_fire, "Sex On Fire", "Kings Of Leon", "Rock"));
                songs.add(new SongEntity(R.raw.de_vei_pleca, "De Vei Pleca", "Iris", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.fara_cuvinte, "Fara Cuvinte", "Byron", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.use_somebody, "Use Somebody", "Kings Of Leon", "Rock"));
                songs.add(new SongEntity(R.raw.cartierul_meu, "Cartierul Meu", "Petre Stefan", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.enter_sandman, "Enter Sandman", "Metallica", "Rock"));
                songs.add(new SongEntity(R.raw.fragede_idile, "Fragede Idile", "The Kryptonite Sparks", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.praf_de_stele, "Praf de Stele", "Vita de Vie", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.twenty_to_one, "Twenty to One", "Dave", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.chalk_outlines, "Chalk Outlines", "Ren", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.floare_de_iris, "Floare de iris", "Iris", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.granita_ranita, "Granita-n ranita", "Byron", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.the_unforgiven, "The unforgiven", "Metallica", "Rock"));
                songs.add(new SongEntity(R.raw.undeva_in_vama, "Undeva in Vama", "Vama", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.doua_beri_goale, "Doua beri goale", "Omul cu Sobolani", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.how_i_met_my_ex, "How I Met My Ex", "Dave", "Hip-Hop"));
                songs.add(new SongEntity(R.raw.slabiciunea_mea, "Slabiciunea mea", "Sami G", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.teenage_dirtbag, "Teenage dirtbag", "Wheatus", "Alternative"));
                songs.add(new SongEntity(R.raw.in_fiecare_seara, "In fiecare seara", "Emaa", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.oda_piata_romana, "Oda (piata Romana)", "Omul cu Sobolani", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.the_unforgiven_2, "The unforgiven 2", "Metallica", "Rock"));
                songs.add(new SongEntity(R.raw.emptiness_machine, "The Emptiness Machine", "Linkin Park", "Rock"));
                songs.add(new SongEntity(R.raw.in_jurul_soarelui, "In jurul soarelui", "Deliric", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.sa_nu_crezi_nimic, "Sa nu crezi nimic", "Iris", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.sper_ca_esti_bine, "Sper ca esti bine", "Sami G", "Romanian Hip-Hop"));
                songs.add(new SongEntity(R.raw.metafora_de_toamna, "Metafora de toamna", "Vita de Vie", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.ziua_vrajitoarelor, "Ziua vrajitoarelor", "Cargo", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.consumatori_de_vise, "Consumatori de vise", "Byron", "Romanian Rock"));
                songs.add(new SongEntity(R.raw.nothing_else_matters, "Nothing else matters", "Metallica", "Rock"));
                songs.add(new SongEntity(R.raw.si_golanii_beau_ceai, "Si golanii beau ceai", "The Kryptonite Sparks", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.ploaie_in_luna_lui_marte, "Ploaie in luna lui marte", "Byron", "Romanian Alternative"));
                songs.add(new SongEntity(R.raw.i_was_made_for_loving_you, "I was made for loving you", "Yungblud", "Alternative"));
                songs.add(new SongEntity(R.raw.nimic_nu_se_compara_cu_bucuresti, "Nimic nu se compara cu bucuresti", "Omu Gnom", "Romanian Hip-Hop"));

                songDao.insertAllSongs(songs);
            }
            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            initialized = true;
        }
    }

    private static Song convertToSong(SongEntity entity) {
        if (entity == null) return null;
        try {
            if (entity.isCloudSong() && entity.getCloudUrl() != null && !entity.getCloudUrl().isEmpty()) {
                Song song;
                String coverUrl = entity.getCoverImageUrl();
                if (coverUrl != null && !coverUrl.trim().isEmpty()) {
                    song = new Song(entity.getCloudUrl(),
                            entity.getTitle() != null ? entity.getTitle() : "Unknown",
                            entity.getArtist() != null ? entity.getArtist() : "Unknown",
                            entity.getGenre() != null ? entity.getGenre() : "Unknown",
                            coverUrl);
                } else {
                    song = new Song(entity.getCloudUrl(),
                            entity.getTitle() != null ? entity.getTitle() : "Unknown",
                            entity.getArtist() != null ? entity.getArtist() : "Unknown",
                            entity.getGenre() != null ? entity.getGenre() : "Unknown");
                }
                return song;
            } else {
                return new Song(entity.getResId(),
                        entity.getTitle() != null ? entity.getTitle() : "Unknown",
                        entity.getArtist() != null ? entity.getArtist() : "Unknown",
                        entity.getGenre() != null ? entity.getGenre() : "Unknown");
            }
        } catch (Exception e) {
            return new Song(0, "Unknown", "Unknown", "Unknown");
        }
    }

    // Returnează DOAR melodiile locale sincron (pentru uz intern sau fallback)
    public static List<Song> getSongList(Context context) {
        try {
            initializeDatabase(context);
            AppDatabase database = AppDatabase.getInstance(context);
            List<SongEntity> entities = database.songDao().getAllSongs();
            List<Song> songs = new ArrayList<>();
            for (SongEntity entity : entities) {
                Song song = convertToSong(entity);
                if (song != null) songs.add(song);
            }
            return songs;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // METODA NOUĂ SIGURĂ: Încarcă TOT (Local + Cloud) și returnează prin Callback
    public static void getAllSongs(Context context, OnSongsLoadedListener callback) {
        // 1. Încarcă local
        List<Song> allSongs = getSongList(context);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            callback.onSongsLoaded(allSongs);
            return;
        }

        // 2. Încarcă cloud
        FirebaseFirestore.getInstance().collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String cloudUrl = doc.getString("cloudUrl");
                        String title = doc.getString("title");
                        String artist = doc.getString("artist");
                        String genre = doc.getString("genre");
                        String coverImageUrl = doc.getString("coverImageUrl");

                        if (cloudUrl != null && title != null) {
                            Song cloudSong;
                            if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                                cloudSong = new Song(cloudUrl, title,
                                        artist != null ? artist : "Unknown",
                                        genre != null ? genre : "Unknown",
                                        coverImageUrl);
                            } else {
                                cloudSong = new Song(cloudUrl, title,
                                        artist != null ? artist : "Unknown",
                                        genre != null ? genre : "Unknown");
                            }
                            allSongs.add(cloudSong);
                        }
                    }
                    // Returnează lista completă DOAR când e gata totul
                    callback.onSongsLoaded(allSongs);
                })
                .addOnFailureListener(e -> {
                    // Dacă eșuează cloud-ul, dăm măcar ce avem local
                    callback.onSongsLoaded(allSongs);
                });
    }

    public static List<Song> getSongsByArtist(Context context, String artist) {
        initializeDatabase(context);
        AppDatabase database = AppDatabase.getInstance(context);
        List<SongEntity> entities = database.songDao().getSongsByArtist(artist);
        List<Song> songs = new ArrayList<>();
        for (SongEntity entity : entities) {
            songs.add(convertToSong(entity));
        }
        return songs;
    }

    public static List<Song> getSongsByGenre(Context context, String genre) {
        initializeDatabase(context);
        AppDatabase database = AppDatabase.getInstance(context);
        List<SongEntity> entities = database.songDao().getSongsByGenre(genre);
        List<Song> songs = new ArrayList<>();
        for (SongEntity entity : entities) {
            songs.add(convertToSong(entity));
        }
        return songs;
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
    }
}