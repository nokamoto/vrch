package vrch.vrgrpc

import io.grpc.ServerServiceDefinition
import vrch.grpc.ServerMain
import vrch.{VrClusterServiceGrpc, VrServiceGrpc}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait Vr extends ServerMain with UseVrConfig with UseVrService with UseVrClusterService with UseVrCluster  {
  protected[this] override lazy val port: Int = vrConfig.port

  protected[this] override lazy val shutdownTimeout: FiniteDuration = vrConfig.shutdownTimeout

  protected[this] override lazy val concurrency: Int = vrConfig.concurrency

  protected[this] override def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
    Seq(
      VrServiceGrpc.bindService(vrService, _),
      VrClusterServiceGrpc.bindService(vrClusterService, _)
    )
  }
}

object VrMain extends Vr with MixinVrConfig with MixinVrService with MixinVrClusterService with MixinVrCluster
