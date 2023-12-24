package tictactoe.shell.outputs

import arrow.core.left
import arrow.core.right
import bed.mgmt.fixtures.containers.Postgres
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import tictactoe.core.GameNotFound
import tictactoe.core.GameStarted
import tictactoe.core.GameStatus
import tictactoe.core.GameWon
import tictactoe.core.MarkPlaced
import tictactoe.core.Player
import tictactoe.core.TicTacToe

@Tag("integration")
class PostgreSQLGameRepositoryTest {

    private val postgres = Postgres()

    private val jdbcTemplate = JdbcTemplate(postgres.datasource)

    private val repository = PostgreSQLGameRepository(jdbcTemplate)

    @Test
    fun `should save and load game`() {
        val newGame = TicTacToe.newGame()
        val ticTacToe = newGame.copy(
            status = GameStatus.Ongoing(turn = Player.O),
            board = listOf(
                listOf(Player.O, null, null),
                listOf(null, Player.O, null),
                listOf(null, null, null)
            ),
            events = listOf(
                GameStarted(gameId = newGame.gameId),
                MarkPlaced(gameId = newGame.gameId, player = Player.O, row = 3, col = 3),
                GameWon(gameId = newGame.gameId, winner = Player.O)
            )
        )

        val result = repository.save(ticTacToe).let { repository.find(ticTacToe.gameId) }

        result shouldBe ticTacToe.copy(events = emptyList()).right()
    }

    @Test
    fun `should not find a game when it does not exists`() {
        val result = repository.find(TicTacToe.newGame().gameId)

        result shouldBe GameNotFound.left()
    }

}