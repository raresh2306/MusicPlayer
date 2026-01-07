package com.example.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView tvSongTitle, tvArtist;
    private ImageView albumArt;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat;

    private List<Song> songList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        albumArt = findViewById(R.id.albumArt);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);

        initSongList();
        if (!songList.isEmpty()) {
            setupMediaPlayer(currentIndex);
        }

        // Make artist name clickable to navigate to artist page
        tvArtist.setOnClickListener(v -> {
            if (!songList.isEmpty() && currentIndex < songList.size()) {
                String artistName = songList.get(currentIndex).getArtist();
                Intent intent = new Intent(MainActivity.this, ArtistActivity.class);
                intent.putExtra("ARTIST_NAME", artistName);
                startActivity(intent);
            }
        });

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNext());
        btnPrev.setOnClickListener(v -> playPrevious());
        
        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setAlpha(isShuffle ? 1.0f : 0.5f);
        });
        
        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setAlpha(isRepeat ? 1.0f : 0.5f);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateSeekBarRunnable, 1000);
    }

    private void initSongList() {
        songList.add(new Song(R.raw.al, "Al'", "Omu Gnom", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.gaia, "Gaia", "Deliric", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.iris, "Iris", "Goo Goo Dolls", "Rock"));
        songList.add(new Song(R.raw.numb, "Numb", "Linkin Park", "Rock"));
        songList.add(new Song(R.raw.pyro, "Pyro", "Kings Of Leon", "Alternative"));
        songList.add(new Song(R.raw.razi, "Razi", "Byron", "Romanian Rock"));
        songList.add(new Song(R.raw.creep, "Creep", "Radiohead", "Alternative"));
        songList.add(new Song(R.raw.scrum, "Scrum", "Petre Stefan", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.adrese, "Adrese", "Petre Stefan", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.change, "Change", "Deftones", "Alternative"));
        songList.add(new Song(R.raw.closer, "Closer", "Kings Of Leon", "Rock"));
        songList.add(new Song(R.raw.gollum, "Gollum", "Deliric", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.in_vis, "In vis", "The Kryptonite Sparks", "Romanian Alternative"));
        songList.add(new Song(R.raw.zombie, "Zombie", "Yungblud", "Alternative"));
        songList.add(new Song(R.raw.heretic, "Heretic", "Ren", "Alternative"));
        songList.add(new Song(R.raw.history, "History", "Dave", "Hip-Hop"));
        songList.add(new Song(R.raw.liniste, "Liniste", "Sami G", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.sextape, "Sextape", "Deftones", "Alternative"));
        songList.add(new Song(R.raw.diazepam, "Diazepam", "Ren", "Hip-Hop"));
        songList.add(new Song(R.raw.let_down, "Let Down", "Radiohead", "Alternative"));
        songList.add(new Song(R.raw.trecator, "Trecator", "Deliric", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.cateodata, "Cateodata", "Petre Stefan ", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.metehne_2, "Metehne 2", "Omu Gnom", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.nemuritor, "Nemuritori", "The Mono Jacks", "Romanian Alternative"));
        songList.add(new Song(R.raw.raindance, "Raindance", "Dave", "Hip-Hop"));
        songList.add(new Song(R.raw.strada_ta, "Strada Ta", "Iris", "Romanian Rock"));
        songList.add(new Song(R.raw._1000_de_da, "1000 De Da", "The Mono Jacks", "Romanian Rock"));
        songList.add(new Song(R.raw._11_minutes, "11 Minutes", "Yungblud", "Alternative"));
        songList.add(new Song(R.raw.apa_si_cer, "Apa si Cer", "Byron", "Romanian Rock"));
        songList.add(new Song(R.raw.marvellous, "Marvellous", "Dave", "Hip-Hop"));
        songList.add(new Song(R.raw.somn_bizar, "Somn bizar", "Iris", "Romanian Rock"));
        songList.add(new Song(R.raw.zburatorul, "Zburatorul", "Emaa", "Romanian Alternative"));
        songList.add(new Song(R.raw.caleidoscop, "Caleidoscop", "The Mono Jacks", "Romanian Alternative"));
        songList.add(new Song(R.raw.daca_ploaia, "Daca Ploaia S-ar Opri", "Cargo", "Romanian Rock"));
        songList.add(new Song(R.raw.sex_on_fire, "Sex On Fire", "Kings Of Leon", "Rock"));
        songList.add(new Song(R.raw.de_vei_pleca, "De Vei Pleca", "Iris", "Romanian Rock"));
        songList.add(new Song(R.raw.fara_cuvinte, "Fara Cuvinte", "Byron", "Romanian Alternative"));
        songList.add(new Song(R.raw.use_somebody, "Use Somebody", "Kings Of Leon", "Rock"));
        songList.add(new Song(R.raw.cartierul_meu, "Cartierul Meu", "Petre Stefan", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.enter_sandman, "Enter Sandman", "Metallica", "Rock"));
        songList.add(new Song(R.raw.fragede_idile, "Fragede Idile", "The Kryptonite Sparks", "Romanian Alternative"));
        songList.add(new Song(R.raw.praf_de_stele, "Praf de Stele", "Vita de Vie", "Romanian Rock"));
        songList.add(new Song(R.raw.twenty_to_one, "Twenty to One", "Dave", "Hip-Hop"));
        songList.add(new Song(R.raw.chalk_outlines, "Chalk Outlines", "Ren", "Hip-Hop"));
        songList.add(new Song(R.raw.floare_de_iris, "Floare de iris", "Iris", "Romanian Rock"));
        songList.add(new Song(R.raw.granita_ranita, "Granita-n ranita", "Byron", "Romanian Rock"));
        songList.add(new Song(R.raw.the_unforgiven, "The unforgiven", "Metallica", "Rock"));
        songList.add(new Song(R.raw.undeva_in_vama, "Undeva in Vama", "Vama", "Romanian Alternative"));
        songList.add(new Song(R.raw.doua_beri_goale, "Doua beri goale", "Omul cu Sobolani", "Romanian Alternative"));
        songList.add(new Song(R.raw.how_i_met_my_ex, "How I Met My Ex", "Dave", "Hip-Hop"));
        songList.add(new Song(R.raw.slabiciunea_mea, "Slabiciunea mea", "Sami G", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.teenage_dirtbag, "Teenage dirtbag", "Wheatus", "Alternative"));
        songList.add(new Song(R.raw.in_fiecare_seara, "In fiecare seara", "Emaa", "Romanian Alternative"));
        songList.add(new Song(R.raw.oda_piata_romana, "Oda (piata Romana)", "Omul cu Sobolani", "Romanian Alternative"));
        songList.add(new Song(R.raw.the_unforgiven_2, "The unforgiven 2", "Metallica", "Rock"));
        songList.add(new Song(R.raw.emptiness_machine, "The Emptiness Machine", "Linkin Park", "Rock"));
        songList.add(new Song(R.raw.in_jurul_soarelui, "In jurul soarelui", "Deliric", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.sa_nu_crezi_nimic, "Sa nu crezi nimic", "Iris", "Romanian Rock"));
        songList.add(new Song(R.raw.sper_ca_esti_bine, "Sper ca esti bine", "Sami G", "Romanian Hip-Hop"));
        songList.add(new Song(R.raw.metafora_de_toamna, "Metafora de toamna", "Vita de Vie", "Romanian Rock"));
        songList.add(new Song(R.raw.ziua_vrajitoarelor, "Ziua vrajitoarelor", "Cargo", "Romanian Rock"));
        songList.add(new Song(R.raw.consumatori_de_vise, "Consumatori de vise", "Byron", "Romanian Rock"));
        songList.add(new Song(R.raw.nothing_else_matters, "Nothing else matters", "Metallica", "Rock"));
        songList.add(new Song(R.raw.si_golanii_beau_ceai, "Si golanii beau ceai", "The Kryptonite Sparks", "Romanian Alternative"));
        songList.add(new Song(R.raw.ploaie_in_luna_lui_marte, "Ploaie in luna lui marte", "Byron", "Romanian Alternative"));
        songList.add(new Song(R.raw.i_was_made_for_loving_you, "I was made for loving you", "Yungblud", "Alternative"));
        songList.add(new Song(R.raw.nimic_nu_se_compara_cu_bucuresti, "Nimic nu se compara cu bucuresti", "Omu Gnom", "Romanian Hip-Hop"));
    }

    private void setupMediaPlayer(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        Song currentSong = songList.get(index);
        mediaPlayer = MediaPlayer.create(this, currentSong.getResId());
        
        tvSongTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());
        
        // Set artist image
        int artistImageRes = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
        albumArt.setImageResource(artistImageRes);
        
        seekBar.setMax(mediaPlayer.getDuration());
        
        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeat) {
                mediaPlayer.start();
            } else {
                playNext();
            }
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void playNext() {
        if (songList.isEmpty()) return;
        
        if (isShuffle) {
            currentIndex = new Random().nextInt(songList.size());
        } else {
            currentIndex = (currentIndex + 1) % songList.size();
        }
        setupMediaPlayer(currentIndex);
        mediaPlayer.start();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void playPrevious() {
        if (songList.isEmpty()) return;
        
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        setupMediaPlayer(currentIndex);
        mediaPlayer.start();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }
}
