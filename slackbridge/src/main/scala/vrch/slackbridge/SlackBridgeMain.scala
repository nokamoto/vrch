package vrch.slackbridge

import com.typesafe.config.ConfigFactory
import vrch.Logger
import vrchcfg.{Cfg, Slackbridge, SlackbridgeCfg}

object SlackBridgeMain extends Logger {
  def main(args: Array[String]): Unit = {
    val cfg = Cfg[SlackbridgeCfg, Slackbridge.SlackbridgeCfg.Builder](
      newBuilder = Slackbridge.SlackbridgeCfg.newBuilder(),
      build = { builder => SlackbridgeCfg.fromJavaProto(builder.build()) }
    )

    val config = cfg.proto(ConfigFactory.load())

    logger.debug(s"props: $config")

    val bridge = if (config.firebase.isEmpty) {
      new StandaloneSlackBridge(config)
    } else {
      new FirebaseSlackBridge(config)
    }

    bridge.run()
  }
}
