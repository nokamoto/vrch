package vrch.vrgrpc

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.grpc.stub.StreamObserver
import vrch.{Incoming, Logger, Outgoing, Text}

class VrActor(out: StreamObserver[Outgoing]) extends Actor with ActorLogging {
  private[this] var last: Option[ActorRef] = None

  private[this] def outgoing(value: Outgoing): Unit = {
    try {
      out.onNext(value)
    } catch {
      case e: Throwable =>
        log.error(e, "{} outgoing failed.", self)
        context.stop(self)
    }
  }

  override def postStop(): Unit = {
    super.postStop()

    log.info("{} post stop called.", self)

    out.onCompleted()
  }

  override def receive: Receive = {
    case text: Text =>
      last match {
        case Some(_) =>
          out.onError(new RuntimeException(s"$self inconsistent state or too many request"))

        case None =>
          outgoing(Outgoing().update(_.text := text))
          last = Some(sender())
      }

    case in: Incoming =>
      in match {
        case _ if in.keeplive != 0 =>
          outgoing(Outgoing().update(_.keepalive := in.keeplive))

        case _ =>
          last match {
            case Some(ref) =>
              ref ! in.getVoice
              last = None

            case None =>
              out.onError(new RuntimeException(s"$self inconsistent state or too many request"))
          }
      }
  }
}

object VrActor extends Logger {
  def props(out: StreamObserver[Outgoing]): Props = Props(new VrActor(out))

  class IncomingObserver(self: ActorRef) extends StreamObserver[Incoming] {
    override def onError(t: Throwable): Unit = {
      logger.error(s"$self error.", t)
      self ! PoisonPill
    }

    override def onCompleted(): Unit = {
      logger.info(s"$self completed.")
      self ! PoisonPill
    }

    override def onNext(value: Incoming): Unit = {
      logger.debug(s"$self next: $value")
      self ! value
    }
  }
}
