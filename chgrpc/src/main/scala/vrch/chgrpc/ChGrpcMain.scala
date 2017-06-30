package vrch.chgrpc

import io.grpc.ServerServiceDefinition
import vrch.ChServiceGrpc
import vrch.grpc.ServerMain

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait ChGrpc extends ServerMain with UseChService with UseChConfig {
  override protected[this] def port: Int = chConfig.port

  override protected[this] def shutdownTimeout: FiniteDuration = chConfig.shutdownTimeout

  override protected[this] def concurrency: Int = chConfig.concurrency

  override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(ChServiceGrpc.bindService(chService, _))
  }
}

object ChGrpcMain extends ChGrpc with MixinChService with MixinChConfig
