package vrch.vrgrpc

import scala.concurrent.duration._

case class VrConfig(shutdownTimeout: FiniteDuration, requestTimeout: FiniteDuration)

trait UseVrConfig {
  def vrConfig: VrConfig
}

trait MixinVrConfig extends UseVrConfig {
  override val vrConfig: VrConfig = {
    VrConfig(shutdownTimeout = 10.seconds, requestTimeout = 10.seconds)
  }
}
