package tictactoe.core

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tictactoe.core.Player.O
import tictactoe.core.Player.X

class TicTacToeTest {

    @Test
    fun `should create a new game`() {
        val ticTacToe = TicTacToe.newGame()

        ticTacToe shouldBe TicTacToe.reconstitute(
            gameId = ticTacToe.gameId,
            board = List(size = 3, init = { listOf(null, null, null) }),
            status = GameStatus.Ongoing(turn = X),
            events = listOf(GameStarted(gameId = ticTacToe.gameId))
        )
    }

    @Test
    fun `should place a mark`() {
        val ticTacToe = TicTacToe.newGame()

        val result = ticTacToe.placeMark(player = X, row = 1, col = 1)

        result shouldBe TicTacToe.reconstitute(
            gameId = ticTacToe.gameId,
            board = listOf(listOf(X, null, null), listOf(null, null, null), listOf(null, null, null)),
            status = GameStatus.Ongoing(turn = O),
            events = listOf(
                GameStarted(gameId = ticTacToe.gameId),
                MarkPlaced(gameId = ticTacToe.gameId, player = X, row = 1, col = 1)
            )
        ).right()
    }

    @Test
    fun `should not place a mark when the player is invalid`() {

        val result = TicTacToe.placeMark(game = TicTacToe.newGame(), player = "x", row = 0, col = 0)

        result shouldBe InvalidPlayer.left()
    }

    @Test
    fun `should not place a mark when game is already finished`() {
        val ticTacToe = TicTacToe.newGame().copy(status = GameStatus.Win(winner = X))

        val result = ticTacToe.placeMark(player = X, row = 3, col = 3)

        result shouldBe GameAlreadyFinished.left()
    }

    @Test
    fun `should not place a mark when it is not the player's turn`() {
        val ticTacToe = TicTacToe.newGame().copy(status = GameStatus.Ongoing(turn = O))

        val result = ticTacToe.placeMark(player = X, row = 3, col = 3)

        result shouldBe InvalidTurn.left()
    }

    @Test
    fun `should not place a mark when position is not within the grid`() {
        val ticTacToe = TicTacToe.newGame()

        val result = ticTacToe.placeMark(player = X, row = 4, col = 4)

        result shouldBe InvalidPosition.left()
    }

    @Test
    fun `should not place a mark when position is already marked`() {
        val ticTacToe = TicTacToe.newGame().copy(
            board = listOf(
                listOf(X, null, null),
                listOf(null, null, null),
                listOf(null, null, null)
            )
        )

        val result = ticTacToe.placeMark(player = X, row = 1, col = 1)

        result shouldBe PositionAlreadyMarked.left()
    }

    @Test
    fun `should place a mark and win the game when there are three marks of the same player in a row`() {
        val ticTacToe = TicTacToe.newGame().copy(
            status = GameStatus.Ongoing(turn = O),
            board = listOf(
                listOf(O, O, null),
                listOf(null, null, null),
                listOf(null, null, null)
            )
        )

        val result = ticTacToe.placeMark(player = O, row = 1, col = 3)

        result shouldBe TicTacToe.reconstitute(
            gameId = ticTacToe.gameId,
            board = listOf(
                listOf(O, O, O),
                listOf(null, null, null),
                listOf(null, null, null)
            ),
            status = GameStatus.Win(winner = O),
            events = listOf(
                GameStarted(gameId = ticTacToe.gameId),
                MarkPlaced(gameId = ticTacToe.gameId, player = O, row = 1, col = 3),
                GameWon(gameId = ticTacToe.gameId, winner = O)
            )
        ).right()
    }

    @Test
    fun `should place a mark and win the game when there are three marks of the same player in a column`() {
        val ticTacToe = TicTacToe.newGame().copy(
            status = GameStatus.Ongoing(turn = O),
            board = listOf(
                listOf(O, null, null),
                listOf(O, null, null),
                listOf(null, null, null)
            )
        )

        val result = ticTacToe.placeMark(player = O, row = 3, col = 1)

        result shouldBe TicTacToe.reconstitute(
            gameId = ticTacToe.gameId,
            board = listOf(listOf(O, null, null), listOf(O, null, null), listOf(O, null, null)),
            status = GameStatus.Win(winner = O),
            events = listOf(
                GameStarted(gameId = ticTacToe.gameId),
                MarkPlaced(gameId = ticTacToe.gameId, player = O, row = 3, col = 1),
                GameWon(gameId = ticTacToe.gameId, winner = O)
            )
        ).right()
    }

    @Test
    fun `should place a mark and win the game when there are three marks of the same player in a diagonal`() {
        val ticTacToe = TicTacToe.newGame().copy(
            status = GameStatus.Ongoing(turn = O),
            board = listOf(
                listOf(O, null, null),
                listOf(null, O, null),
                listOf(null, null, null)
            )
        )

        val result = ticTacToe.placeMark(player = O, row = 3, col = 3)

        result shouldBe TicTacToe.reconstitute(
            gameId = ticTacToe.gameId,
            board = listOf(
                listOf(O, null, null),
                listOf(null, O, null),
                listOf(null, null, O)
            ),
            status = GameStatus.Win(winner = O),
            events = listOf(
                GameStarted(gameId = ticTacToe.gameId),
                MarkPlaced(gameId = ticTacToe.gameId, player = O, row = 3, col = 3),
                GameWon(gameId = ticTacToe.gameId, winner = O)
            )
        ).right()
    }

    @Test
    fun `should place a mark and draw the game when there are no more empty positions`() {
        val ticTacToe = TicTacToe.newGame().copy(
            board = listOf(
                listOf(X, O, X),
                listOf(X, O, O),
                listOf(O, X, null)
            )
        )

        val result = ticTacToe.placeMark(player = X, row = 3, col = 3)

        result shouldBe TicTacToe.reconstitute(
            gameId = ticTacToe.gameId,
            board = listOf(
                listOf(X, O, X),
                listOf(X, O, O),
                listOf(O, X, X)
            ),
            status = GameStatus.Draw,
            events = listOf(
                GameStarted(gameId = ticTacToe.gameId),
                MarkPlaced(gameId = ticTacToe.gameId, player = X, row = 3, col = 3),
                GameDrawn(gameId = ticTacToe.gameId)
            )
        ).right()
    }
}
