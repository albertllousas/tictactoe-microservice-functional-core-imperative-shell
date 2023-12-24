package tictactoe.shell.inputs

import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import tictactoe.core.DomainError
import tictactoe.core.GameAlreadyFinished
import tictactoe.core.GameNotFound
import tictactoe.core.InvalidMove
import tictactoe.core.Outputs
import tictactoe.core.PositionAlreadyMarked
import tictactoe.core.TicTacToe
import java.util.UUID

@RestController
class StartGameHttpResource(
    private val repository: Outputs.GameRepository,
    private val writeLogs: Outputs.WriteLogs,
    private val newGame: () -> TicTacToe = { TicTacToe.newGame() }
) {

    @PostMapping("/games")
    fun startGame(): ResponseEntity<StartGameHttpHttpResponse> =
        newGame()
            .also { repository.save(it) }
            .also { writeLogs(it.events) }
            .let { status(CREATED).body(StartGameHttpHttpResponse(it.gameId)) }
}

data class StartGameHttpHttpResponse(val id: UUID)
