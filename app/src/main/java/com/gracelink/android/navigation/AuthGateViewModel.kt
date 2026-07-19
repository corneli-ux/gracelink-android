package com.gracelink.android.navigation

import androidx.lifecycle.ViewModel
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Answers exactly one question for the nav graph: is there a signed-in user
 * right now? Used at the Splash/Onboarding boundary to decide whether to
 * send the person to Home or to the mandatory sign-in screen.
 */
@HiltViewModel
class AuthGateViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {
    suspend fun isSignedIn(): Boolean = userRepo.currentOnce() != null
}
