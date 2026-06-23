package com.gracelink.android.feature.auth

/**
 * Temporary holder for Google sign-in data that needs to be passed
 * from AuthScreen to RegistrationScreen.
 *
 * This is set when a new Google user signs in, and read by the
 * Registration screen to pre-fill the name and email fields.
 */
object GoogleAuthData {
    var name: String = ""
    var email: String = ""

    fun set(name: String, email: String) {
        this.name = name
        this.email = email
    }

    fun clear() {
        name = ""
        email = ""
    }
}
