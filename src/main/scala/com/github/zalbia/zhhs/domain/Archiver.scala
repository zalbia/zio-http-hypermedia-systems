package com.github.zalbia.zhhs.domain

import zio.*
import zio.durationInt

import java.io.File

trait Archiver {
  def getState: UIO[Archiver.State]

  def run: UIO[Unit]

  def archiveFile: UIO[File]

  def reset: UIO[Unit]
}

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

  lazy val live: ULayer[Archiver] = ZLayer.fromZIO {
    for {
      ref <- Ref.make(Archiver.State.initialState)
    } yield new Archiver {
      override def getState: UIO[State] =
        ref.get

      override def run: UIO[Unit] = {
        def runImpl: UIO[Unit] =
          ZIO
            .foreachDiscard(0 until 10) { i =>
              for {
                sleepDuration <- Random.nextDouble
                _             <- ZIO.sleep((sleepDuration * 1000).toInt.millis)
                state         <- ref.updateAndGet(_.copy(progress = (i + 1) / 10.0))
                _             <- ZIO.log(s"Here... ${state.progress}")
                _             <- ZIO.fail(()).unless(state.status == Status.Running) // early exit
              } yield ()
            }
            .ignore *> // early exit off-ramp
            ref.update { state =>
              state.status match {
                case Status.Running  => state.copy(status = Status.Complete)
                case Status.Waiting  => state
                case Status.Complete => state
              }
            }

        for {
          state <- ref.getAndUpdate { state =>
                     if (state.status == Status.Waiting) {
                       state.copy(status = Status.Running, progress = 0.0)
                     } else state
                   }
          _     <- ZIO.when(state.status == Status.Running)(runImpl.fork)
        } yield ()
      }

      override def archiveFile: UIO[File] =
        ZIO.succeed(new File("contacts.json"))

      override def reset: UIO[Unit] =
        ref.set(Archiver.State.initialState)
    }
  }
}
