package com.gracelink.android.feature.auth

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gracelink.android.R
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Minimalist, Google-only entry point. Deliberately no email/password
 * form -- one clear action. Picking a Google account is the only way in;
 * a brand-new account always continues to Registration (pick account
 * type + enter details) before anything else.
 */
@Composable
fun AuthScreen(
    onSignInComplete: () -> Unit,
    onNewUserNeedsRegistration: (String, String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val mainHandler = Handler(Looper.getMainLooper())

    fun runOnMain(action: () -> Unit) {
        mainHandler.post { action() }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken == null) {
                    errorMsg = "Google sign-in failed: no ID token"
                    return@rememberLauncherForActivityResult
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                isLoading = true
                errorMsg = null

                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task2 ->
                        isLoading = false
                        if (task2.isSuccessful) {
                            val fbUser = task2.result?.user
                            val isNewUser = task2.result?.additionalUserInfo?.isNewUser ?: false
                            val name = account.displayName ?: fbUser?.displayName ?: "Friend"
                            val emailVal = account.email ?: fbUser?.email ?: ""

                            if (isNewUser) {
                                GoogleAuthData.set(name, emailVal)
                                runOnMain { onNewUserNeedsRegistration(name, emailVal) }
                            } else {
                                runOnMain { onSignInComplete() }
                            }
                        } else {
                            val err = task2.exception?.message ?: "Sign-in failed"
                            Log.e("FaithLinkAuth", "Firebase auth failed: $err", task2.exception)
                            runOnMain { errorMsg = err }
                        }
                    }
            } catch (e: ApiException) {
                Log.e("FaithLinkAuth", "Google API exception: ${e.statusCode}", e)
                runOnMain { errorMsg = "Google sign-in failed (${e.statusCode})" }
            }
        }
    }

    fun signInWithGoogle() {
        errorMsg = null
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("755996957801-33j458gvmmfphuv1bombb09vtra01pm3.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        // Sign out of the Google client first so the account picker always shows,
        // even if the device only has one Google account cached.
        client.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.faith_link_logo),
                contentDescription = "Faith Link",
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text("Faith Link", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text("Continue with Google to get started", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(40.dp))

            if (isLoading) {
                CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(22.dp))
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TextPrimary)
                        .clickable { signInWithGoogle() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Continue with Google", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.background)
                }
            }

            errorMsg?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "By continuing you agree to be part of the Faith Link community.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}
