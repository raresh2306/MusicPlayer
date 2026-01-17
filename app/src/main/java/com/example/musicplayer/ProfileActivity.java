package com.example.musicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.File;
import java.io.FileOutputStream;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends BaseActivity {
    private ImageView ivProfilePicture;
    private TextView tvUsername, tvEmail;
    private Button btnChangePassword, btnChangeProfilePicture, btnCreatePlaylist, btnAddCloudSong, btnLogout;
    private ListView lvPlaylists;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private List<Playlist> playlists;
    private PlaylistAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        playlists = new ArrayList<>();

        initViews();
        loadUserData();
        loadPlaylists();
        setupListeners();
        setupMiniPlayer();
        setupBottomNavigation();
    }

    private void initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangeProfilePicture = findViewById(R.id.btnChangeProfilePicture);
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        btnAddCloudSong = findViewById(R.id.btnAddCloudSong);
        btnLogout = findViewById(R.id.btnLogout);
        lvPlaylists = findViewById(R.id.lvPlaylists);

        adapter = new PlaylistAdapter(playlists);
        lvPlaylists.setAdapter(adapter);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            
            // Obține username-ul și poza de profil din Firestore
            db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        String username = doc.getString("username");
                        if (username != null) {
                            tvUsername.setText(username);
                        }
                        
                        // Încarcă poza de profil din stocare locală
                        loadLocalProfilePicture();
                    }
                });
        }
    }
    
    private void loadLocalProfilePicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            ivProfilePicture.setImageResource(R.drawable.ic_account);
            return;
        }
        
        try {
            File imageFile = new File(getFilesDir(), "profile_picture_" + user.getUid() + ".jpg");
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    // Creează o imagine circulară
                    int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                    Bitmap circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(circularBitmap);
                    android.graphics.Paint paint = new android.graphics.Paint();
                    paint.setAntiAlias(true);
                    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
                    paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(bitmap, 0, 0, paint);
                    ivProfilePicture.setImageBitmap(circularBitmap);
                } else {
                    ivProfilePicture.setImageResource(R.drawable.ic_account);
                }
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_account);
            }
        } catch (Exception e) {
            ivProfilePicture.setImageResource(R.drawable.ic_account);
        }
    }

    private void loadPlaylists() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    playlists.clear();
                    boolean hasLikedSongs = false;
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Playlist playlist = new Playlist();
                        playlist.setId(document.getId());
                        playlist.setName(document.getString("name"));
                        playlist.setUserId(document.getString("userId"));
                        
                        // Verifică dacă există deja "Liked Songs"
                        if ("Liked Songs".equals(playlist.getName())) {
                            hasLikedSongs = true;
                        }
                        
                        @SuppressWarnings("unchecked")
                        List<String> songIds = (List<String>) document.get("songIds");
                        if (songIds != null) {
                            playlist.setSongIds(songIds);
                        }
                        
                        @SuppressWarnings("unchecked")
                        List<String> cloudSongIds = (List<String>) document.get("cloudSongIds");
                        if (cloudSongIds != null) {
                            playlist.setCloudSongIds(cloudSongIds);
                        }
                        
                        Long createdAt = document.getLong("createdAt");
                        if (createdAt != null) {
                            playlist.setCreatedAt(createdAt);
                        }
                        
                        playlists.add(playlist);
                    }
                    
                    // Dacă nu există "Liked Songs", îl creează automat
                    if (!hasLikedSongs) {
                        createLikedSongsPlaylistIfNeeded(userId);
                    }
                    
                    adapter.notifyDataSetChanged();
                }
            });
    }
    
    private void createLikedSongsPlaylistIfNeeded(String userId) {
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", "Liked Songs");
        playlist.put("userId", userId);
        playlist.put("songIds", new ArrayList<String>());
        playlist.put("cloudSongIds", new ArrayList<String>());
        playlist.put("createdAt", System.currentTimeMillis());
        
        db.collection("playlists")
            .add(playlist)
            .addOnSuccessListener(documentReference -> {
                // Reîncarcă playlist-urile după creare
                loadPlaylists();
            });
    }

    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnChangeProfilePicture.setOnClickListener(v -> selectProfilePicture());
        ivProfilePicture.setOnClickListener(v -> selectProfilePicture());
        btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
        btnAddCloudSong.setOnClickListener(v -> showAddCloudSongDialog());
        btnLogout.setOnClickListener(v -> performLogout());

        lvPlaylists.setOnItemClickListener((parent, view, position, id) -> {
            Playlist playlist = playlists.get(position);
            showPlaylistOptionsDialog(playlist);
        });
    }
    
    private void selectProfilePicture() {
        // Verifică permisiuni
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES}, 
                PERMISSION_REQUEST_CODE);
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadProfilePicture(imageUri);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectProfilePicture();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void uploadProfilePicture(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Citește imaginea
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot read image file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) {
                Toast.makeText(this, "Cannot decode image", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Redimensionează imaginea pentru a economisi spațiu (max 500x500)
            int maxSize = 500;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float scale = Math.min((float) maxSize / width, (float) maxSize / height);
                int newWidth = Math.round(width * scale);
                int newHeight = Math.round(height * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            
            // Salvează imaginea local
            String userId = user.getUid();
            File imageFile = new File(getFilesDir(), "profile_picture_" + userId + ".jpg");
            
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            
            // Actualizează UI-ul
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            loadLocalProfilePicture();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Re-autentifică utilizatorul pentru a schimba parola
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    // Re-autentifică cu parola curentă
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(user.getEmail(), currentPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Acum schimbă parola
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            String error = updateTask.getException() != null ?
                                                updateTask.getException().getMessage() : "Failed to change password";
                                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            } else {
                                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
        });

        dialog.show();
    }

    private void showCreatePlaylistDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_playlist, null);
        TextInputEditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Create New Playlist")
            .setView(dialogView)
            .setPositiveButton("Create", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String playlistName = etPlaylistName.getText().toString().trim();
                if (playlistName.isEmpty()) {
                    Toast.makeText(this, "Please enter a playlist name", Toast.LENGTH_SHORT).show();
                    return;
                }

                createPlaylist(playlistName);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createPlaylist(String name) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", name);
        playlist.put("userId", userId);
        playlist.put("songIds", new ArrayList<String>());
        playlist.put("cloudSongIds", new ArrayList<String>());
        playlist.put("createdAt", System.currentTimeMillis());

        db.collection("playlists")
            .add(playlist)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Playlist created!", Toast.LENGTH_SHORT).show();
                    loadPlaylists(); // Reîncarcă lista
                } else {
                    Toast.makeText(this, "Failed to create playlist", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void deletePlaylist(Playlist playlist) {
        if (playlist.getId() == null) return;

        db.collection("playlists").document(playlist.getId())
            .delete()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                    loadPlaylists(); // Reîncarcă lista
                } else {
                    Toast.makeText(this, "Failed to delete playlist", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showPlaylistOptionsDialog(Playlist playlist) {
        String[] options = {"Play Playlist", "Edit Songs", "Delete Playlist"};
        
        new AlertDialog.Builder(this)
            .setTitle(playlist.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Play Playlist
                        playPlaylist(playlist);
                        break;
                    case 1: // Edit Songs
                        showPlaylistSongsDialog(playlist);
                        break;
                    case 2: // Delete Playlist
                        deletePlaylist(playlist);
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void playPlaylist(Playlist playlist) {
        if (playlist.getSongCount() == 0) {
            Toast.makeText(this, "Playlist is empty. Add some songs first!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        PlaylistHelper.getPlaylistSongs(this, playlist, new PlaylistHelper.OnPlaylistSongsLoaded() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (songs.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "No songs found in playlist", Toast.LENGTH_SHORT).show();
                } else {
                    // Redă playlist-ul
                    MusicPlayerManager.getInstance().playSong(ProfileActivity.this, songs, 0);
                    Toast.makeText(ProfileActivity.this, "Playing: " + playlist.getName(), Toast.LENGTH_SHORT).show();
                    // Deschide player-ul
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistSongsDialog(Playlist playlist) {
        // Obține toate melodiile disponibile
        List<Song> allSongs = MusicLibrary.getSongList(this);
        
        // Creează lista de nume de melodii pentru dialog
        String[] songNames = new String[allSongs.size()];
        boolean[] checkedSongs = new boolean[allSongs.size()];
        
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            songNames[i] = song.getTitle() + " - " + song.getArtist();
            // Verifică dacă melodia este deja în playlist
            checkedSongs[i] = playlist.getSongIds().contains(String.valueOf(song.getResId()));
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add songs to: " + playlist.getName())
            .setMultiChoiceItems(songNames, checkedSongs, (dialog, which, isChecked) -> {
                checkedSongs[which] = isChecked;
            })
            .setPositiveButton("Save", (dialog, which) -> {
                // Actualizează playlist-ul cu melodiile selectate
                List<String> selectedSongIds = new ArrayList<>();
                for (int i = 0; i < checkedSongs.length; i++) {
                    if (checkedSongs[i]) {
                        selectedSongIds.add(String.valueOf(allSongs.get(i).getResId()));
                    }
                }
                updatePlaylistSongs(playlist, selectedSongIds);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updatePlaylistSongs(Playlist playlist, List<String> songIds) {
        if (playlist.getId() == null) return;
        
        db.collection("playlists").document(playlist.getId())
            .update("songIds", songIds)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Playlist updated!", Toast.LENGTH_SHORT).show();
                    loadPlaylists(); // Reîncarcă lista
                } else {
                    Toast.makeText(this, "Failed to update playlist", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showAddCloudSongDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_cloud_song, null);
        TextInputEditText etCloudUrl = dialogView.findViewById(R.id.etCloudUrl);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etArtist = dialogView.findViewById(R.id.etArtist);
        TextInputEditText etGenre = dialogView.findViewById(R.id.etGenre);
        TextInputEditText etCoverImageUrl = dialogView.findViewById(R.id.etCoverImageUrl);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Add Cloud Song")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String cloudUrl = etCloudUrl.getText().toString().trim();
                String title = etTitle.getText().toString().trim();
                String artist = etArtist.getText().toString().trim();
                String genre = etGenre.getText().toString().trim();
                String coverImageUrl = etCoverImageUrl.getText().toString().trim();

                if (cloudUrl.isEmpty() || title.isEmpty()) {
                    Toast.makeText(this, "URL and Title are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!cloudUrl.startsWith("http://") && !cloudUrl.startsWith("https://")) {
                    Toast.makeText(this, "Please enter a valid URL (starting with http:// or https://)", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert Google Drive share link to direct download link
                String directUrl = convertToDirectDownloadUrl(cloudUrl);
                String directCoverUrl = coverImageUrl.isEmpty() ? null : convertToDirectDownloadUrl(coverImageUrl);

                CloudSongManager manager = new CloudSongManager(this);
                manager.addCloudSong(directUrl, title, 
                    artist.isEmpty() ? "Unknown" : artist, 
                    genre.isEmpty() ? "Unknown" : genre,
                    directCoverUrl,
                    new CloudSongManager.OnCloudSongAdded() {
                        @Override
                        public void onSuccess(String songId) {
                            Toast.makeText(ProfileActivity.this, "Cloud song added!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
            });
        });

        dialog.show();
    }

    /**
     * Convertește link-uri de share (Google Drive, Dropbox) în link-uri directe de download
     */
    private String convertToDirectDownloadUrl(String url) {
        // Google Drive share link -> direct download
        if (url.contains("drive.google.com/file/d/")) {
            String fileId = url.substring(url.indexOf("/d/") + 3);
            if (fileId.contains("/")) {
                fileId = fileId.substring(0, fileId.indexOf("/"));
            }
            return "https://drive.google.com/uc?export=download&id=" + fileId;
        }
        
        // Dropbox share link -> direct download
        if (url.contains("dropbox.com/s/")) {
            if (!url.contains("?dl=1")) {
                url = url.replace("?dl=0", "?dl=1");
                if (!url.contains("?dl=1")) {
                    url += (url.contains("?") ? "&" : "?") + "dl=1";
                }
            }
            return url;
        }
        
        // OneDrive share link -> direct download
        if (url.contains("onedrive.live.com") || url.contains("1drv.ms")) {
            // OneDrive necesită conversie specială, pentru moment returnăm URL-ul original
            // Utilizatorul trebuie să obțină link-ul direct manual
        }
        
        // Pentru alte servicii, returnăm URL-ul original
        return url;
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                // Oprește melodia care rulează
                MusicPlayerManager.getInstance().stop();
                
                sessionManager.logout();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private class PlaylistAdapter extends BaseAdapter {
        private List<Playlist> playlists;

        public PlaylistAdapter(List<Playlist> playlists) {
            this.playlists = playlists;
        }

        @Override
        public int getCount() {
            return playlists.size();
        }

        @Override
        public Object getItem(int position) {
            return playlists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ProfileActivity.this)
                    .inflate(R.layout.item_playlist, parent, false);
            }

            Playlist playlist = playlists.get(position);
            TextView tvName = convertView.findViewById(R.id.tvPlaylistName);
            TextView tvCount = convertView.findViewById(R.id.tvPlaylistSongCount);
            ImageButton btnDelete = convertView.findViewById(R.id.btnDeletePlaylist);
            ImageView ivPlaylistImage = convertView.findViewById(R.id.ivPlaylistImage);

            tvName.setText(playlist.getName());
            tvCount.setText(playlist.getSongCount() + " songs");

            // Setează imaginea default pentru "Liked Songs"
            if ("Liked Songs".equals(playlist.getName())) {
                ivPlaylistImage.setImageResource(R.drawable.ic_liked_songs);
                ivPlaylistImage.setBackgroundTintList(null); // Elimină background tint pentru heart
                // Ascunde butonul de delete pentru "Liked Songs" (nu poate fi șters)
                btnDelete.setVisibility(View.GONE);
            } else {
                ivPlaylistImage.setImageResource(R.drawable.ic_playlist);
                ivPlaylistImage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1DB954")));
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Delete Playlist")
                        .setMessage("Are you sure you want to delete \"" + playlist.getName() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> deletePlaylist(playlist))
                        .setNegativeButton("Cancel", null)
                        .show();
                });
            }

            return convertView;
        }
    }
}
