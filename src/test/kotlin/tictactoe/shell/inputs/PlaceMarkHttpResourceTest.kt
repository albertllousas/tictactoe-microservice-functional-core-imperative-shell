package tictactoe.shell.inputs

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tictactoe.core.GameAlreadyFinished
import tictactoe.core.GameNotFound
import tictactoe.core.InvalidMove
import tictactoe.core.InvalidTurn
import tictactoe.core.Outputs
import tictactoe.core.TicTacToe
import java.util.UUID


@Tag("integration")
@WebMvcTest(PlaceMarkHttpResource::class)
class PlaceMarkHttpResourceTest(@Autowired private val mvc: MockMvc) {

    @MockkBean
    private lateinit var gameRepository: Outputs.GameRepository

    @MockkBean
    private lateinit var writeLog: Outputs.WriteLogs

    @MockkBean
    private lateinit var placeMark: (TicTacToe, String, Int, Int) -> Either<InvalidMove, TicTacToe>

    @BeforeEach
    fun setUp() {
        every { gameRepository.save(any()) } just runs
        every { writeLog(any()) } just runs
    }

    @Test
    fun `should place a mark`() {
        val gameId = UUID.randomUUID()
        val newGame = TicTacToe.newGame(gameId)
        every { gameRepository.find(gameId) } returns newGame.right()
        every { placeMark(newGame, "X", 1, 1) } returns newGame.right()

        val response = mvc.perform(
            post("/games/$gameId/marks")
                .contentType(APPLICATION_JSON)
                .content("""{"player": "X", "col": 1, "row": 1}""")
        )

        response.andExpect(status().isCreated)
        verify {
            gameRepository.save(newGame)
            writeLog(newGame.events)
        }
    }

    @Test
    fun `should fail when game does not exists`() {
        every { gameRepository.find(any()) } returns GameNotFound.left()

        val response = mvc.perform(
            post("/games/${UUID.randomUUID()}/marks")
                .contentType(APPLICATION_JSON)
                .content("""{"player": "X", "col": 1, "row": 1}""")
        )

        response.andExpect(status().isNotFound)
    }

    @Test
    fun `should fail when game is already finished`() {
        every { gameRepository.find(any()) } returns TicTacToe.newGame().right()
        every { placeMark(any(), any(), any(), any()) } returns GameAlreadyFinished.left()

        val response = mvc.perform(
            post("/games/${UUID.randomUUID()}/marks")
                .contentType(APPLICATION_JSON)
                .content("""{"player": "X", "col": 1, "row": 1}""")
        )

        response.andExpect(status().isConflict)
    }

    @Test
    fun `should fail when move is invalid`() {
        every { gameRepository.find(any()) } returns TicTacToe.newGame().right()
        every { placeMark(any(), any(), any(), any()) } returns InvalidTurn.left()

        val response = mvc.perform(
            post("/games/${UUID.randomUUID()}/marks")
                .contentType(APPLICATION_JSON)
                .content("""{"player": "X", "col": 1, "row": 1}""")
        )

        response.andExpect(status().isUnprocessableEntity)
    }
}