package vrch.vr

import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import io.grpc.stub.StreamObserver
import vrch.{Incoming, Outgoing, Text}

class VrActor(out: StreamObserver[Outgoing]) extends Actor {
  private[this] var last: Option[ActorRef] = None

  override def receive: Receive = {
    case text: Text =>
      last match {
        case Some(_) =>
          println("already exists.")

        case None =>
          out.onNext(Outgoing().update(_.text := text))
          last = Some(sender())

      }

    case in: Incoming =>
      last match {
        case Some(ref) =>
          ref ! in.getVoice
          last = None

        case None =>
          println("does not exist.")
      }
  }
}

object VrActor {
  def props(out: StreamObserver[Outgoing]): Props = Props(new VrActor(out))

  class IncomingObserver(id: UUID, self: ActorRef) extends StreamObserver[Incoming] {
    override def onError(t: Throwable): Unit = {
      println(s"$id error: $t")
    }

    override def onCompleted(): Unit = {
      println(s"$id completed.")
      self ! PoisonPill
    }

    override def onNext(value: Incoming): Unit = {
      println(s"$id got: $value")
      self ! value
    }
  }
}
