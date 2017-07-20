package vrch.slackbridge.slack

import vrch.grpc.ImplicitProperty._

case class SlackConfig(url: String, token: String, channel: String)

object SlackConfig {
  lazy val default: SlackConfig = {
    SlackConfig(url = "slack.url".stringProp, token = "slack.token".stringProp, channel = "slack.channel".stringProp)
  }
}