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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.gracelink.android.R
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.GhostButton
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate950
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun AuthScreen(
    onSignInComplete: () -> Unit,
    onNewUserNeedsRegistration: (String, String) -> Unit,
    onRegister: () -> Unit,
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val mainHandler = Handler(Looper.getMainLooper())

    // Helper to run navigation on the main thread
    fun runOnMain(action: () -> Unit) {
        mainHandler.post { action() }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("FaithLinkAuth", "Google sign-in result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("FaithLinkAuth", "Google account selected: ${account.email} ${account.displayName}")
                val idToken = account.idToken
                if (idToken == null) {
                    errorMsg = "Google sign-in failed: no ID token"
                    Log.e("FaithLinkAuth", "ID token is null")
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
                            Log.d("FaithLinkAuth", "Firebase auth SUCCESS. UID: ${fbUser?.uid}, New: $isNewUser")

                            val name = account.displayName ?: fbUser?.displayName ?: "Friend"
                            val emailVal = account.email ?: fbUser?.email ?: ""

                            if (isNewUser) {
                                Log.d("FaithLinkAuth", "New user → Registration with name=$name email=$emailVal")
                                GoogleAuthData.set(name, emailVal)
                                runOnMain { onNewUserNeedsRegistration(name, emailVal) }
                            } else {
                                Log.d("FaithLinkAuth", "Returning user → Home")
                                runOnMain { onSignInComplete() }
                            }
                        } else {
                            val err = task2.exception?.message ?: "Firebase auth failed"
                            Log.e("FaithLinkAuth", "Firebase auth FAILED: $err", task2.exception)
                            runOnMain { errorMsg = err }
                        }
                    }
            } catch (e: ApiException) {
                Log.e("FaithLinkAuth", "Google API exception: ${e.statusCode}", e)
                runOnMain { errorMsg = "Google sign-in failed: ${e.statusCode}" }
            }
        } else {
            Log.d("FaithLinkAuth", "Google sign-in cancelled by user")
            // Don't show error — user just cancelled
        }
    }

    fun signInWithEmail() {
        if (email.isBlank() || password.isBlank()) {
            errorMsg = "Enter email and password"
            return
        }
        isLoading = true
        errorMsg = null

        // Try sign-in first. If it fails (user doesn't exist), try create account.
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    isLoading = false
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    Log.d("FaithLinkAuth", "Email sign-in success. New: $isNewUser")
                    if (isNewUser) {
                        GoogleAuthData.set("", email)
                        runOnMain { onNewUserNeedsRegistration("", email) }
                    } else {
                        runOnMain { onSignInComplete() }
                    }
                } else {
                    // Sign-in failed — try creating the account
                    Log.d("FaithLinkAuth", "Sign-in failed, trying create account...")
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task2 ->
                            isLoading = false
                            if (task2.isSuccessful) {
                                Log.d("FaithLinkAuth", "Account created successfully")
                                GoogleAuthData.set("", email)
                                runOnMain { onNewUserNeedsRegistration("", email) }
                            } else {
                                val err = task2.exception?.message ?: "Authentication failed"
                                Log.e("FaithLinkAuth", "Create account failed: $err")
                                runOnMain { errorMsg = err }
                            }
                        }
                }
            }
    }

    fun signInWithGoogle() {
        errorMsg = null
        Log.d("FaithLinkAuth", "Starting Google sign-in...")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("755996957801-33j458gvmmfphuv1bombb09vtra01pm3.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        // Sign out first to force the account picker
        client.signOut().addOnCompleteListener {
            Log.d("FaithLinkAuth", "Launching Google sign-in intent")
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Obsidian, Slate950, Slate800)))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))
            Image(painter = painterResource(id = R.drawable.faith_link_logo), contentDescription = "Faith Link", modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text("Faith Link", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("Sign in or create account", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            // Email field
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                TextField(
                    value = email, onValueChange = { email = it; errorMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Rounded.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
            }
            Spacer(Modifier.height(12.dp))

            // Password field
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                TextField(
                    value = password, onValueChange = { password = it; errorMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { signInWithEmail() }),
                )
            }

            // Error message
            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            if (isLoading) {
                CircularProgressIndicator(color = Gold400, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(10.dp))
            }

            // Sign In / Sign Up button (same action — tries sign in, falls back to create)
            GoldButton("Sign In / Sign Up", onClick = { signInWithEmail() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            GhostButton("Sign in with Google", onClick = { signInWithGoogle() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text(
                "New here? Sign up with email or Google above",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onRegister() },
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}
