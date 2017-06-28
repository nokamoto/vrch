package vrch.vr

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.routing.{RoundRobinRoutingLogic, Router}
import vrch.Text
import vrch.vr.VrClusterActor.Join

class VrClusterActor extends Actor {
  private[this] var router = Router(RoundRobinRoutingLogic(), Vector.empty)

  def receive: Receive = {
    case Join(ref) =>
      router = router.addRoutee(ref)
      context.watch(ref)

    case Terminated(ref) =>
      router = router.removeRoutee(ref)

    case text: Text =>
      router.route(message = text, sender = sender())
  }
}

object VrClusterActor {
  case class Join(ref: ActorRef)

  def props: Props = Props(new VrClusterActor)
}
