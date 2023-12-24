package tictactoe.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import tictactoe.core.GameStatus.Draw
import tictactoe.core.GameStatus.Ongoing
import tictactoe.core.GameStatus.Win
import tictactoe.core.Player.O
import tictactoe.core.Player.X
import java.util.UUID

enum class Player {
    X, O
}

sealed interface GameStatus {
    data class Ongoing(val turn: Player) : GameStatus
    data class Win(val winner: Player) : GameStatus
    data object Draw : GameStatus
}

data class TicTacToe private constructor(
    val gameId: UUID,
    val board: List<List<Player?>>,
    val status: GameStatus,
    @Transient val events: List<TicTacToeEvent>
) {

    fun placeMark(player: Player, row: Int, col: Int): Either<InvalidMove, TicTacToe> =
        when {
            this.status is Win -> GameAlreadyFinished.left()
            !validTurn(player) -> InvalidTurn.left()
            !isPositionWithinTheGrid(row, col) -> InvalidPosition.left()
            !isPositionEmpty(row, col) -> PositionAlreadyMarked.left()
            else -> mark(player, row, col).right()
        }

    private fun validTurn(player: Player): Boolean = when (this.status) {
        is Ongoing -> player == this.status.turn
        else -> false
    }

    private fun isPositionWithinTheGrid(row: Int, col: Int): Boolean = row in 1..3 && col in 1..3

    private fun isPositionEmpty(row: Int, col: Int): Boolean = this.board[row - 1][col - 1] == null

    private fun mark(player: Player, row: Int, col: Int): TicTacToe {
        val markPlaced = MarkPlaced(gameId, player, row, col)
        val newBoard = this.board.replace(row - 1, col - 1, player)
        val (newStatus, newEvents) = when {
            newBoard.isDraw() -> Pair(Draw, listOf(markPlaced, GameDrawn(gameId)))
            newBoard.areThreeInARow(player) -> Pair(Win(player), listOf(markPlaced, GameWon(gameId, player)))
            newBoard.areThreeInAColumn(player) -> Pair(Win(player), listOf(markPlaced, GameWon(gameId, player)))
            newBoard.areThreeInADiagonal(player) -> Pair(Win(player), listOf(markPlaced, GameWon(gameId, player)))
            else -> Pair(Ongoing(turn = if (player == X) O else X), listOf(markPlaced))
        }
        return this.copy(board = newBoard, status = newStatus, events = this.events + newEvents)
    }

    private fun List<List<Player?>>.isDraw() = this.all { row -> row.all { it != null } }

    private fun List<List<Player?>>.areThreeInARow(player: Player) = this.any { row -> row.all { it == player } }

    private fun List<List<Player?>>.areThreeInAColumn(player: Player) = this.transpose().areThreeInARow(player)

    private fun List<List<Player?>>.transpose(): List<List<Player?>> =
        List(size = 3, init = { col -> List(size = 3, init = { row -> this[row][col] }) })

    private fun List<List<Player?>>.areThreeInADiagonal(player: Player) =
        this[0][0] == player && this[1][1] == player && this[2][2] == player ||
                this[0][2] == player && this[1][1] == player && this[2][0] == player


    companion object {

        fun newGame(gameId: UUID = UUID.randomUUID()): TicTacToe = TicTacToe(
            gameId = gameId,
            board = List(size = 3, init = { listOf(null, null, null) }),
            status = Ongoing(turn = X),
            events = listOf(GameStarted(gameId))
        )

        fun reconstitute(
            gameId: UUID, board: List<List<Player?>>, status: GameStatus, events: List<TicTacToeEvent>
        ): TicTacToe = TicTacToe(gameId, board, status, events)

        fun placeMark(game: TicTacToe, player: String, row: Int, col: Int): Either<InvalidMove, TicTacToe> =
            when (player) {
                "X" -> game.placeMark(X, row, col)
                "O" -> game.placeMark(O, row, col)
                else -> InvalidPlayer.left()
            }
    }
}


fun <T> List<List<T?>>.replace(row: Int, col: Int, value: T): List<List<T?>> =
    mapIndexed { r, rowData ->
        if (r == row) rowData.mapIndexed { c, data -> if (c == col) value else data }
        else rowData
    }
