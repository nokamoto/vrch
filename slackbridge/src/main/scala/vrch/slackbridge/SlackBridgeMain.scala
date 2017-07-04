package vrch.slackbridge

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import com.github.andyglow.websocket.WebsocketClient
import io.grpc.netty.NettyChannelBuilder
import play.api.libs.json._
import vrch.grpc.GcpApiKeyInterceptor
import vrch.grpc.ImplicitProperty._
import vrch.slackbridge.slack._
import vrch.{Request, VrchServiceGrpc}

import scalaj.http.MultiPart

object SlackBridgeMain {
  def main(args: Array[String]): Unit = {
    val slackUrl = "slack.url".stringProp
    val slackToken = "slack.token".stringProp
    val slackChannel = "slack.channel".stringProp
    val vrchHost = "vrch.host".stringProp
    val vrchPort = "vrch.port".intProp
    val apikey = "gcp.apikey".stringProp
    val context = new AtomicReference[String]("vrch.context".stringProp)

    val channel = NettyChannelBuilder.forAddress(vrchHost, vrchPort).
      usePlaintext(true).intercept(new GcpApiKeyInterceptor(apikey)).build()
    val stub = VrchServiceGrpc.blockingStub(channel)

    val slackApi = SlackApi(url = slackUrl, token = slackToken)

    val connected = slackApi.post[RtmConnected]("/api/rtm.connect")

    println(connected)

    val channels = slackApi.post[SlackChannels](
      "/api/channels.list",
      ("exclude_archived", "true"),
      ("exclude_members", "true")
    )

    val activeChannel = channels.channels.find(_.name == slackChannel).
      getOrElse(throw new RuntimeException(s"#$slackChannel not found."))

    println(activeChannel)

    val id = new AtomicLong(0)
    val ack = new AtomicLong(0)

    val client = WebsocketClient[String](connected.url) {
      case str =>
        try {
          val input = Json.parse(str)

          println(str.take(100))

          input.\("type").as[String] match {
            case "message" =>
              input.as[RtmMessage] match {
                case message if message.channel != activeChannel.id =>
                  println(s"${context.get()}: do not respond to ${message.channel}")

                case message if message.user == connected.self.id =>
                  println(s"${context.get()}: do not respond to myself")

                case message if message.text.contains("has joined the channel") =>
                  println(s"${context.get()}: do not respond to joined message")

                case message =>
                  val req = Request().update(_.dialogue.text.text := message.text, _.dialogue.context := context.get())
                  val res = stub.talk(req)

                  println(s"${context.get()}: ${req.toString.take(100)} - ${res.toString.take(100)}")

                  context.set(res.getDialogue.context)

                  val file = MultiPart(
                    name = "file",
                    filename = s"${res.getDialogue.getText.text}.wav",
                    mime = "audio/wav",
                    data =  res.getVoice.voice.toByteArray
                  )

                  val upload = slackApi.postFile[JsValue](
                    "/api/files.upload",
                    file,
                    ("filetype", "auto"),
                    ("channels", activeChannel.id)
                  )

                  println(upload)
              }

            case "pong" =>
              ack.set(input.as[RtmPong].reply_to)

            case _ =>
              // nop
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
