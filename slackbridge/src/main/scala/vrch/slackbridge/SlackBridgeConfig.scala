package vrch.slackbridge

import vrch.slackbridge.SlackBridgeConfig.GrpcConfig
import vrch.slackbridge.firebase.FirebaseConfig
import vrch.slackbridge.slack.SlackConfig
import vrch.grpc.ImplicitProperty._

case class SlackBridgeConfig(slack: SlackConfig, firebase: FirebaseConfig, grpc: GrpcConfig)

object SlackBridgeConfig {
  case class GrpcConfig(host: String, port: Int, apiKey: String)

  object GrpcConfig {
    lazy val default = GrpcConfig(host = "grpc.host".stringProp, port = "grpc.port".intProp, apiKey = "grpc.apikey".stringProp)
  }

  lazy val default = SlackBridgeConfig(SlackConfig.default, FirebaseConfig.default, GrpcConfig.default)
}
