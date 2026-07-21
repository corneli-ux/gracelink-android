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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.data.db.entity.AccountType
import com.gracelink.android.data.db.entity.BeliefSystem
import com.gracelink.android.feature.auth.GoogleAuthData

/**
 * Lightweight local profile setup -- NOT a login/credential screen. No
 * password, no backend account. This just tells the app who you are
 * (name + role) so church/pastor features and prayer/comment attribution
 * have something to attach to. Reachable non-blockingly from Profile; a
 * real authenticated login can replace/extend this later.
 */
@Composable
fun RegistrationScreen(
    onComplete: (com.gracelink.android.data.db.entity.AccountType) -> Unit,
    onBack: () -> Unit = {},
    prefillName: String = "",
    prefillEmail: String = "",
    vm: RegistrationViewModel = hiltViewModel(),
) {
    val googleName = if (GoogleAuthData.name.isNotBlank()) GoogleAuthData.name else prefillName
    val googleEmail = if (GoogleAuthData.email.isNotBlank()) GoogleAuthData.email else prefillEmail

    var accountType by remember { mutableStateOf<AccountType?>(null) }
    var name by remember { mutableStateOf(googleName) }
    var email by remember { mutableStateOf(googleEmail) }
    var pastorName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedBelief by remember { mutableStateOf(BeliefSystem.NONDENOMINATIONAL) }

    Box(Modifier.fillMaxSize().statusBarsPadding().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Set Up Your Profile", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text(
                if (accountType == null) "How will you be using Faith Link?" else "Tell us a bit about you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            if (accountType == null) {
                RoleCard(Icons.Rounded.Person, "Member", "Listen, pray, join a church, follow pastors") { accountType = AccountType.PERSONAL }
                Spacer(Modifier.height(12.dp))
                RoleCard(Icons.Rounded.Mic, "Individual Pastor", "Publish podcasts, host live spaces, write articles") { accountType = AccountType.PASTOR }
                Spacer(Modifier.height(12.dp))
                RoleCard(Icons.Rounded.Church, "Church", "Manage members, run events, book radio slots") { accountType = AccountType.CHURCH }
            } else {
                Field("Your name", name) { name = it }
                Spacer(Modifier.height(12.dp))
                Field("Email (optional)", email) { email = it }
                if (accountType == AccountType.CHURCH) {
                    Spacer(Modifier.height(12.dp))
                    Field("Pastor's Name", pastorName) { pastorName = it }
                    Spacer(Modifier.height(12.dp))
                    Field("Location", location) { location = it }
                }
                Spacer(Modifier.height(16.dp))
                Text("Belief System", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                BeliefSystem.values().take(8).forEach { belief ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (belief == selectedBelief) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant).clickable { selectedBelief = belief }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = belief == selectedBelief, onClick = { selectedBelief = belief }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(8.dp))
                        Text(belief.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(20.dp))
                GoldButton("Continue as ${accountType.label()}", onClick = {
                    if (name.isNotBlank()) {
                        when (accountType) {
                            AccountType.CHURCH -> vm.registerChurch(name, pastorName, location, selectedBelief, email, onComplete)
                            AccountType.PASTOR -> vm.registerPastor(name, email, selectedBelief, onComplete)
                            else -> vm.registerPersonal(name, email, selectedBelief, onComplete)
                        }
                    }
                }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Text("Choose a different role", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { accountType = null })
            }
        }
    }
}

private fun AccountType?.label(): String = when (this) {
    AccountType.CHURCH -> "Church"
    AccountType.PASTOR -> "Pastor"
    else -> "Member"
}

@Composable
private fun RoleCard(icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 4.dp)) {
        TextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = MaterialTheme.colorScheme.primary), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
}
