package tictactoe.shell.outputs

import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.slf4j.helpers.NOPLogger
import tictactoe.core.GameDrawn
import tictactoe.core.GameWon
import tictactoe.core.MarkPlaced
import tictactoe.core.Player
import tictactoe.core.TicTacToe.Companion.newGame

@Tag("integration")
class WriteLogsWithSL4JTest {

    private val logger = spyk(NOPLogger.NOP_LOGGER)

    private val writeLogs = WriteLogsWithSL4J(logger)

    @Test
    fun `should write logs for a new game`() {
        val game = newGame()

        writeLogs(game.events)

        verify { logger.info("event:'GameStarted', game-id:'${game.gameId}'") }
    }

    @Test
    fun `should write logs for a game with a placed mark`() {
        val newGame = newGame()
        val game = newGame.copy(
            events = listOf(MarkPlaced(gameId = newGame.gameId, player = Player.X, row = 1, col = 1))
        )

        writeLogs(game.events)

        verify { logger.info("event:'MarkPlaced', game-id:'${game.gameId}', player:'X', row:'1', col:'1'") }
    }

    @Test
    fun `should write logs for a game with a winner`() {
        val newGame = newGame()
        val game = newGame.copy(events = listOf(GameWon(gameId = newGame.gameId, winner = Player.X)))

        writeLogs(game.events)

        verify { logger.info("event:'GameWon', game-id:'${game.gameId}', winner:'X'") }
    }

    @Test
    fun `should write logs for a game with a draw`() {
        val newGame = newGame()
        val game = newGame.copy(events = listOf(GameDrawn(gameId = newGame.gameId)))

        writeLogs(game.events)

        verify { logger.info("event:'GameDrawn', game-id:'${game.gameId}'") }
    }
}
