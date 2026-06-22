package com.gracelink.android.data.repository

import com.gracelink.android.data.mock.MockData
import com.gracelink.android.data.model.ContentLanguage
import com.gracelink.android.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    private val _user = MutableStateFlow(MockData.currentUser)
    val user: StateFlow<User> = _user.asStateFlow()

    private val _isSignedIn = MutableStateFlow(true)  // start signed-in for demo
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    fun signOut() { _isSignedIn.value = false }
    fun signIn(user: User = MockData.currentUser) {
        _user.value = user
        _isSignedIn.value = true
    }

    fun setPreferredLanguage(language: ContentLanguage) {
        _user.update { it.copy(preferredLanguage = language) }
    }

    fun setDataSaver(enabled: Boolean) {
        _user.update { it.copy(dataSaverEnabled = enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _user.update { it.copy(notificationsEnabled = enabled) }
    }
}
