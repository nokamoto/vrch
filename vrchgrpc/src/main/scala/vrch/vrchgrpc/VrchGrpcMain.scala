package vrch.vrchgrpc

import io.grpc.ServerServiceDefinition
import vrch.VrchServiceGrpc
import vrch.grpc.{MixinExecutionContext, ServerMain}

import scala.concurrent.ExecutionContext

trait VrchGrpc extends ServerMain with UseVrchService {
  override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(VrchServiceGrpc.bindService(vrchService, _))
  }
}

object VrchGrpcMain extends VrchGrpc with MixinVrchService with MixinVrchConfig with MixinExecutionContext
