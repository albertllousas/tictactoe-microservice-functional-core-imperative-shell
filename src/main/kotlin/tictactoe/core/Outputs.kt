package tictactoe.core

import arrow.core.Either
import java.util.UUID

object Outputs {

    interface GameRepository {
        fun save(game: TicTacToe)
        fun find(gameId: UUID): Either<GameNotFound, TicTacToe>
    }

    interface WriteLogs : (List<TicTacToeEvent>) -> Unit
}
