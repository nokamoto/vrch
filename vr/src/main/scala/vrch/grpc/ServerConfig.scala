package vrch.grpc

import scala.concurrent.duration.FiniteDuration

case class ServerConfig(port: Int, concurrency: Int, shutdownTimeout: FiniteDuration)

trait UseServerConfig {
  def serverConfig: ServerConfig
}
