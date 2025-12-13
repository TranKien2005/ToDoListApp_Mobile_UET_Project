package com.example.todolist.data.repository

import android.content.Context
import android.content.Intent

/**
 * Data class representing a Google user after sign-in
 */
data class GoogleUser(
    val googleId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?
)

/**
 * Repository interface for Google authentication operations
 */
interface GoogleAuthRepository {
    /**
     * Sign in with Google using Credential Manager
     * @param context Activity context required for Credential Manager
     * @return Result containing GoogleUser on success or exception on failure
     */
    suspend fun signIn(context: Context): Result<GoogleUser>
    
    /**
     * Sign out from Google account and clear cached credentials
     */
    suspend fun signOut()
    
    /**
     * Check if user is currently signed in with Google
     */
    fun isSignedIn(): Boolean
    
    /**
     * Get the currently signed-in Google user
     * @return GoogleUser if signed in, null otherwise
     */
    fun getCurrentUser(): GoogleUser?

    /**
     * Get the Google Calendar OAuth access token
     * Note: This method implies that the necessary calendar scopes were requested during sign-in.
     * @return Access token string if available, null otherwise
     */
    suspend fun getCalendarAccessToken(context: Context): String?

    /**
     * Get the PendingIntent to request Calendar scope permission from the user.
     * This should be used to launch the authorization flow when getCalendarAccessToken returns null.
     */
    suspend fun getCalendarPermissionIntent(context: Context): android.app.PendingIntent?

    /**
     * Handle the result of the permission request execution
     */
    suspend fun handleAuthorizationResult(context: Context, data: Intent): Boolean
}
