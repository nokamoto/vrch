package vrch.vrchgrpc

import vrch.grpc.{ServerConfig, UseServerConfig}

import scala.concurrent.duration._
import vrch.grpc.ImplicitProperty._

case class VrchConfig(port: Int,
                      concurrency: Int,
                      shutdownTimeout: FiniteDuration,
                      vrHost: String,
                      vrPort: Int,
                      chHost: String,
                      chPort: Int)

trait UseVrchConfig extends UseServerConfig {
  def vrchConfig: VrchConfig

  override def serverConfig: ServerConfig = {
    ServerConfig(port = vrchConfig.port, concurrency = vrchConfig.concurrency, shutdownTimeout = vrchConfig.shutdownTimeout)
  }
}

trait MixinVrchConfig extends UseVrchConfig {
  override def vrchConfig: VrchConfig = {
    VrchConfig(
      port = "grpc.port".intProp,
      concurrency = 3,
      shutdownTimeout = 10.seconds,
      vrHost = "grpc.vr.host".stringProp,
      vrPort = "grpc.vr.port".intProp,
      chHost = "grpc.ch.host".stringProp,
      chPort = "grpc.ch.port".intProp
    )
  }
}
