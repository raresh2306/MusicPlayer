package com.example.musicplayer;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    public interface AuthCallback {
        void onSuccess(String userId, String username);
        void onError(String errorMessage);
    }
    
    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Creează un cont nou cu email și parolă
     * Stochează username-ul în Firestore
     */
    public void signUp(String email, String password, String username, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Salvează username-ul în Firestore
                        saveUserData(user.getUid(), username, email, callback);
                    } else {
                        callback.onError("Failed to create user");
                    }
                } else {
                    String error = task.getException() != null ? 
                        task.getException().getMessage() : "Sign up failed";
                    callback.onError(error);
                }
            });
    }
    
    /**
     * Autentifică utilizatorul cu email și parolă
     */
    public void signIn(String email, String password, AuthCallback callback) {
        // Verificări de bază
        if (email == null || email.trim().isEmpty()) {
            callback.onError("Email cannot be empty");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            callback.onError("Password cannot be empty");
            return;
        }

        mAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Obține username-ul din Firestore
                        getUserData(user.getUid(), callback);
                    } else {
                        callback.onError("User not found");
                    }
                } else {
                    Exception exception = task.getException();
                    String error;
                    if (exception != null) {
                        String errorMsg = exception.getMessage();
                        if (errorMsg != null) {
                            if (errorMsg.contains("password") || errorMsg.contains("credential")) {
                                error = "Incorrect password. Please check your password and try again.";
                            } else if (errorMsg.contains("user") || errorMsg.contains("not found")) {
                                error = "No account found with this email. Please check your email or sign up.";
                            } else {
                                error = "Login failed: " + errorMsg;
                            }
                        } else {
                            error = "Sign in failed. Please try again.";
                        }
                    } else {
                        error = "Sign in failed. Please try again.";
                    }
                    callback.onError(error);
                }
            });
    }
    
    /**
     * Autentifică utilizatorul cu username sau email și parolă
     * Detectează automat dacă input-ul este email sau username
     */
    public void signInWithUsername(String usernameOrEmail, String password, AuthCallback callback) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            callback.onError("Username or email cannot be empty");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            callback.onError("Password cannot be empty");
            return;
        }

        String input = usernameOrEmail.trim();
        
        // Verifică dacă input-ul este un email (conține @)
        if (input.contains("@")) {
            // Este email, folosește direct signIn
            signIn(input, password, callback);
        } else {
            // Este username, caută email-ul asociat în Firestore
            // Folosim o abordare mai robustă: obținem toți utilizatorii și căutăm local
            // pentru a evita problemele cu index-urile Firestore
            Log.d(TAG, "Searching for username: " + input);
            db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean found = false;
                        String foundEmail = null;
                        
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            String docUsername = doc.getString("username");
                            if (docUsername != null && docUsername.trim().equalsIgnoreCase(input.trim())) {
                                foundEmail = doc.getString("email");
                                if (foundEmail != null && !foundEmail.isEmpty()) {
                                    found = true;
                                    Log.d(TAG, "Found username, email: " + foundEmail);
                                    break;
                                }
                            }
                        }
                        
                        if (found && foundEmail != null) {
                            // Folosește email-ul pentru autentificare
                            signIn(foundEmail, password, callback);
                        } else {
                            Log.d(TAG, "Username not found: " + input);
                            callback.onError("Username not found. Please check your username or use your email address to login.");
                        }
                    } else {
                        Exception exception = task.getException();
                        String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                        Log.e(TAG, "Error searching for username: " + errorMsg);
                        
                        // Încearcă și cu query-ul direct (poate funcționa dacă există index)
                        db.collection("users")
                            .whereEqualTo("username", input)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(queryTask -> {
                                if (queryTask.isSuccessful() && queryTask.getResult() != null && !queryTask.getResult().isEmpty()) {
                                    DocumentSnapshot doc = queryTask.getResult().getDocuments().get(0);
                                    String email = doc.getString("email");
                                    if (email != null && !email.isEmpty()) {
                                        Log.d(TAG, "Found username via query, email: " + email);
                                        signIn(email, password, callback);
                                    } else {
                                        callback.onError("Email not found for this username. Please contact support.");
                                    }
                                } else {
                                    callback.onError("Username not found. Please check your username or use your email address to login.");
                                }
                            });
                    }
                });
        }
    }
    
    /**
     * Salvează datele utilizatorului în Firestore
     */
    private void saveUserData(String userId, String username, String email, AuthCallback callback) {
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        
        db.collection("users").document(userId)
            .set(userData)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess(userId, username);
                } else {
                    callback.onError("Failed to save user data");
                }
            });
    }
    
    /**
     * Obține datele utilizatorului din Firestore
     */
    private void getUserData(String userId, AuthCallback callback) {
        db.collection("users").document(userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot doc = task.getResult();
                    String username = doc.getString("username");
                    if (username != null) {
                        callback.onSuccess(userId, username);
                    } else {
                        callback.onError("Username not found");
                    }
                } else {
                    callback.onError("Failed to get user data");
                }
            });
    }
    
    /**
     * Verifică dacă un username există deja
     */
    public void checkUsernameExists(String username, OnCompleteListener<QuerySnapshot> callback) {
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnCompleteListener(callback);
    }
    
    /**
     * Verifică dacă un email există deja
     */
    public void checkEmailExists(String email, OnCompleteListener<QuerySnapshot> callback) {
        db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnCompleteListener(callback);
    }
    
    /**
     * Obține utilizatorul curent autentificat
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    /**
     * Schimbă parola utilizatorului
     */
    public void changePassword(String newPassword, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(user.getUid(), null);
                    } else {
                        String error = task.getException() != null ? 
                            task.getException().getMessage() : "Failed to change password";
                        callback.onError(error);
                    }
                });
        } else {
            callback.onError("User not logged in");
        }
    }
    
    /**
     * Deconectează utilizatorul
     */
    public void signOut() {
        mAuth.signOut();
    }
}
