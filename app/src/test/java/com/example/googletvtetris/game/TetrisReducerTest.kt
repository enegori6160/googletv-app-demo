package com.example.googletvtetris.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TetrisReducerTest {

    @Test
    fun `soft drop interval should be fast`() {
        assertEquals(45L, dropIntervalMs(level = 5, softDropping = true))
    }

    @Test
    fun `line clear scoring should follow classic table`() {
        assertEquals(100, scoreForLines(1, level = 1))
        assertEquals(600, scoreForLines(2, level = 2))
        assertEquals(1500, scoreForLines(3, level = 3))
        assertEquals(3200, scoreForLines(4, level = 4))
    }

    @Test
    fun `clearLines removes full rows`() {
        val full = List(BoardWidth) { CellType.I }
        val board = emptyBoard().toMutableList()
        board[BoardHeight - 1] = full

        val (cleared, count) = clearLines(board)

        assertEquals(1, count)
        assertTrue(cleared.first().all { it == CellType.Empty })
    }

    @Test
    fun `back during playing pauses game`() {
        val state = initialState(seed = 1)
        val paused = reduce(state, GameAction.BackPressed, randomSeed = 2)
        assertEquals(GamePhase.Paused, paused.phase)
    }
}
