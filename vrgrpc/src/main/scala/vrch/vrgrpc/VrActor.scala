package vrch.vrgrpc

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
          out.onError(new RuntimeException("inconsistent state or too many request"))

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
          out.onError(new RuntimeException("inconsistent state or too many request"))
      }
  }
}

object VrActor {
  def props(out: StreamObserver[Outgoing]): Props = Props(new VrActor(out))

  class IncomingObserver(self: ActorRef) extends StreamObserver[Incoming] {
    override def onError(t: Throwable): Unit = {
      println(s"$self error: $t")
    }

    override def onCompleted(): Unit = {
      println(s"$self completed.")
      self ! PoisonPill
    }

    override def onNext(value: Incoming): Unit = {
      println(s"$self got: $value")
      self ! value
    }
  }
}
