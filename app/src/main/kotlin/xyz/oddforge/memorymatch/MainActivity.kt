package xyz.oddforge.memorymatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MemoryMatchGame(playSound = soundManager::play)
                }
            }
        }
    }

    override fun onDestroy() {
        soundManager.release()
        super.onDestroy()
    }
}

@Composable
private fun MemoryMatchGame(playSound: (SoundCue) -> Unit) {
    var state by remember { mutableStateOf(GameEngine.createInitialState()) }
    val dispatch: (GameEvent) -> Unit = { event: GameEvent ->
        val oldState = state
        state = GameEngine.reduce(state, event)
        detectSoundCue(oldState, state)?.let(playSound)
    }

    LaunchedEffect(state.pendingHideIndices) {
        if (state.pendingHideIndices.isNotEmpty()) {
            delay(800)
            dispatch(GameEvent.HideUnmatched)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Memory Match", fontSize = 24.sp)
        Text("Moves: ${state.movesCount}", fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

        ThemeSelector(currentTheme = state.theme, onThemeSelected = { dispatch(GameEvent.ChangeTheme(it)) })

        if (state.isGameOver) {
            Text("You Win! ${state.movesCount} moves", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
            Button(onClick = { dispatch(GameEvent.ResetGame) }) {
                Text("Play Again")
            }
        }

        CardGrid(state, dispatch)
    }
}

@Composable
private fun CardGrid(state: GameState, dispatch: (GameEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 4) {
                    val index = row * 4 + col
                    val card = state.cards[index]
                    CardCell(card, onClick = { dispatch(GameEvent.CardTapped(index)) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: GameEngine.TileTheme,
    onThemeSelected: (GameEngine.TileTheme) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        GameEngine.TileTheme.entries.forEach { theme ->
            Button(
                onClick = { onThemeSelected(theme) },
                enabled = theme != currentTheme
            ) {
                Text(theme.label)
            }
        }
    }
}

@Composable
private fun CardCell(card: Card, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val showFace = card.isFaceUp || card.isMatched
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (card.isMatched) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !showFace) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (showFace) card.emoji else "?",
            fontSize = if (showFace) 32.sp else 24.sp
        )
    }
}
