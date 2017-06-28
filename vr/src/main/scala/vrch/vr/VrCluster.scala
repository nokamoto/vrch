package vrch.vr

import java.util.UUID

import akka.actor.ActorSystem
import akka.pattern.ask
import io.grpc.stub.StreamObserver
import vrch.vr.VrActor.IncomingObserver
import vrch.vr.VrClusterActor.Join
import vrch.{Outgoing, Text, Voice}

import scala.concurrent.{Await, Future}

trait VrCluster extends UseVrConfig {
  private[this] val system = ActorSystem("vr-cluster")

  private[this] val cluster = system.actorOf(VrClusterActor.props)

  def join(out: StreamObserver[Outgoing]): IncomingObserver = {
    val ref = system.actorOf(VrActor.props(out))
    cluster ! Join(ref)
    val id = UUID.randomUUID()
    new IncomingObserver(id = id, self = ref)
  }

  def talk(text: Text): Future[Voice] = cluster.ask(text)(vrConfig.requestTimeout).mapTo[Voice]

  def shutdown(): Unit = {
    Await.result(system.terminate(), vrConfig.shutdownTimeout)
  }
}

trait UseVrCluster {
  def vrCluster: VrCluster
}

trait MixinVrCluster extends UseVrCluster {
  override val vrCluster: VrCluster = new VrCluster with MixinVrConfig
}
