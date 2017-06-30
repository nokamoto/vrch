package vrch.vrgrpc

import io.grpc.ServerServiceDefinition
import vrch.grpc.{MixinExecutionContext, ServerMain}
import vrch.{VrClusterServiceGrpc, VrServiceGrpc}

import scala.concurrent.ExecutionContext

trait Vr extends ServerMain with UseVrConfig with UseVrService with UseVrClusterService with UseVrCluster  {
  protected[this] override def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(
      VrServiceGrpc.bindService(vrService, _),
      VrClusterServiceGrpc.bindService(vrClusterService, _)
    )
  }
}

object VrMain extends Vr
  with MixinVrConfig with MixinVrService with MixinVrClusterService with MixinVrCluster with MixinExecutionContext
