package com.tieuvat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tieuvat.app.data.Entry
import com.tieuvat.app.data.EntryType
import com.tieuvat.app.data.Store
import com.tieuvat.app.data.weekStart
import com.tieuvat.app.ui.EntryDialog
import com.tieuvat.app.ui.GardenBackground
import com.tieuvat.app.ui.HistoryScreen
import com.tieuvat.app.ui.HomeScreen
import com.tieuvat.app.ui.InkDark
import com.tieuvat.app.ui.Rainbow
import com.tieuvat.app.ui.SettingsScreen
import com.tieuvat.app.ui.StatsScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF7048E8),
                    secondary = Color(0xFFE64980),
                    tertiary = Color(0xFF12B886)
                )
            ) { App() }
        }
    }
}

private data class Tab(val emoji: String, val label: String, val colors: List<Color>)

private val tabs = listOf(
    Tab("🏡", "Tổng quan", listOf(Color(0xFFFF8FAB), Color(0xFFFFB86B))),
    Tab("📊", "Thống kê", listOf(Color(0xFF4DABF7), Color(0xFF9775FA))),
    Tab("📜", "Lịch sử", listOf(Color(0xFF38D9A9), Color(0xFF69DB7C))),
    Tab("🐷", "Tiết kiệm", listOf(Color(0xFFFFD43B), Color(0xFFFF922B)))
)

@Composable
private fun App() {
    val ctx = LocalContext.current
    val store = remember { Store(ctx.applicationContext) }
    var tab by remember { mutableIntStateOf(0) }
    var week by remember { mutableStateOf(LocalDate.now().weekStart()) }
    var editing by remember { mutableStateOf<Entry?>(null) }
    var adding by remember { mutableStateOf<EntryType?>(null) }

    Box(Modifier.fillMaxSize()) {
        GardenBackground()
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomBar(tab) { tab = it } },
            floatingActionButton = { AddButton { adding = EntryType.EXPENSE } }
        ) { pad ->
            when (tab) {
                0 -> HomeScreen(store, week, { week = it }, pad) { editing = it }
                1 -> StatsScreen(store, week, { week = it }, pad)
                2 -> HistoryScreen(store, pad) { editing = it }
                else -> SettingsScreen(store, week, pad)
            }
        }
    }

    if (adding != null || editing != null) {
        EntryDialog(
            initial = editing,
            defaultType = adding ?: EntryType.EXPENSE,
            onDismiss = { adding = null; editing = null },
            onSave = { store.upsert(it); adding = null; editing = null },
            onDelete = { store.delete(it); adding = null; editing = null }
        )
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "fab")
    val pulse by t.animateFloat(1f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse")
    Box(
        Modifier.scale(pulse).size(66.dp)
            .shadow(12.dp, CircleShape, spotColor = Color(0xFFE64980))
            .clip(CircleShape)
            .background(Brush.sweepGradient(Rainbow + Rainbow.first()))
            .border(3.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text("+", fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.ExtraBold) }
}

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(14.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(2.dp, Brush.horizontalGradient(Rainbow), RoundedCornerShape(28.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { i, t ->
            val sel = i == selected
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(22.dp))
                    .background(if (sel) Brush.linearGradient(t.colors) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                    .clickable { onSelect(i) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(t.emoji, fontSize = if (sel) 22.sp else 20.sp)
                Text(t.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else InkDark)
            }
        }
    }
}
