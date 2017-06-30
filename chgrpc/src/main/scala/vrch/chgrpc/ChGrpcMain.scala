package vrch.chgrpc

import io.grpc.ServerServiceDefinition
import vrch.ChServiceGrpc
import vrch.grpc.{MixinExecutionContext, ServerMain}

import scala.concurrent.ExecutionContext

trait ChGrpc extends ServerMain with UseChService with UseChConfig {
  override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(ChServiceGrpc.bindService(chService, _))
  }
}

object ChGrpcMain extends ChGrpc
  with MixinChService with MixinChConfig with MixinExecutionContext
