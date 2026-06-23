package com.gracelink.android.feature.auth

import android.app.Activity
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("FaithLinkAuth", "Google account: ${account.email} ${account.displayName}")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task2 ->
                        isLoading = false
                        if (task2.isSuccessful) {
                            val fbUser = task2.result?.user
                            val is_new = task2.result?.additionalUserInfo?.isNewUser ?: false
                            Log.d("FaithLinkAuth", "Firebase auth success. New user: $is_new")
                            if (is_new) {
                                // New Google user — go to Registration to collect
                                // name, account type, belief system
                                val name = account.displayName ?: fbUser?.displayName ?: "Friend"
                                val email = account.email ?: fbUser?.email ?: ""
                                onNewUserNeedsRegistration(name, email)
                            } else {
                                // Returning user — go straight to Home
                                onSignInComplete()
                            }
                        } else {
                            errorMsg = "Firebase auth failed: ${task2.exception?.message}"
                            Log.e("FaithLinkAuth", "Firebase auth failed", task2.exception)
                        }
                    }
            } catch (e: ApiException) {
                errorMsg = "Google sign-in failed: ${e.statusCode} ${e.message}"
                Log.e("FaithLinkAuth", "Google sign-in API exception", e)
            }
        } else {
            Log.d("FaithLinkAuth", "Google sign-in cancelled, result code: ${result.resultCode}")
        }
    }

    fun signInWithEmail() {
        if (email.isBlank() || password.isBlank()) {
            errorMsg = "Enter email and password"
            return
        }
        isLoading = true
        errorMsg = null
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val is_new = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (is_new) {
                        onNewUserNeedsRegistration("", email)
                    } else {
                        onSignInComplete()
                    }
                } else {
                    errorMsg = task.exception?.message ?: "Sign-in failed"
                }
            }
    }

    fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("755996957801-33j458gvmmfphuv1bombb09vtra01pm3.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        // Sign out first to force the account picker every time
        client.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Obsidian, Slate950, Slate800)))) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))
            Image(painter = painterResource(id = R.drawable.faith_link_logo), contentDescription = "Faith Link", modifier = Modifier.size(72.dp))
            Spacer(Modifier.height(16.dp))
            Text("Faith Link", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("Sign in to continue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                TextField(value = email, onValueChange = { email = it; errorMsg = null }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant) }, leadingIcon = { Icon(Icons.Rounded.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                TextField(value = password, onValueChange = { password = it; errorMsg = null }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) }, leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }, visualTransformation = PasswordVisualTransformation(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { signInWithEmail() }))
            }

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            if (isLoading) {
                CircularProgressIndicator(color = Gold400, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(10.dp))
            }
            GoldButton("Sign In", onClick = { signInWithEmail() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            GhostButton("Sign in with Google", onClick = { signInWithGoogle() }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text("Don't have an account? Sign up", style = MaterialTheme.typography.bodySmall, color = Gold400, modifier = Modifier.clickable { onRegister() })
            Spacer(Modifier.height(40.dp))
        }
    }
}
