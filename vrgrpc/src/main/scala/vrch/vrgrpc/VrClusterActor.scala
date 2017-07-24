package vrch.vrgrpc

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import vrch.ClusterInfo.Node
import vrch.vrgrpc.VrActor.VoiceResult
import vrch.vrgrpc.VrClusterActor.{Info, Join, NotReady}
import vrch.{ClusterInfo, Text}

import scala.collection.immutable.Queue

class VrClusterActor extends Actor with ActorLogging {
  private[this] case class Waiting(sender: ActorRef, ts: Long = System.currentTimeMillis())

  private[this] var ready = Queue.empty[ActorRef]

  private[this] var waiting = Map.empty[ActorRef, Waiting]

  def receive: Receive = {
    case Join(ref) =>
      ready = ready.enqueue(ref)

      context.watch(ref)

    case Info =>
      sender() ! ClusterInfo().update(
        _.node := ready.map(r => Node().update(_.name := r.path.name)) ++
          waiting.map(r => Node().update(_.name := r._1.path.name))
      )

    case Terminated(ref) =>
      ready = ready.filterNot(_ == ref)

      waiting = waiting.filterNot(_._1 == ref)

    case text: Text =>
      log.info("ready={}, waiting={}: {}", ready.size, waiting.size, text)

      if (ready.nonEmpty) {
        val (head, next) = ready.dequeue

        ready = next

        waiting = waiting + (head -> Waiting(sender()))

        head ! text
      } else {
        log.error("ready queue is empty.")

        sender() ! NotReady
      }

    case voice: VoiceResult =>
      waiting.get(voice.vr) match {
        case Some(elem) =>
          log.info("sender={}, node={}", elem.sender, voice.vr)

          waiting = waiting.filterNot(_._1 == voice.vr)

          ready = ready.enqueue(voice.vr)

          elem.sender ! voice.voice

        case None =>
          log.error("{} already terminated.", voice.vr)
      }
  }
}

object VrClusterActor {
  case class Join(ref: ActorRef)

  case object Info

  case object NotReady

  def props: Props = Props(new VrClusterActor)
}
