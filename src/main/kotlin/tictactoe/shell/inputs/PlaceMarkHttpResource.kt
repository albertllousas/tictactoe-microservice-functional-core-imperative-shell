package tictactoe.shell.inputs

import arrow.core.Either
import arrow.core.flatMap
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
class PlaceMarkHttpResource(
    private val repository: Outputs.GameRepository,
    private val writeLogs: Outputs.WriteLogs,
    private val placeMark: (TicTacToe, String, Int, Int) -> Either<InvalidMove, TicTacToe> = TicTacToe.Companion::placeMark
) {

    @PostMapping("/games/{gameId}/marks")
    fun placeMark(@PathVariable gameId: UUID, @RequestBody request: MarkHttpDto): ResponseEntity<Unit> =
        repository.find(gameId)
            .flatMap { placeMark(it, request.player, request.row, request.col) }
            .onRight { repository.save(it) }
            .onRight { writeLogs(it.events) }
            .fold({ it.toHttpError() }, { status(CREATED).build() })
}

data class MarkHttpDto(val player: String, val row: Int, val col: Int)

private fun DomainError.toHttpError(): ResponseEntity<Unit> = when (this) {
    GameNotFound -> notFound().build()
    is GameAlreadyFinished, PositionAlreadyMarked -> status(CONFLICT).build()
    is InvalidMove -> unprocessableEntity().build()
}
