package com.github.zalbia.zhhs.domain

object Archiver {
  final case class State(
    status: Status,
    progress: Double,
  )

  object State {
    lazy val initialState: State = State(
      status = Status.Waiting,
      progress = 0.0,
    )
  }

  sealed trait Status extends Product with Serializable
  object Status {
    case object Waiting  extends Status
    case object Running  extends Status
    case object Complete extends Status
  }
}
