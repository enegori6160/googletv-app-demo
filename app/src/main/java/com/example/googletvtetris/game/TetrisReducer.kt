package com.example.googletvtetris.game

import kotlin.random.Random

sealed interface GameAction {
    data object Tick : GameAction
    data object MoveLeft : GameAction
    data object MoveRight : GameAction
    data object Rotate : GameAction
    data object SoftDropPressed : GameAction
    data object SoftDropReleased : GameAction
    data object HardDrop : GameAction
    data object BackPressed : GameAction
    data object ResumeFromPause : GameAction
    data object Restart : GameAction
}

fun initialState(seed: Int = Random.nextInt()): TetrisState {
    val bag = shuffledBag(seed)
    val first = bag[0]
    val second = bag[1]
    return TetrisState(
        board = emptyBoard(),
        activePiece = ActivePiece(first, x = 3, y = 0, rotation = 0),
        nextPiece = second,
        phase = GamePhase.Playing,
        score = 0,
        level = 1,
        linesCleared = 0,
        softDropping = false
    )
}

fun reduce(state: TetrisState, action: GameAction, randomSeed: Int = Random.nextInt()): TetrisState {
    return when (action) {
        GameAction.Tick -> onTick(state, randomSeed)
        GameAction.MoveLeft -> move(state, -1)
        GameAction.MoveRight -> move(state, 1)
        GameAction.Rotate -> rotate(state)
        GameAction.SoftDropPressed -> state.copy(softDropping = true)
        GameAction.SoftDropReleased -> state.copy(softDropping = false)
        GameAction.HardDrop -> hardDrop(state, randomSeed)
        GameAction.BackPressed -> onBack(state)
        GameAction.ResumeFromPause -> if (state.phase == GamePhase.Paused) state.copy(phase = GamePhase.Playing) else state
        GameAction.Restart -> initialState(randomSeed)
    }
}

private fun onTick(state: TetrisState, randomSeed: Int): TetrisState {
    if (state.phase != GamePhase.Playing) return state
    val moved = state.activePiece.copy(y = state.activePiece.y + 1)
    if (canPlace(state.board, moved)) {
        return state.copy(activePiece = moved)
    }
    return lockAndSpawn(state, randomSeed)
}

private fun move(state: TetrisState, dx: Int): TetrisState {
    if (state.phase != GamePhase.Playing) return state
    val moved = state.activePiece.copy(x = state.activePiece.x + dx)
    return if (canPlace(state.board, moved)) state.copy(activePiece = moved) else state
}

private fun rotate(state: TetrisState): TetrisState {
    if (state.phase == GamePhase.Paused) return state.copy(phase = GamePhase.Playing)
    if (state.phase != GamePhase.Playing) return state

    val rotated = state.activePiece.copy(rotation = (state.activePiece.rotation + 1) % 4)
    if (canPlace(state.board, rotated)) return state.copy(activePiece = rotated)

    val kicks = listOf(-1, 1, -2, 2)
    for (kick in kicks) {
        val kicked = rotated.copy(x = rotated.x + kick)
        if (canPlace(state.board, kicked)) return state.copy(activePiece = kicked)
    }
    return state
}

private fun hardDrop(state: TetrisState, randomSeed: Int): TetrisState {
    if (state.phase != GamePhase.Playing) return state
    var dropped = state.activePiece
    while (canPlace(state.board, dropped.copy(y = dropped.y + 1))) {
        dropped = dropped.copy(y = dropped.y + 1)
    }
    return lockAndSpawn(state.copy(activePiece = dropped), randomSeed, hardDropBonus = 2)
}

private fun onBack(state: TetrisState): TetrisState {
    return when (state.phase) {
        GamePhase.Playing -> state.copy(phase = GamePhase.Paused)
        GamePhase.Paused,
        GamePhase.GameOver -> state
    }
}

private fun lockAndSpawn(state: TetrisState, randomSeed: Int, hardDropBonus: Int = 0): TetrisState {
    val merged = mergePiece(state.board, state.activePiece)
    val (clearedBoard, clearedLines) = clearLines(merged)
    val newLines = state.linesCleared + clearedLines
    val newLevel = (newLines / 10) + 1
    val gained = scoreForLines(clearedLines, state.level) + hardDropBonus

    val nextPiece = state.nextPiece
    val bag = shuffledBag(randomSeed)
    val upcoming = bag.firstOrNull { it != nextPiece } ?: TetrominoType.I
    val spawned = ActivePiece(nextPiece, x = 3, y = 0, rotation = 0)

    val canSpawn = canPlace(clearedBoard, spawned)
    return state.copy(
        board = clearedBoard,
        activePiece = spawned,
        nextPiece = upcoming,
        phase = if (canSpawn) GamePhase.Playing else GamePhase.GameOver,
        score = state.score + gained,
        level = newLevel,
        linesCleared = newLines
    )
}

fun scoreForLines(lines: Int, level: Int): Int {
    val base = when (lines) {
        1 -> 100
        2 -> 300
        3 -> 500
        4 -> 800
        else -> 0
    }
    return base * level
}

fun dropIntervalMs(level: Int, softDropping: Boolean): Long {
    if (softDropping) return 45L
    val raw = 800L - ((level - 1) * 65L)
    return raw.coerceAtLeast(90L)
}

fun pieceCells(piece: ActivePiece): List<Point> {
    return piece.type.rotations[piece.rotation].map { Point(piece.x + it.x, piece.y + it.y) }
}

fun canPlace(board: List<List<CellType>>, piece: ActivePiece): Boolean {
    return pieceCells(piece).all { p ->
        p.x in 0 until BoardWidth && p.y in 0 until BoardHeight && board[p.y][p.x] == CellType.Empty
    }
}

fun mergePiece(board: List<List<CellType>>, piece: ActivePiece): List<List<CellType>> {
    val mutable = board.map { it.toMutableList() }
    pieceCells(piece).forEach { p ->
        if (p.y in 0 until BoardHeight && p.x in 0 until BoardWidth) {
            mutable[p.y][p.x] = piece.type.cell
        }
    }
    return mutable.map { it.toList() }
}

fun clearLines(board: List<List<CellType>>): Pair<List<List<CellType>>, Int> {
    val remaining = board.filter { row -> row.any { it == CellType.Empty } }.toMutableList()
    val cleared = BoardHeight - remaining.size
    repeat(cleared) {
        remaining.add(0, List(BoardWidth) { CellType.Empty })
    }
    return remaining.toList() to cleared
}

private fun shuffledBag(seed: Int): List<TetrominoType> = TetrominoType.entries.shuffled(Random(seed))
