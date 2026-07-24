package com.gracelink.android.feature.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gracelink.android.core.components.GlassCard
import com.gracelink.android.core.theme.Gold400
import com.gracelink.android.core.theme.GoldGradient
import com.gracelink.android.data.db.entity.QuestionEntity

/**
 * GraceLink public forum: anyone can ask a question, anyone can answer.
 * Glass-card design — translucent containers, gold accents, no flat dividers.
 */
@Composable
fun ForumScreen(
    onOpenQuestion: (String) -> Unit,
    onAskQuestion: () -> Unit,
    onRequireSignIn: () -> Unit = {},
    vm: ForumViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Forum", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = Gold400)
                Text("Ask honestly. Answer with grace.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                Modifier
                    .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = Gold400.copy(alpha = 0.25f))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(GoldGradient))
                    .clickable {
                        if (state.isGuest) onRequireSignIn() else onAskQuestion()
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, "Ask a question", tint = Color(0xFF1A0F00), modifier = Modifier.size(24.dp))
            }
        }

        if (state.questions.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(40.dp))
                Icon(Icons.Rounded.Forum, null, tint = Gold400.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(10.dp))
                Text("No questions yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Gold400)
                Text("Be the first to ask on GraceLink", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.questions, key = { it.id }) { q ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenQuestion(q.id) }
                    ) {
                        QuestionRow(q)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionRow(q: QuestionEntity) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(q.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(q.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Asked by ${q.authorName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.ChatBubbleOutline, null, tint = Gold400.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("${q.answerCount}", style = MaterialTheme.typography.labelSmall, color = Gold400)
        }
    }
}
