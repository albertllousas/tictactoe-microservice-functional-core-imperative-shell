package tictactoe.shell.inputs

import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tictactoe.core.GameNotFound
import tictactoe.core.GameStatus
import tictactoe.core.Outputs
import tictactoe.core.Player
import tictactoe.core.TicTacToe
import java.util.UUID

@Tag("integration")
@WebMvcTest(GameStatusHttpResource::class)
class GameStatusHttpResourceTest(@Autowired private val mvc: MockMvc) {

    @MockkBean
    private lateinit var gameRepository: Outputs.GameRepository

    @Test
    fun `should return game status`() {
        val game = TicTacToe.reconstitute(
            gameId = UUID.randomUUID(),
            board = listOf(
                listOf(Player.O, null, null),
                listOf(null, Player.X, null),
                listOf(null, null, Player.X)
            ),
            status = GameStatus.Ongoing(turn = Player.O),
            events = emptyList()
        )
        every { gameRepository.find(game.gameId) } returns game.right()

        val response = mvc.perform(get("/games/${game.gameId}"))

        response
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                {"id":"${game.gameId}","board":[["O","",""],["","X",""],["","","X"]],"status":"ONGOING","turn":"O","winner":null}
            """
                )
            )
    }

    @Test
    fun `should return 404 when game is not found`() {
        every { gameRepository.find(any()) } returns GameNotFound.left()

        val response = mvc.perform(get("/games/${UUID.randomUUID()}"))

        response.andExpect(status().isNotFound)
    }

}