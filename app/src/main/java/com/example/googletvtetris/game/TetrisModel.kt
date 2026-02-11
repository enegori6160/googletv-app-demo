package com.example.googletvtetris.game

const val BoardWidth = 10
const val BoardHeight = 20

enum class CellType {
    Empty,
    I,
    O,
    T,
    S,
    Z,
    J,
    L
}

enum class GamePhase {
    Playing,
    Paused,
    GameOver
}

data class Point(val x: Int, val y: Int)

enum class TetrominoType(val cell: CellType, val rotations: List<List<Point>>) {
    I(
        CellType.I,
        listOf(
            listOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1)),
            listOf(Point(2, 0), Point(2, 1), Point(2, 2), Point(2, 3)),
            listOf(Point(0, 2), Point(1, 2), Point(2, 2), Point(3, 2)),
            listOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(1, 3))
        )
    ),
    O(
        CellType.O,
        listOf(
            listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(2, 1)),
            listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(2, 1)),
            listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(2, 1)),
            listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(2, 1))
        )
    ),
    T(
        CellType.T,
        listOf(
            listOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
            listOf(Point(1, 0), Point(1, 1), Point(2, 1), Point(1, 2)),
            listOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(1, 2)),
            listOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(1, 2))
        )
    ),
    S(
        CellType.S,
        listOf(
            listOf(Point(1, 0), Point(2, 0), Point(0, 1), Point(1, 1)),
            listOf(Point(1, 0), Point(1, 1), Point(2, 1), Point(2, 2)),
            listOf(Point(1, 1), Point(2, 1), Point(0, 2), Point(1, 2)),
            listOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(1, 2))
        )
    ),
    Z(
        CellType.Z,
        listOf(
            listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(2, 1)),
            listOf(Point(2, 0), Point(1, 1), Point(2, 1), Point(1, 2)),
            listOf(Point(0, 1), Point(1, 1), Point(1, 2), Point(2, 2)),
            listOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(0, 2))
        )
    ),
    J(
        CellType.J,
        listOf(
            listOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
            listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(1, 2)),
            listOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(2, 2)),
            listOf(Point(1, 0), Point(1, 1), Point(0, 2), Point(1, 2))
        )
    ),
    L(
        CellType.L,
        listOf(
            listOf(Point(2, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
            listOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(2, 2)),
            listOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(0, 2)),
            listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(1, 2))
        )
    )
}

data class ActivePiece(
    val type: TetrominoType,
    val x: Int,
    val y: Int,
    val rotation: Int
)

data class TetrisState(
    val board: List<List<CellType>>,
    val activePiece: ActivePiece,
    val nextPiece: TetrominoType,
    val phase: GamePhase,
    val score: Int,
    val level: Int,
    val linesCleared: Int,
    val softDropping: Boolean
)

fun emptyBoard(): List<List<CellType>> = List(BoardHeight) { List(BoardWidth) { CellType.Empty } }
