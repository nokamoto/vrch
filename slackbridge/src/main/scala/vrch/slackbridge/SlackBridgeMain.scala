package vrch.slackbridge

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import com.github.andyglow.websocket.WebsocketClient
import io.grpc.netty.NettyChannelBuilder
import play.api.libs.json._
import vrch.VrchServiceGrpc
import vrch.grpc.GcpApiKeyInterceptor
import vrch.slackbridge.firebase.Platform.{Android, Slack}
import vrch.slackbridge.firebase.WhoAmI.{Kiritan, Self}
import vrch.slackbridge.firebase.{FirebaseMessageClient, FirebaseStorageClient}
import vrch.slackbridge.slack._

import scala.concurrent.Await
import scala.concurrent.duration._
import scalaj.http.MultiPart

object SlackBridgeMain {
  def main(args: Array[String]): Unit = {
    val config = SlackBridgeConfig.default

    println(config)

    val firebase = new FirebaseMessageClient(file = config.firebase.json, url = config.firebase.url, room = "lobby")
    val context = new AtomicReference[String](Await.result(firebase.context(), 10.seconds).context)

    val storage = new FirebaseStorageClient(file = config.firebase.json, bucket = config.firebase.bucket)

    val channel = NettyChannelBuilder.forAddress(config.grpc.host, config.grpc.port).
      usePlaintext(true).intercept(new GcpApiKeyInterceptor(config.grpc.apiKey)).build()
    val stub = VrchServiceGrpc.blockingStub(channel)

    val slackApi = SlackApi(url = config.slack.url, token = config.slack.token)

    val channels = slackApi.post[SlackChannels](
      "/api/channels.list",
      ("exclude_archived", "true"),
      ("exclude_members", "true")
    )

    val activeChannel = channels.channels.find(_.name == config.slack.channel).
      getOrElse(throw new RuntimeException(s"#${config.slack.channel} not found."))

    val id = new AtomicLong(0)
    val ack = new AtomicLong(0)

    def newClient(): WebsocketClient[String] = {
      val connected = slackApi.post[RtmConnected]("/api/rtm.connect")

      val received = new WsReceived(
        activeChannel = activeChannel,
        context = context,
        stub = stub,
        ack = ack,
        connected = connected,
        slackApi = slackApi,
        firebase = firebase,
        storage = storage
      )

      WebsocketClient[String](connected.url)(received.received)
    }

    var client = newClient()
    var websocket = client.open()

    firebase.addListener { message =>
      message.who match {
        case Self =>
          message.platform match {
            case Android =>
              slackApi.post[JsValue](
                "/api/chat.postMessage",
                ("channel", activeChannel.id),
                ("text", message.message),
                ("username", s"${message.displayName}@Android")
              )

            case Slack => // nop
          }

        case Kiritan =>
          val bytes: Array[Byte] = storage.download(s"${message.uuid}.wav")

          val file = MultiPart(
            name = "file",
            filename = s"${message.message}.wav",
            mime = "audio/wav",
            data = bytes
          )

          val upload = slackApi.postFile[JsValue](
            "/api/files.upload",
            file,
            ("filetype", "auto"),
            ("channels", activeChannel.id)
          )

          println(upload)
      }
    }

    while (true) {
      Thread.sleep(10 * 1000)

      try {
        if (ack.get() != id.get()) {
          println("ping/pong failed and shutdown now...")
          client.shutdownSync()

          println("sleep 10 seconds...")
          Thread.sleep(10 * 1000)

          println("reconnect...")
          client = newClient()
          websocket = client.open()

          id.set(0)
          ack.set(0)
        } else {
          val ping = Json.obj("id" -> id.incrementAndGet(), "type" -> "ping").toString()

          println(ping)

          websocket ! ping
        }
      } catch {
        case t: Throwable =>
          println(t)
      }
    }
  }
}
