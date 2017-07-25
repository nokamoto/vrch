package vrch.vrgrpc

import akka.actor.ActorSystem
import akka.pattern.ask
import io.grpc.stub.StreamObserver
import vrch.ImplicitDuration._
import vrch._
import vrch.vrgrpc.VrActor.IncomingObserver
import vrch.vrgrpc.VrClusterActor.{Info, Join}
import vrchcfg.VrCfg

import scala.concurrent.{Await, Future}

trait VrCluster extends UseVrConfig with Logger {
  private[this] val system = ActorSystem("vr-cluster")

  private[this] val cluster = system.actorOf(VrClusterActor.props)

  def join(out: StreamObserver[Outgoing]): IncomingObserver = {
    val ref = system.actorOf(VrActor.props(out))

    cluster ! Join(ref)
    logger.info(s"join: $ref")

    new IncomingObserver(self = ref)
  }

  def info: Future[ClusterInfo] = cluster.ask(Info)(vrConfig.getRequestTimeout.duration).mapTo[ClusterInfo]

  def talk(text: Text): Future[Voice] = cluster.ask(text)(vrConfig.getRequestTimeout.duration).mapTo[Voice]

  def shutdown(): Unit = {
    logger.info(s"shutdown: $system")
    Await.result(system.terminate(), vrConfig.getShutdownTimeout.duration)
  }
}

trait UseVrCluster {
  def vrCluster: VrCluster
}

trait MixinVrCluster extends UseVrCluster with UseVrConfig { self =>
  override val vrCluster: VrCluster = new VrCluster {
    override def vrConfig: VrCfg = self.vrConfig
  }
}
