package com.example.googletvtetris

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googletvtetris.game.ActivePiece
import com.example.googletvtetris.game.BoardHeight
import com.example.googletvtetris.game.BoardWidth
import com.example.googletvtetris.game.CellType
import com.example.googletvtetris.game.GameAction
import com.example.googletvtetris.game.GamePhase
import com.example.googletvtetris.game.TetrisState
import com.example.googletvtetris.game.TetrisViewModel
import com.example.googletvtetris.game.mergePiece

class MainActivity : ComponentActivity() {
    private val vm by viewModels<TetrisViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val state by vm.state.collectAsStateWithLifecycle()
                TetrisScreen(
                    state = state,
                    onAction = vm::dispatch
                )
            }
        }
    }
}

@Composable
private fun TetrisScreen(state: TetrisState, onAction: (GameAction) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BackHandler {
        when (state.phase) {
            GamePhase.Playing -> onAction(GameAction.BackPressed)
            GamePhase.Paused,
            GamePhase.GameOver -> activity?.finish()
        }
    }

    val renderBoard = if (state.phase == GamePhase.Playing) {
        mergePiece(state.board, state.activePiece)
    } else {
        state.board
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 72.dp, vertical = 48.dp)
            // 修正箇所: focusable() を追加し、focusRequester を適用
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyDown -> {
                        onAction(GameAction.SoftDropPressed)
                        true
                    }
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyUp -> {
                        onAction(GameAction.SoftDropReleased)
                        true
                    }
                    keyEvent.type != KeyEventType.KeyDown -> false
                    keyEvent.nativeKeyEvent.repeatCount > 0 && keyEvent.key in listOf(Key.DirectionCenter, Key.Enter, Key.DirectionUp) -> true
                    keyEvent.key == Key.DirectionLeft -> {
                        onAction(GameAction.MoveLeft); true
                    }
                    keyEvent.key == Key.DirectionRight -> {
                        onAction(GameAction.MoveRight); true
                    }
                    keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter -> {
                        when (state.phase) {
                            GamePhase.GameOver -> onAction(GameAction.Restart)
                            else -> onAction(GameAction.Rotate)
                        }
                        true
                    }
                    keyEvent.key == Key.DirectionUp -> {
                        onAction(GameAction.HardDrop); true
                    }
                    keyEvent.key == Key.Back -> {
                        onAction(GameAction.BackPressed); true
                    }
                    else -> false
                }
            },
        horizontalArrangement = Arrangement.spacedBy(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TetrisBoard(
            board = renderBoard,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .border(2.dp, Color(0xFF94A3B8))
                .padding(8.dp)
        )

        SidePanel(state, modifier = Modifier.width(320.dp).fillMaxHeight())
    }
}

@Composable
private fun TetrisBoard(board: List<List<CellType>>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cellW = size.width / BoardWidth
        val cellH = size.height / BoardHeight

        for (y in 0 until BoardHeight) {
            for (x in 0 until BoardWidth) {
                val topLeft = Offset(x * cellW, y * cellH)
                drawRect(
                    color = cellColor(board[y][x]),
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(cellW, cellH)
                )
                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(cellW, cellH),
                    style = Stroke(width = 1f)
                )
            }
        }
    }
}

@Composable
private fun SidePanel(state: TetrisState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("TV Tetris", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text("Score: ${state.score}", color = Color(0xFFBFDBFE), style = MaterialTheme.typography.titleLarge)
        Text("Level: ${state.level}", color = Color(0xFFBFDBFE), style = MaterialTheme.typography.titleLarge)
        Text("Lines: ${state.linesCleared}", color = Color(0xFFBFDBFE), style = MaterialTheme.typography.titleLarge)

        Text("Next", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
        NextPiecePreview(state.activePiece.copy(type = state.nextPiece), Modifier.width(120.dp).height(120.dp).border(1.dp, Color.Gray).padding(8.dp))

        when (state.phase) {
            GamePhase.Playing -> Text("← → 移動 / ↓ ソフトドロップ\n↑ ハードドロップ / 決定 回転\nBACK 一時停止", color = Color(0xFFD1D5DB))
            GamePhase.Paused -> Text("PAUSED\n決定で再開\nBACKでホームへ", color = Color(0xFFFBBF24), style = MaterialTheme.typography.titleLarge)
            GamePhase.GameOver -> Text("GAME OVER\nBACKでホームへ\n決定でリスタート", color = Color(0xFFF87171), style = MaterialTheme.typography.titleLarge)
        }

        if (state.phase == GamePhase.GameOver) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text("決定で新しいゲーム", color = Color.White)
            }
        }
    }
}

@Composable
private fun NextPiecePreview(piece: ActivePiece, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cell = size.minDimension / 4f
        piece.type.rotations[0].forEach { p ->
            val x = p.x * cell
            val y = p.y * cell
            drawRect(
                color = cellColor(piece.type.cell),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(cell, cell)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(cell, cell),
                style = Stroke(width = 1f)
            )
        }
    }
}

private fun cellColor(cell: CellType): Color = when (cell) {
    CellType.Empty -> Color(0xFF111827)
    CellType.I -> Color(0xFF22D3EE)
    CellType.O -> Color(0xFFFACC15)
    CellType.T -> Color(0xFFA78BFA)
    CellType.S -> Color(0xFF4ADE80)
    CellType.Z -> Color(0xFFF87171)
    CellType.J -> Color(0xFF60A5FA)
    CellType.L -> Color(0xFFFB923C)
}
