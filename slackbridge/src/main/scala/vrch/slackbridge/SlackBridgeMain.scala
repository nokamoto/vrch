package vrch.slackbridge

import java.net.URL
import java.util.concurrent.atomic.AtomicLong

import com.github.andyglow.websocket.WebsocketClient
import io.grpc.netty.NettyChannelBuilder
import play.api.libs.json.Json
import vrch.grpc.GcpApiKeyInterceptor
import vrch.grpc.ImplicitProperty._
import vrch.{Request, VrchServiceGrpc}

import scalaj.http.{Http, MultiPart}

object SlackBridgeMain {
  def main(args: Array[String]): Unit = {
    val slackUrl = "slack.url".stringProp
    val slackToken = "slack.token".stringProp
    val vrchHost = "vrch.host".stringProp
    val vrchPort = "vrch.port".intProp
    val apikey = "gcp.apikey".stringProp

    val channel = NettyChannelBuilder.forAddress(vrchHost, vrchPort).
      usePlaintext(true).intercept(new GcpApiKeyInterceptor(apikey)).build()
    val stub = VrchServiceGrpc.blockingStub(channel)

    val connected =
      Http(new URL(new URL(slackUrl), "/api/rtm.connect").toString).
        postForm(Seq(("token", slackToken))).asString

    val rtm = Json.parse(connected.body)

    println(rtm)

    val wss = rtm.\("url").as[String]
    val self = rtm.\("self").\("id").as[String]
    var context: String = ""
    val id = new AtomicLong(0)
    val ack = new AtomicLong(0)

    val client = WebsocketClient[String](wss) {
      case str =>
        try {
          val input = Json.parse(str)
          println(input)

          if (input.\("type").as[String] == "message" && input.\("user").as[String] != self) {
            val channel = input.\("channel").as[String]
            val text = input.\("text").as[String]

            println(s"$channel: $text")

            val res = stub.talk(Request().update(_.dialogue.text.text := text, _.dialogue.context := context))
            context = res.getDialogue.context

            val upload = Http(new URL(new URL(slackUrl), "/api/files.upload").toString).
              params(
                Seq(
                  ("token", slackToken), ("filetype", "auto"), ("channels", channel)
                )
              ).
              postMulti(
                MultiPart(
                  name = "file",
                  filename = s"${res.getDialogue.getText.text}.wav",
                  mime = "audio/wav",
                  data =  res.getVoice.voice.toByteArray
                )
              )

            println(upload.asString)

          } else if (input.\("type").as[String] == "pong") {
            ack.set(input.\("reply_to").as[Long])
          }
        } catch {
          case t: Throwable =>
            println(t)
        }
    }

    val websocket = client.open()

    while (true) {
      Thread.sleep(10 * 1000)
//      if (ack.get() != id.get()) {
//        throw new RuntimeException(s"ping/pong failed.")
//      }
      websocket ! Json.obj("id" -> id.incrementAndGet(), "type" -> "ping").toString()
    }
  }
}
