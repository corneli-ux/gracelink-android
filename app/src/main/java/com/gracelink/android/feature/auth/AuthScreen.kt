package com.gracelink.android.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.components.GhostButton
import com.gracelink.android.core.theme.Gold500
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate950

@Composable
fun AuthScreen(onDone: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Obsidian, Slate950, Slate800)))) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.gracelink.android.R.drawable.faith_link_logo),
                contentDescription = "Faith Link",
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Faith Link", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text("Sign in to continue", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                TextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Email", color = MaterialTheme.colorScheme.onSurfaceVariant) }, leadingIcon = { Icon(Icons.Rounded.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
                TextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Password", color = MaterialTheme.colorScheme.onSurfaceVariant) }, leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }, visualTransformation = PasswordVisualTransformation(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold500), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onDone() }))
            }
            Spacer(Modifier.height(20.dp))
            GoldButton("Sign In", onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            GhostButton("Sign in with Google", onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text("Don't have an account? Sign up", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onDone() })
        }
    }
}
