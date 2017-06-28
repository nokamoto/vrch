package vrch.vr

import scala.concurrent.duration._

case class VrConfig(port: Int, concurrency: Int, shutdownTimeout: FiniteDuration, requestTimeout: FiniteDuration)

trait UseVrConfig {
  def vrConfig: VrConfig
}

trait MixinVrConfig extends UseVrConfig {
  override val vrConfig: VrConfig = {
    VrConfig(port = 9000, concurrency = 3, shutdownTimeout = 10.seconds, requestTimeout = 10.seconds)
  }
}
