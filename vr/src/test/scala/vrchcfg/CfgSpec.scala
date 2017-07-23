package vrchcfg

import java.util.UUID

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

class CfgSpec extends FlatSpec {
  it should "convert typesafe config to protocol buffer" in {
    val grpc = GrpcCfg().update(_.host := "localhost", _.port := 80, _.apiKey := UUID.randomUUID().toString)

    val slack = SlackCfg().update(_.url := "https://slack.com", _.token := UUID.randomUUID().toString, _.channel := "general")

    val firebase = FirebaseCfg().update(
      _.adminsdkJsonPath := "/docker/vrchandroid-firebase-adminsdk.json",
      _.adminsdkUrl := "https://vrchandroid.com",
      _.storageBucket := "vrchandroid.example")

    val expected = SlackbridgeCfg().update(_.grpc := grpc, _.slack := slack, _.firebase := firebase)

    val s =
      s"""grpc {
         |  host = ${grpc.host}
         |  port = ${grpc.port}
         |  api_key = ${grpc.apiKey}
         |}
         |slack {
         |  url = "${slack.url}"
         |  token = ${slack.token}
         |  channel = ${slack.channel}
         |}
         |firebase {
         |  adminsdk_json_path = "${firebase.adminsdkJsonPath}"
         |  adminsdk_url = "${firebase.adminsdkUrl}"
         |  storage_bucket = "${firebase.storageBucket}"
         |}
       """.stripMargin

    val cfg = new Cfg[SlackbridgeCfg, Slackbridge.SlackbridgeCfg.Builder] {
      override def newBuilder: Builder = Slackbridge.SlackbridgeCfg.newBuilder()

      override def build(builder: Builder): SlackbridgeCfg = SlackbridgeCfg.fromJavaProto(builder.build())
    }

    val actual = cfg.proto(ConfigFactory.parseString(s))

    assert(actual === expected)
  }
}
