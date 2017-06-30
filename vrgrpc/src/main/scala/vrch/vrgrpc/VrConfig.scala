package vrch.vrgrpc

import vrch.grpc.ImplicitProperty._
import vrch.grpc.{ServerConfig, UseServerConfig}

import scala.concurrent.duration._

case class VrConfig(port: Int, concurrency: Int, shutdownTimeout: FiniteDuration, requestTimeout: FiniteDuration)

trait UseVrConfig extends UseServerConfig {
  def vrConfig: VrConfig

  override def serverConfig: ServerConfig = {
    ServerConfig(port = vrConfig.port, concurrency = vrConfig.concurrency, shutdownTimeout = vrConfig.shutdownTimeout)
  }
}

trait MixinVrConfig extends UseVrConfig {
  override val vrConfig: VrConfig = {
    VrConfig(port = "grpc.port".intProp, concurrency = 3, shutdownTimeout = 10.seconds, requestTimeout = 10.seconds)
  }
}
