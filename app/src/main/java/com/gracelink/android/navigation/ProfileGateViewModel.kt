package com.gracelink.android.navigation

import androidx.lifecycle.ViewModel
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Answers one question for the nav graph at the Splash/Onboarding boundary:
 * has a profile been set up yet, and if so, what account type is it? Used
 * to make "Set Up Profile" a mandatory step before the app opens, and to
 * route straight into the right portal (Church/Pastor/member Home)
 * afterward instead of always landing on the generic Home.
 */
@HiltViewModel
class ProfileGateViewModel @Inject constructor(
    private val userRepo: UserRepository,
) : ViewModel() {
    /** Null if no profile has been set up yet. */
    suspend fun currentAccountType(): AccountType? = userRepo.currentOnce()?.accountType
}
