package tictactoe.shell.outputs

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import arrow.core.right
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import tictactoe.core.GameNotFound
import tictactoe.core.GameStatus.Draw
import tictactoe.core.GameStatus.Ongoing
import tictactoe.core.GameStatus.Win
import tictactoe.core.Outputs
import tictactoe.core.Player
import tictactoe.core.Player.O
import tictactoe.core.Player.X
import tictactoe.core.TicTacToe
import java.util.UUID

@Repository
class PostgreSQLGameRepository(private val jdbcTemplate: JdbcTemplate) : Outputs.GameRepository {

    override fun save(game: TicTacToe) {
        val status = when (game.status) {
            is Ongoing -> "ONGOING_${game.status.turn}"
            is Win -> "WIN_${game.status.winner}"
            is Draw -> "DRAW"
        }
        val boardAsString = game.board.flatten().map { it?.toString() }.joinToString(separator = ",")
        jdbcTemplate.update(
            """
            INSERT INTO games (id, board, status) VALUES (?,?,?) 
            ON CONFLICT (id) DO UPDATE SET board = ?, status = ?
            """,
            game.gameId,
            boardAsString,
            status,
            boardAsString,
            status
        )
    }

    override fun find(gameId: UUID): Either<GameNotFound, TicTacToe> = try {
        jdbcTemplate.queryForObject(""" SELECT * FROM games WHERE id = '$gameId' """) { rs, _ ->
            TicTacToe.reconstitute(
                gameId = rs.getObject("id", UUID::class.java),
                board = rs.getString("board").split(",").map { it.toPlayer() }.chunked(3),
                status = when (val status = rs.getString("status")) {
                    "ONGOING_O" -> Ongoing(turn = O)
                    "ONGOING_X" -> Ongoing(turn = X)
                    "WIN_O" -> Win(winner = O)
                    "WIN_X" -> Win(winner = X)
                    "DRAW" -> Draw
                    else -> throw IllegalArgumentException("Invalid status $status")
                },
                events = emptyList()
            )
        }!!.right()
    } catch (exception: EmptyResultDataAccessException) {
        GameNotFound.left()
    }

    private fun String.toPlayer(): Player? = when (this) {
        "X" -> X
        "O" -> O
        "null" -> null
        else -> throw IllegalArgumentException("Invalid player $this")
    }
}
