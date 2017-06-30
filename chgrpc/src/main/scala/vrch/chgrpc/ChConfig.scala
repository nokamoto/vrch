package vrch.chgrpc

import scala.concurrent.duration._

case class ChConfig(apiKey: String, port: Int, concurrency: Int, shutdownTimeout: FiniteDuration)

trait UseChConfig {
  def chConfig: ChConfig
}

trait MixinChConfig extends UseChConfig {
  override val chConfig: ChConfig = {
    ChConfig(
      apiKey = sys.props.getOrElse("apikey", throw new UninitializedError),
      port = 9001,
      concurrency = 3,
      shutdownTimeout = 10.seconds
    )
  }
}
