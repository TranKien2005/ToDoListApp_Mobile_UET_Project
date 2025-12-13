package com.example.todolist.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Repository for Google Sign-In with OAuth scopes support
 * Uses GoogleSignInClient for OAuth flow with Calendar API access
 */
class GoogleSignInRepository(
    private val context: Context,
    private val webClientId: String
) {
    companion object {
        private const val TAG = "GoogleSignInRepository"
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get sign-in intent to launch Google Sign-In UI
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Handle sign-in result from activity result
     */
    suspend fun handleSignInResult(data: Intent?): Result<GoogleSignInAccount> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = suspendCoroutine<GoogleSignInAccount> { continuation ->
                task.addOnSuccessListener { account ->
                    continuation.resume(account)
                }
                task.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            }
            Log.d(TAG, "Sign-in successful: ${account.email}")
            Result.success(account)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out from Google account
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            suspendCoroutine<Unit> { continuation ->
                googleSignInClient.signOut()
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }
            Log.d(TAG, "Sign-out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out failed", e)
            Result.failure(e)
        }
    }

    /**
     * Revoke access (removes app permissions from Google account)
     */
    suspend fun revokeAccess(): Result<Unit> {
        return try {
            suspendCoroutine<Unit> { continuation ->
                googleSignInClient.revokeAccess()
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }
            Log.d(TAG, "Access revoked")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Revoke access failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get currently signed-in account
     */
    fun getCurrentAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Check if user has granted Calendar permission
     */
    fun hasCalendarPermission(): Boolean {
        val account = getCurrentAccount() ?: return false
        return GoogleSignIn.hasPermissions(account, Scope(CalendarScopes.CALENDAR))
    }

    /**
     * Get access token for API calls
     */
    suspend fun getAccessToken(): String? {
        val account = getCurrentAccount() ?: return null
        
        return try {
            com.google.android.gms.auth.GoogleAuthUtil.getToken(
                context,
                account.account!!,
                "oauth2:${CalendarScopes.CALENDAR}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token", e)
            null
        }
    }
}
