package vrch.chgrpc

import vrch.grpc.{ServerConfig, UseServerConfig}

import scala.concurrent.duration._
import vrch.grpc.ImplicitProperty._

case class ChConfig(apiKey: String, port: Int, concurrency: Int, shutdownTimeout: FiniteDuration)

trait UseChConfig extends UseServerConfig {
  def chConfig: ChConfig

  override def serverConfig: ServerConfig = {
    ServerConfig(port = chConfig.port, concurrency = chConfig.concurrency, shutdownTimeout = chConfig.shutdownTimeout)
  }
}

trait MixinChConfig extends UseChConfig {
  override val chConfig: ChConfig = {
    ChConfig(
      apiKey = "apikey".stringProp,
      port = "grpc.port".intProp,
      concurrency = 3,
      shutdownTimeout = 10.seconds
    )
  }
}
