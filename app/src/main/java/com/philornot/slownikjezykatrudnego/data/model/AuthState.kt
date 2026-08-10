package com.philornot.slownikjezykatrudnego.data.model

import com.google.firebase.auth.FirebaseUser

/**
 * Sealed hierarchy representing the Firebase authentication state.
 */
sealed class AuthState {
    /** No user is currently authenticated. */
    data object Unauthenticated : AuthState()

    /** Authentication state is being determined (app startup). */
    data object Loading : AuthState()

    /**
     * A user is authenticated.
     *
     * @property user The Firebase [FirebaseUser] representing the signed-in user.
     */
    data class Authenticated(val user: FirebaseUser) : AuthState()
}
