package vrch.chgrpc

import vrch.grpc.ImplicitProperty._

case class ChConfig(apiKey: String, url: String)

trait UseChConfig {
  def chConfig: ChConfig
}

trait MixinChConfig extends UseChConfig {
  override val chConfig: ChConfig = {
    ChConfig(apiKey = "apikey".stringProp, url = "https://api.apigw.smt.docomo.ne.jp")
  }
}
