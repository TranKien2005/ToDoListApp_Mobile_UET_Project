package com.example.todolist.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Implementation of GoogleAuthRepository using Android Credential Manager
 * and AuthorizationClient API for Google Calendar access
 */
class GoogleAuthRepositoryImpl(
    private val credentialManager: CredentialManager,
    private val webClientId: String
) : GoogleAuthRepository {

    companion object {
        private const val TAG = "GoogleAuthRepository"
    }

    @Volatile
    private var currentUser: GoogleUser? = null

    @Volatile
    private var cachedAccessToken: String? = null

    override suspend fun signIn(context: Context): Result<GoogleUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setRequestVerifiedPhoneNumber(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential

            when (val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)) {
                is GoogleIdTokenCredential -> {
                    val user = GoogleUser(
                        googleId = googleIdTokenCredential.id,
                        email = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName ?: "User",
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                    )
                    currentUser = user
                    Log.d(TAG, "Sign-in successful for: ${user.email}")
                    Result.success(user)
                }
                else -> {
                    Log.e(TAG, "Unexpected credential type")
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign-in failed", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in error", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            currentUser = null
            cachedAccessToken = null
            Log.d(TAG, "Sign-out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out error", e)
        }
    }

    override fun isSignedIn(): Boolean {
        return currentUser != null
    }

    override fun getCurrentUser(): GoogleUser? {
        return currentUser
    }

    /**
     * Get Calendar access token using AuthorizationClient API
     *
     * This is the official recommended way to access Google Account data
     * like Google Calendar, as per Android documentation:
     * "For authorization actions needed to access data stored in the Google Account
     * such as Google Drive, use the AuthorizationClient API."
     */
    override suspend fun getCalendarAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
        if (!isSignedIn()) {
            Log.w(TAG, "User not signed in")
            return@withContext null
        }

        try {
            // Check if we have a cached token
            if (cachedAccessToken != null) {
                Log.d(TAG, "Using cached access token")
                return@withContext cachedAccessToken
            }

            // Use AuthorizationClient for Calendar scope authorization
            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(CalendarScopes.CALENDAR)))
                .build()

            val authorizationClient = Identity.getAuthorizationClient(context)

            // Request authorization
            val authorizationResult: AuthorizationResult = authorizationClient
                .authorize(authorizationRequest)
                .await()

            // Check if authorization was successful
            if (authorizationResult.hasResolution()) {
                // User needs to grant permission - this requires UI interaction
                // The calling code should handle the PendingIntent resolution
                Log.w(TAG, "Authorization requires user interaction")
                return@withContext null
            }

            // Get the access token
            val accessToken = authorizationResult.accessToken

            if (accessToken != null) {
                cachedAccessToken = accessToken
                Log.d(TAG, "Successfully obtained Calendar access token")
                return@withContext accessToken
            }

            Log.w(TAG, "No access token received")
            return@withContext null

        } catch (e: Exception) {
            Log.e(TAG, "Error getting Calendar access token", e)
            return@withContext null
        }
    }
    

    override suspend fun getCalendarPermissionIntent(context: Context): android.app.PendingIntent? = withContext(Dispatchers.IO) {
        try {
            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(CalendarScopes.CALENDAR)))
                .build()

            val authorizationClient = Identity.getAuthorizationClient(context)

            val result = authorizationClient
                .authorize(authorizationRequest)
                .await()

            if (result.hasResolution()) {
                Log.d(TAG, "Authorization requires user interaction")
                return@withContext result.pendingIntent
            }
            
            // If we are here, authorization is already granted.
            // We should try to capture the token now so the next getCalendarAccessToken call succeeds.
            val token = result.accessToken
            if (token != null) {
                 cachedAccessToken = token
                 Log.d(TAG, "Authorization already granted. Token retrieved and cached.")
            } else {
                 Log.w(TAG, "Authorization granted but no token found.")
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting permission intent", e)
            return@withContext null
        }
    }

    override suspend fun handleAuthorizationResult(context: Context, data: Intent): Boolean = withContext(Dispatchers.IO) {
        try {
            val authorizationClient = Identity.getAuthorizationClient(context)
            val authorizationResult = authorizationClient.getAuthorizationResultFromIntent(data)
            
            val accessToken = authorizationResult.accessToken
            if (accessToken != null) {
                cachedAccessToken = accessToken
                Log.d(TAG, "Authorization successful, token cached")
                return@withContext true
            }
            Log.w(TAG, "No access token in authorization result")
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error handling authorization result", e)
            return@withContext false
        }
    }

    /**
     * Clear the cached access token to force a refresh on next request
     */
    fun clearCachedToken() {
        cachedAccessToken = null
        Log.d(TAG, "Cached access token cleared")
    }
}
