package com.example.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    public void createLoginSession(String userId, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }
    
    public boolean isLoggedIn() {
        // Verifică atât SharedPreferences cât și Firebase Auth
        boolean prefLoggedIn = pref.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean firebaseLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        return prefLoggedIn && firebaseLoggedIn;
    }
    
    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }
    
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }
    
    public void logout() {
        // Deconectează și din Firebase
        FirebaseAuth.getInstance().signOut();
        editor.clear();
        editor.commit();
    }
}
