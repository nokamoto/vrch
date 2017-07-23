package vrch.slackbridge

import vrch.Logger
import vrch.slackbridge.firebase.FirebaseConfig

object SlackBridgeMain extends Logger {
  def main(args: Array[String]): Unit = {
    val config = SlackBridgeConfig.default
    logger.debug(s"props: $config")

    val bridge = if (config.standalone) {
      new StandaloneSlackBridge(config)
    } else {
      new FirebaseSlackBridge(config, FirebaseConfig.default)
    }

    bridge.run()
  }
}
