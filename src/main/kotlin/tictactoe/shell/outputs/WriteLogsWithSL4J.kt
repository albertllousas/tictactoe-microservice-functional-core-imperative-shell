package tictactoe.shell.outputs

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import tictactoe.core.GameWon
import tictactoe.core.MarkPlaced
import tictactoe.core.Outputs
import tictactoe.core.TicTacToeEvent
import java.lang.invoke.MethodHandles

@Component
class WriteLogsWithSL4J(
    private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
) : Outputs.WriteLogs {

    override fun invoke(events: List<TicTacToeEvent>) {
        events.forEach {
            val extraLog = when (it) {
                is MarkPlaced -> ", player:'${it.player}', row:'${it.row}', col:'${it.col}'"
                is GameWon -> ", winner:'${it.winner}'"
                else -> ""
            }
            logger.info("event:'${it::class.simpleName}', game-id:'${it.gameId}'$extraLog")
        }
    }
}