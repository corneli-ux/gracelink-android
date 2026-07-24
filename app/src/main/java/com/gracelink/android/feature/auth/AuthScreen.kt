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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gracelink.android.R
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.core.theme.Slate850
import com.gracelink.android.core.theme.TextMuted
import com.gracelink.android.core.theme.TextPrimary
import com.gracelink.android.core.theme.TextSecondary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Google-only sign-in — one clear action, no email/password form.
 * Brand name is now consistently "GraceLink".
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

    fun runOnMain(action: () -> Unit) { mainHandler.post { action() } }

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
                            Log.e("GraceLinkAuth", "Firebase auth failed: $err", task2.exception)
                            runOnMain { errorMsg = err }
                        }
                    }
            } catch (e: ApiException) {
                Log.e("GraceLinkAuth", "Google API exception: ${e.statusCode}", e)
                isLoading = false
                runOnMain {
                    errorMsg = when (e.statusCode) {
                        10 -> "Sign-in is not configured for this build (error 10). This app's signing certificate needs to be registered in the Firebase console under Google Sign-In settings."
                        7 -> "No internet connection. Check your network and try again."
                        12501 -> null
                        else -> "Google sign-in failed (code ${e.statusCode})"
                    }
                }
            }
        } else {
            isLoading = false
            Log.d("GraceLinkAuth", "Google sign-in did not complete, resultCode=${result.resultCode}")
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
        client.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, Slate850)))
    ) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo with glow
            Box(
                Modifier
                    .size(80.dp)
                    .shadow(16.dp, androidx.compose.foundation.shape.CircleShape, ambientColor = Gold400.copy(alpha = 0.2f))
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Brush.radialGradient(listOf(Gold400.copy(alpha = 0.15f), androidx.compose.ui.graphics.Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.faith_link_logo),
                    contentDescription = "GraceLink",
                    modifier = Modifier.size(60.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("GraceLink", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Continue with Google to get started", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(20.dp))
            Text(
                "\u201cThe just shall live by faith.\u201d",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = TextSecondary,
            )
            Text("\u2014 Romans 1:17", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Gold400, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            } else {
                // Gold gradient sign-in button
                Box(
                    Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = Gold400.copy(alpha = 0.25f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(GoldGradient))
                        .clickable { signInWithGoogle() }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Continue with Google", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color(0xFF1A0F00))
                }
            }

            errorMsg?.let {
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(16.dp),
                ) {
                    Text(it, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "By continuing you agree to be part of the GraceLink community.",
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
