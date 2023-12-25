package tictactoe.shell.inputs

import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.notFound
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import tictactoe.core.GameStatus.Draw
import tictactoe.core.GameStatus.Ongoing
import tictactoe.core.GameStatus.Win
import tictactoe.core.Outputs
import tictactoe.core.TicTacToe
import java.util.UUID

@RestController
class GameStatusHttpResource(private val repository: Outputs.GameRepository) {

    @GetMapping("/games/{gameId}")
    fun gameStatus(@PathVariable gameId: UUID): ResponseEntity<GameStatusHttpHttpResponse> =
            repository.find(gameId).fold({ notFound().build() }, { ok(it.asHttpHttpResponse()) })

    private fun TicTacToe.asHttpHttpResponse(): GameStatusHttpHttpResponse =
            GameStatusHttpHttpResponse(
                    id = gameId,
                    board = board.map { row -> row.map { it?.toString() ?: "" } },
                    status = when (status) {
                        is Ongoing -> "ONGOING"
                        is Win -> "WIN"
                        is Draw -> "DRAW"
                    },
                    turn = if (status is Ongoing) status.turn.toString() else null,
                    winner = if (status is Win) status.winner.toString() else null,
            )
}

data class GameStatusHttpHttpResponse(
        val id: UUID,
        val board: List<List<String>>,
        val status: String,
        val turn: String?,
        val winner: String?
)
