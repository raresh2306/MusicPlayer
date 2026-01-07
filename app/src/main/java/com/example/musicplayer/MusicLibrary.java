package com.example.musicplayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicLibrary {
    private static List<Song> songList = new ArrayList<>();

    static {
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
        songList.add(new Song(R.raw.cateodata, "Cateodata", "Petre Stefan", "Romanian Hip-Hop"));
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

    public static List<Song> getSongList() {
        return songList;
    }

    public static List<Song> getSongsByArtist(String artist) {
        List<Song> artistSongs = new ArrayList<>();
        for (Song song : songList) {
            if (song.getArtist().trim().equalsIgnoreCase(artist.trim())) {
                artistSongs.add(song);
            }
        }
        return artistSongs;
    }

    public static List<Song> getSongsByGenre(String genre) {
        List<Song> genreSongs = new ArrayList<>();
        for (Song song : songList) {
            // Case-insensitive check (e.g., "Rock" matches "rock")
            if (song.getGenre().trim().equalsIgnoreCase(genre.trim())) {
                genreSongs.add(song);
            }
        }
        return genreSongs;
    }

    public static List<String> getUniqueArtists() {
        Set<String> artists = new HashSet<>();
        for (Song song : songList) {
            artists.add(song.getArtist().trim());
        }
        return new ArrayList<>(artists);
    }
}