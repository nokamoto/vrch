package vrch.vrgrpc

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.grpc.stub.StreamObserver
import vrch.ImplicitDuration._
import vrch._
import vrch.vrgrpc.VrActor.{KeepAlive, VoiceResult, VrSetting}
import vrchcfg.VrCfg

class VrActor(setting: VrSetting) extends Actor with ActorLogging {
  import context._
  import setting._

  private[this] var last: Option[ActorRef] = None

  private[this] val INTERVAL = cfg.getKeepaliveInterval.duration

  private[this] val KEEP_ALIVE_TIMEOUT = cfg.getKeepaliveTimeout.duration

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
        log.error("{} keep-alive failed: {} + {} < {}", self, ts, KEEP_ALIVE_TIMEOUT, now)
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

  case class VrSetting(out: StreamObserver[Outgoing], cfg: VrCfg)

  def props(setting: VrSetting): Props = Props(new VrActor(setting))

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
