package vrch.vrchgrpc

import io.grpc.ServerServiceDefinition
import vrch.chgrpc.MixinChConfig
import vrch.{VrClusterServiceGrpc, VrchServiceGrpc}
import vrch.grpc.{MixinExecutionContext, ServerMain}
import vrch.vrgrpc.{MixinVrCluster, MixinVrClusterService, MixinVrConfig, UseVrClusterService}

import scala.concurrent.ExecutionContext

trait VrchGrpc extends ServerMain with UseVrchService with UseVrClusterService {
  override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(VrchServiceGrpc.bindService(vrchService, _), VrClusterServiceGrpc.bindService(vrClusterService, _))
  }
}

object VrchGrpcMain extends VrchGrpc
  with MixinVrchService with MixinVrchConfig with MixinExecutionContext with MixinVrClusterService
  with MixinVrCluster with MixinChConfig with MixinVrConfig
