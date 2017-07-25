package vrch.vrchgrpc

import akka.actor.ActorSystem
import io.grpc.ServerServiceDefinition
import vrch.grpc.{MixinExecutionContext, ServerMain}
import vrch.vrgrpc.{MixinVrCluster, MixinVrClusterService, UseVrClusterService}
import vrch.{Logger, VrClusterServiceGrpc, VrchServiceGrpc}

import scala.concurrent.ExecutionContext

trait VrchGrpc extends ServerMain with UseVrchService with UseVrClusterService {
  override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(VrchServiceGrpc.bindService(vrchService, _), VrClusterServiceGrpc.bindService(vrClusterService, _))
  }
}

object VrchGrpcMain extends VrchGrpc
  with MixinVrchService with MixinVrchConfig with MixinExecutionContext with MixinVrClusterService
  with MixinVrCluster with Logger {

  override def main(args: Array[String]): Unit = {
    logger.info(s"props: $vrchConfig")
    super.main(args)
  }

  override def actorSystem: ActorSystem = ActorSystem("vrchgrpc")
}
