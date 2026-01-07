package com.example.musicplayer;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class ArtistImageHelper {
    
    private static final Map<String, String> artistImageMap = new HashMap<>();
    
    static {
        // Map artist names to their drawable resource names
        // The drawable resource names should match files in res/drawable (e.g., omu_gnom.png)
        // Convert spaces and special characters to underscores and make lowercase
        
        // Add mappings for all artists
        // Format: artistName -> drawableResourceName (without extension)
        artistImageMap.put("Omu Gnom", "omu_gnom");
        artistImageMap.put("Deliric", "deliric");
        artistImageMap.put("Goo Goo Dolls", "goo_goo_dolls");
        artistImageMap.put("Linkin Park", "linkin_park");
        artistImageMap.put("Kings Of Leon", "kings_of_leon");
        artistImageMap.put("Byron", "byron");
        artistImageMap.put("Radiohead", "radiohead");
        artistImageMap.put("Petre Stefan", "petre_stefan");
        artistImageMap.put("Petre Stefan ", "petre_stefan"); // Handle trailing space
        artistImageMap.put("Deftones", "deftones");
        artistImageMap.put("The Kryptonite Sparks", "the_kryptonite_sparks");
        artistImageMap.put("Yungblud", "yungblud");
        artistImageMap.put("Ren", "ren");
        artistImageMap.put("Dave", "dave");
        artistImageMap.put("Sami G", "sami_g");
        artistImageMap.put("The Mono Jacks", "the_mono_jacks");
        artistImageMap.put("Iris", "iris");
        artistImageMap.put("Emaa", "emaa");
        artistImageMap.put("Cargo", "cargo");
        artistImageMap.put("Metallica", "metallica");
        artistImageMap.put("Vita de Vie", "vita_de_vie");
        artistImageMap.put("Vama", "vama");
        artistImageMap.put("Omul cu Sobolani", "omul_cu_sobolani");
        artistImageMap.put("Wheatus", "wheatus");
    }
    
    /**
     * Gets the drawable resource ID for an artist name
     * @param context The application context
     * @param artistName The name of the artist
     * @return The drawable resource ID, or a default placeholder if not found
     */
    public static int getArtistImageResource(Context context, String artistName) {
        if (artistName == null || context == null) {
            return android.R.drawable.ic_menu_gallery; // Default placeholder
        }
        
        // Normalize the artist name (trim and handle variations)
        String normalizedName = artistName.trim();
        String resourceName = artistImageMap.get(normalizedName);
        
        if (resourceName == null) {
            // Try to generate resource name from artist name
            resourceName = normalizedName.toLowerCase()
                    .replace(" ", "_")
                    .replace("'", "")
                    .replace(".", "")
                    .replace("-", "_");
        }
        
        // Use getIdentifier to get the resource ID dynamically
        int resId = context.getResources().getIdentifier(
                resourceName,
                "drawable",
                context.getPackageName()
        );
        
        // If resource not found, return default placeholder
        if (resId == 0) {
            return android.R.drawable.ic_menu_gallery;
        }
        
        return resId;
    }
    
    /**
     * Gets the drawable resource name (for reference/debugging)
     * @param artistName The name of the artist
     * @return The drawable resource name that should be used
     */
    public static String getArtistImageResourceName(String artistName) {
        if (artistName == null) {
            return "default";
        }
        
        String normalizedName = artistName.trim();
        String resourceName = artistImageMap.get(normalizedName);
        
        if (resourceName == null) {
            resourceName = normalizedName.toLowerCase()
                    .replace(" ", "_")
                    .replace("'", "")
                    .replace(".", "")
                    .replace("-", "_");
        }
        
        return resourceName;
    }
}

