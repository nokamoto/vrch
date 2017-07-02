package vrch.chgrpc

import vrch.grpc.ImplicitProperty._

case class ChConfig(apiKey: String)

trait UseChConfig {
  def chConfig: ChConfig
}

trait MixinChConfig extends UseChConfig {
  override val chConfig: ChConfig = {
    ChConfig(apiKey = "apikey".stringProp)
  }
}
