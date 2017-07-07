package vrch.vrgrpc

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.routing.{RoundRobinRoutingLogic, Router}
import vrch.ClusterInfo.Node
import vrch.{ClusterInfo, Text}
import vrch.vrgrpc.VrClusterActor.{Info, Join}

class VrClusterActor extends Actor {
  private[this] var router = Router(RoundRobinRoutingLogic(), Vector.empty)

  def receive: Receive = {
    case Join(ref) =>
      router = router.addRoutee(ref)
      context.watch(ref)

    case Info =>
      sender() ! ClusterInfo().update(_.node := router.routees.map(r => Node().update(_.name := r.toString)))

    case Terminated(ref) =>
      router = router.removeRoutee(ref)

    case text: Text =>
      router.route(message = text, sender = sender())
  }
}

object VrClusterActor {
  case class Join(ref: ActorRef)

  case object Info

  def props: Props = Props(new VrClusterActor)
}
