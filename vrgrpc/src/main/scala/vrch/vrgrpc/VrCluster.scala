package vrch.vrgrpc

import akka.actor.ActorSystem
import akka.pattern.ask
import io.grpc.stub.StreamObserver
import vrch.vrgrpc.VrActor.IncomingObserver
import vrch.vrgrpc.VrClusterActor.{Info, Join}
import vrch.{ClusterInfo, Outgoing, Text, Voice}

import scala.concurrent.{Await, Future}

trait VrCluster extends UseVrConfig {
  private[this] val system = ActorSystem("vr-cluster")

  private[this] val cluster = system.actorOf(VrClusterActor.props)

  def join(out: StreamObserver[Outgoing]): IncomingObserver = {
    val ref = system.actorOf(VrActor.props(out))
    cluster ! Join(ref)
    println(s"join $ref")
    new IncomingObserver(self = ref)
  }

  def info: Future[ClusterInfo] = cluster.ask(Info)(vrConfig.requestTimeout).mapTo[ClusterInfo]

  def talk(text: Text): Future[Voice] = cluster.ask(text)(vrConfig.requestTimeout).mapTo[Voice]

  def shutdown(): Unit = {
    println(s"shutdown $system")
    Await.result(system.terminate(), vrConfig.shutdownTimeout)
  }
}

trait UseVrCluster {
  def vrCluster: VrCluster
}

trait MixinVrCluster extends UseVrCluster with UseVrConfig { self =>
  override val vrCluster: VrCluster = new VrCluster {
    override def vrConfig: VrConfig = self.vrConfig
  }
}
