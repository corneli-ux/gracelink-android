package com.gracelink.android.feature.churchportal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Church
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GoldButton
import com.gracelink.android.data.db.entity.BeliefSystem

@Composable
fun ChurchEditProfileScreen(onBack: () -> Unit, vm: ChurchEditProfileViewModel = hiltViewModel()) {
    val screenState by vm.state.collectAsStateWithLifecycle()
    val church = screenState.church

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) vm.uploadPhoto(uri)
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pastorName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var belief by remember { mutableStateOf(BeliefSystem.NONDENOMINATIONAL) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(church) {
        val c = church
        if (c != null && !initialized) {
            name = c.name
            description = c.description
            pastorName = c.pastorName
            location = c.location
            website = c.website ?: ""
            phone = c.phone ?: ""
            belief = c.beliefSystem
            initialized = true
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Edit Church Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        }

        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable(enabled = church != null) { photoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        screenState.isUploadingPhoto -> CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                        church?.photoUrl != null -> coil.compose.AsyncImage(model = church.photoUrl, contentDescription = "Church photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                        else -> Icon(Icons.Rounded.Church, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CameraAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Change church photo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    if (screenState.photoUploadError != null) {
                        Text(screenState.photoUploadError ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Field("Church name", name) { name = it }
            Spacer(Modifier.height(12.dp))
            Field("Description", description) { description = it }
            Spacer(Modifier.height(12.dp))
            Field("Pastor's name", pastorName) { pastorName = it }
            Spacer(Modifier.height(12.dp))
            Field("Location", location) { location = it }
            Spacer(Modifier.height(12.dp))
            Field("Website (optional)", website) { website = it }
            Spacer(Modifier.height(12.dp))
            Field("Phone (optional)", phone) { phone = it }

            Spacer(Modifier.height(20.dp))
            Text("Belief System", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            BeliefSystem.values().forEach { b ->
                Row(
                    Modifier.fillMaxWidth().clickable { belief = b }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = b == belief, onClick = { belief = b }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                    Text(b.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(24.dp))
            GoldButton("Save Changes", onClick = {
                if (name.isNotBlank()) {
                    vm.save(name, description, pastorName, location, belief, website, phone, onDone = onBack)
                }
            }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}
