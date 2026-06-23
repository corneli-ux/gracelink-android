package com.gracelink.android.feature.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Obsidian
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.feature.auth.GoogleAuthData
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegistrationScreen(
    onComplete: () -> Unit,
    prefillName: String = "",
    prefillEmail: String = "",
    vm: RegistrationViewModel = hiltViewModel(),
) {
    // Read Google auth data if available (set by AuthScreen after Google sign-in)
    val googleName = if (GoogleAuthData.name.isNotBlank()) GoogleAuthData.name else prefillName
    val googleEmail = if (GoogleAuthData.email.isNotBlank()) GoogleAuthData.email else prefillEmail

    var accountType by remember { mutableStateOf<AccountType?>(null) }
    var name by remember { mutableStateOf(googleName) }
    var email by remember { mutableStateOf(googleEmail) }
    var pastorName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedBelief by remember { mutableStateOf(BeliefSystem.NONDENOMINATIONAL) }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Obsidian, Slate800)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            if (googleName.isNotBlank()) {
                Text("Welcome, $googleName! Complete your profile to continue.", style = MaterialTheme.typography.bodyMedium, color = Gold400)
            } else {
                Text("Choose your account type", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))

            if (accountType == null) {
                // Account type selection
                AccountTypeCard(Icons.Rounded.Person, "Personal Account", "Listen, pray, join churches, write articles", false) { accountType = AccountType.PERSONAL }
                Spacer(Modifier.height(12.dp))
                AccountTypeCard(Icons.Rounded.Church, "Church Account", "Manage members, create events, verify your church", true) { accountType = AccountType.CHURCH }
            } else {
                // Registration form
                Field("Name", name) { name = it }
                Spacer(Modifier.height(12.dp))
                Field("Email", email) { email = it }
                if (accountType == AccountType.CHURCH) {
                    Spacer(Modifier.height(12.dp))
                    Field("Pastor's Name", pastorName) { pastorName = it }
                    Spacer(Modifier.height(12.dp))
                    Field("Location", location) { location = it }
                }
                Spacer(Modifier.height(16.dp))
                Text("Select Your Belief System", style = MaterialTheme.typography.titleSmall, color = Gold400, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                BeliefSystem.values().take(8).forEach { belief ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (belief == selectedBelief) Gold400.copy(alpha = 0.15f) else Slate800).clickable { selectedBelief = belief }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = belief == selectedBelief, onClick = { selectedBelief = belief }, colors = RadioButtonDefaults.colors(selectedColor = Gold400))
                        Spacer(Modifier.width(8.dp))
                        Text(belief.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(20.dp))
                GoldButton("Create ${if (accountType == AccountType.CHURCH) "Church" else "Personal"} Account", onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        if (accountType == AccountType.CHURCH) {
                            vm.registerChurch(name, pastorName, location, selectedBelief, email, onComplete)
                        } else {
                            vm.registerPersonal(name, email, selectedBelief, onComplete)
                        }
                    }
                }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Text("Switch to ${if (accountType == AccountType.CHURCH) "Personal" else "Church"}", style = MaterialTheme.typography.bodySmall, color = Gold400, modifier = Modifier.clickable { accountType = if (accountType == AccountType.CHURCH) AccountType.PERSONAL else AccountType.CHURCH })
            }
        }
    }
}

@Composable
private fun AccountTypeCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, isChurch: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Slate800).clickable(onClick = onClick).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Gold400, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Slate800).padding(horizontal = 12.dp, vertical = 4.dp)) {
        TextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
}
