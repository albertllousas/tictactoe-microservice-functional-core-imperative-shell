package tictactoe.core

sealed interface DomainError
data object GameNotFound : DomainError

sealed interface InvalidMove : DomainError
data object InvalidPosition: InvalidMove
data object GameAlreadyFinished: InvalidMove
data object InvalidTurn: InvalidMove
data object InvalidPlayer: InvalidMove
data object PositionAlreadyMarked: InvalidMove
