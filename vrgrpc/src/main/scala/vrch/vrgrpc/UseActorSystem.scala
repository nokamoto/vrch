package vrch.vrgrpc

import akka.actor.ActorSystem

trait UseActorSystem {
  def actorSystem: ActorSystem
}
