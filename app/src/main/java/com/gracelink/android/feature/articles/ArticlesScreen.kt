package com.gracelink.android.feature.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.Slate800
import com.gracelink.android.core.theme.Slate900
import com.gracelink.android.data.db.entity.ArticleEntity

@Composable
fun ArticlesScreen(vm: ArticlesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Articles", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                    Text("Insights from churches & believers", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Gold400).clickable { vm.showWriteDialog(true) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, "Write", tint = Color(0xFF1A1408), modifier = Modifier.size(22.dp))
                }
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.articles, key = { it.id }) { article -> ArticleCard(article) { vm.toggleLike(article.id) } }
            }
        }
        if (state.showWrite) { WriteDialog(onPublish = { t, c -> vm.writeArticle(t, c) }, onDismiss = { vm.showWriteDialog(false) }) }
    }
}

@Composable
private fun ArticleCard(article: ArticleEntity, onLike: () -> Unit) {
    var liked by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Slate800).clickable { }.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Gold400.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(article.authorName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.labelLarge, color = Gold400, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(article.authorName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(formatTime(article.publishedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (article.authorType == com.gracelink.android.data.db.entity.AccountType.CHURCH) {
                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Gold400).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("CHURCH", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1A1408), fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(article.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Text(article.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Like", tint = if (liked) Gold400 else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp).clickable { liked = !liked; onLike() })
            Spacer(Modifier.width(4.dp))
            Text("${article.likeCount}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Rounded.ChatBubble, "Comments", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("${article.commentCount}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WriteDialog(onPublish: (String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(onClick = onDismiss)) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Slate900).padding(24.dp).clickable(enabled = false) {}) {
            Column {
                Text("Write Article", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Title") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, modifier = Modifier.fillMaxWidth().height(150.dp), placeholder = { Text("Write your article...") }, colors = TextFieldDefaults.colors(focusedContainerColor = Slate800, unfocusedContainerColor = Slate800, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Gold400), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { if (title.isNotBlank() && content.isNotBlank()) onPublish(title, content) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold400, contentColor = Color(0xFF1A1408))) { Text("Publish") }
            }
        }
    }
}

private fun formatTime(epoch: Long): String {
    val diff = System.currentTimeMillis() - epoch
    val h = diff / 3_600_000; val d = h / 24
    return when { d > 0 -> "${d}d ago"; h > 0 -> "${h}h ago"; else -> "just now" }
}
