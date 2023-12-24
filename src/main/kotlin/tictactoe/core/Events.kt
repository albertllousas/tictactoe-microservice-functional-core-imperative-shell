package tictactoe.core

import java.util.UUID

sealed interface TicTacToeEvent {
    val gameId: UUID
}

data class GameStarted(override val gameId: UUID) : TicTacToeEvent

data class MarkPlaced(override val gameId: UUID, val player:Player, val row: Int, val col: Int) : TicTacToeEvent

data class GameWon(override val gameId: UUID, val winner: Player) : TicTacToeEvent

data class GameDrawn(override val gameId: UUID) : TicTacToeEvent