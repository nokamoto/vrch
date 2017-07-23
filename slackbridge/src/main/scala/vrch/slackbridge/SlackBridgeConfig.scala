package vrch.slackbridge

import vrch.grpc.ImplicitProperty._
import vrch.slackbridge.SlackBridgeConfig.GrpcConfig
import vrch.slackbridge.firebase.FirebaseConfig
import vrch.slackbridge.slack.SlackConfig

import scala.util.Try

case class SlackBridgeConfig(slack: SlackConfig, grpc: GrpcConfig, standalone: Boolean)

object SlackBridgeConfig {
  case class GrpcConfig(host: String, port: Int, apiKey: String)

  object GrpcConfig {
    lazy val default = GrpcConfig(host = "grpc.host".stringProp, port = "grpc.port".intProp, apiKey = "grpc.apikey".stringProp)
  }

  lazy val default: SlackBridgeConfig = {
    SlackBridgeConfig(
      slack = SlackConfig.default,
      grpc = GrpcConfig.default,
      standalone = Try(FirebaseConfig.default).isFailure)
  }
}
