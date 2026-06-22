package com.gracelink.android.data.repository

import com.gracelink.android.data.mock.MockData
import com.gracelink.android.data.model.Encouragement
import com.gracelink.android.data.model.PrayerRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prayer wall repository.
 *
 * Spec §8: all prayers start in PENDING state and require admin approval.
 * For the MVP demo we surface approved prayers directly. Wire moderation
 * via Firestore + Cloud Functions before public launch.
 */
@Singleton
class PrayerRepository @Inject constructor() {

    private val _prayers = MutableStateFlow(MockData.prayerRequests)
    val prayers: StateFlow<List<PrayerRequest>> = _prayers.asStateFlow()

    suspend fun fetchPrayers(): List<PrayerRequest> = withContext(Dispatchers.IO) {
        delay(300)
        _prayers.value
    }

    fun submitPrayer(text: String, anonymous: Boolean) {
        val newRequest = PrayerRequest(
            id = "pr_${System.currentTimeMillis()}",
            userId = if (anonymous) null else "u_demo",
            displayName = if (anonymous) null else MockData.currentUser.displayName,
            text = text,
            timestamp = System.currentTimeMillis(),
            prayedCount = 0,
            isMine = true,
        )
        // Phase 2: write to Firestore with status=PENDING instead of direct insert.
        _prayers.update { listOf(newRequest) + it }
    }

    fun togglePrayed(id: String) {
        _prayers.update { list ->
            list.map { p ->
                if (p.id == id) {
                    val nowPrayed = !p.userPrayedThis
                    p.copy(
                        userPrayedThis = nowPrayed,
                        prayedCount = if (nowPrayed) p.prayedCount + 1 else (p.prayedCount - 1).coerceAtLeast(0)
                    )
                } else p
            }
        }
    }

    fun markAnswered(id: String) {
        _prayers.update { list ->
            list.map { if (it.id == id) it.copy(isAnswered = true) else it }
        }
    }

    fun addEncouragement(prayerId: String, text: String) {
        val enc = Encouragement(
            id = "e_${System.currentTimeMillis()}",
            userId = "u_demo",
            displayName = MockData.currentUser.displayName,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        _prayers.update { list ->
            list.map { if (it.id == prayerId) it.copy(encouragements = it.encouragements + enc) else it }
        }
    }
}
