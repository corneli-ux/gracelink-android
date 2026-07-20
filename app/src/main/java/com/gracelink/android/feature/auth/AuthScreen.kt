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
import androidx.compose.runtime.LaunchedEffect
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

    // Safety net: if Firebase's callback never fires at all (e.g. the device
    // is on a network that can reach the Google picker but not Firebase's
    // servers), isLoading would otherwise spin forever with zero feedback --
    // indistinguishable from "the app is just stuck." Time it out instead.
    LaunchedEffect(isLoading) {
        if (isLoading) {
            kotlinx.coroutines.delay(15_000)
            if (isLoading) {
                isLoading = false
                errorMsg = "This is taking too long. Check your connection and try again."
            }
        }
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
                isLoading = false
                runOnMain {
                    errorMsg = when (e.statusCode) {
                        // DEVELOPER_ERROR -- almost always means this build's signing
                        // certificate SHA-1 isn't registered against the OAuth client
                        // in the Firebase/Google Cloud console. A different debug
                        // keystore used by CI vs. a local machine is the classic cause.
                        10 -> "Sign-in is not configured for this build (error 10). This app's signing certificate needs to be registered in the Firebase console under Google Sign-In settings."
                        7 -> "No internet connection. Check your network and try again."
                        12501 -> null // user closed the account picker -- not an error, just stay on screen quietly
                        else -> "Google sign-in failed (code ${e.statusCode})"
                    }
                }
            }
        } else {
            // Non-OK result that ISN'T a simple cancel (e.g. the picker closed due
            // to an underlying error) -- previously this left the screen doing
            // nothing with no feedback at all, which looked like being stuck.
            isLoading = false
            Log.d("FaithLinkAuth", "Google sign-in did not complete, resultCode=${result.resultCode}")
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
            Spacer(Modifier.height(24.dp))
            Text(
                "\u201cThe just shall live by faith.\u201d",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = TextSecondary,
            )
            Text("\u2014 Romans 1:17", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(16.dp))

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
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        .padding(16.dp),
                ) {
                    Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "By continuing you agree to be part of the Faith Link community.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Build ${com.gracelink.android.BuildConfig.GIT_SHA}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}
