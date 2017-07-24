package vrch.vrgrpc

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.grpc.stub.StreamObserver
import vrch.vrgrpc.VrActor.{KeepAlive, VoiceResult}
import vrch._

import scala.concurrent.duration._

class VrActor(out: StreamObserver[Outgoing]) extends Actor with ActorLogging {
  import context._

  private[this] var last: Option[ActorRef] = None

  private[this] val INTERVAL = 10.seconds

  private[this] val KEEP_ALIVE_TIMEOUT = 30.seconds

  private[this] val keepAlive = context.system.scheduler.schedule(INTERVAL, INTERVAL, self, KeepAlive)

  private[this] var ts = System.currentTimeMillis()

  private[this] def outgoing(value: Outgoing): Unit = {
    try {
      out.onNext(value)
    } catch {
      case e: Throwable =>
        log.error(e, "{} outgoing failed.", self)
        context.stop(self)
    }
  }

  override def preStart(): Unit = super.preStart()

  override def postStop(): Unit = {
    super.postStop()

    log.info("{} post stop called.", self)

    try {
      out.onCompleted()
    } catch {
      case e: Exception => log.warning(s"{} complete failed. {}", self, e)
    }

    keepAlive.cancel()
  }

  override def receive: Receive = {
    case KeepAlive =>
      val now = System.currentTimeMillis()

      if (ts + KEEP_ALIVE_TIMEOUT.toMillis < now) {
        log.error("{} keep-alive failed: {} + 60 seconds < {}", self, ts, now)
        context.stop(self)
      }

    case text: Text =>
      last match {
        case Some(_) =>
          out.onError(new RuntimeException(s"$self inconsistent state"))
          context.stop(self)

        case None =>
          outgoing(Outgoing().update(_.text := text))
          last = Some(sender())
      }

    case in: Incoming =>
      ts = System.currentTimeMillis()

      in match {
        case _ if in.keeplive != 0 =>
          outgoing(Outgoing().update(_.keepalive := in.keeplive))

        case _ =>
          last match {
            case Some(ref) =>
              ref ! VoiceResult(in.getVoice, self)
              last = None

            case None =>
              out.onError(new RuntimeException(s"$self inconsistent state"))
              context.stop(self)
          }
      }
  }
}

object VrActor extends Logger {
  private case object KeepAlive

  case class VoiceResult(voice: Voice, vr: ActorRef)

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
