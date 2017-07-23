package vrch.slackbridge

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import com.github.andyglow.websocket.WebsocketClient
import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder
import play.api.libs.json.{JsValue, Json}
import vrch.grpc.GcpApiKeyInterceptor
import vrch.slackbridge.slack._
import vrch.slackbridge.slack.value._
import vrch.{Logger, Request, Response, VrchServiceGrpc}

import scalaj.http.MultiPart

abstract class SlackBridge(protected[this] val config: SlackBridgeConfig) extends Logger {
  protected[this] def context: AtomicReference[String]

  private[this] val channel: ManagedChannel = {
    NettyChannelBuilder.forAddress(config.grpc.host, config.grpc.port).
      usePlaintext(true).intercept(new GcpApiKeyInterceptor(config.grpc.apiKey)).build()
  }

  private[this] def stub: VrchServiceGrpc.VrchServiceBlockingStub = VrchServiceGrpc.blockingStub(channel)

  protected[this] val slackApi = SlackApi(config.slack)

  protected[this] val listenedChannel: SlackChannel = {
    val channels = slackApi.post[SlackChannels](
      "/api/channels.list",
      ("exclude_archived", "true"),
      ("exclude_members", "true")
    )

    channels.channels.find(_.name == config.slack.channel).
      getOrElse(throw new RuntimeException(s"#${config.slack.channel} not found."))
  }

  private[this] val ack = new AtomicLong(0)

  private[this] def newClient(): WebsocketClient[String] = {
    val connected = slackApi.post[RtmConnected]("/api/rtm.connect")

    val received = new WsReceived(
      listenedChannel = listenedChannel,
      context = context,
      ack = ack,
      connected = connected,
      onMessage = { message =>
        val req = Request().update(_.dialogue.text.text := message.text, _.dialogue.context := context.get())
        logger.info(s"${context.get()}: $req")

        val res = stub.talk(req)
        logger.info(s"${context.get()}: $res")

        val last = context.getAndSet(res.getDialogue.context)
        if (context.get() != last) {
          logger.info(s"context changed: $last > ${context.get()}")
        }
      }
    )

    WebsocketClient[String](connected.url)(received.received)
  }

  protected[this] def upload(filename: String, bytes: Array[Byte]): Unit = {
    val file = MultiPart(
      name = "file",
      filename = filename,
      mime = "audio/wav",
      data = bytes
    )

    val upload = slackApi.postFile[JsValue](
      "/api/files.upload",
      file,
      ("filetype", "auto"),
      ("channels", listenedChannel.id)
    )

    logger.info(s"/api/files.upload - $upload")
  }

  protected[this] def call(req: Request, res: Response): Unit

  def run(): Unit = {
    val id = new AtomicLong(0)

    var client = newClient()

    var websocket = client.open()

    while (true) {
      Thread.sleep(10 * 1000)

      try {
        if (ack.get() != id.get()) {
          logger.info("ping/pong failed and shutdown now...")
          client.shutdownSync()

          logger.info("sleep 10 seconds...")
          Thread.sleep(10 * 1000)

          logger.info("reconnect...")
          client = newClient()
          websocket = client.open()

          id.set(0)
          ack.set(0)
        } else {
          val ping = Json.obj("id" -> id.incrementAndGet(), "type" -> "ping").toString()

          logger.info(ping)

          websocket ! ping
        }
      } catch {
        case t: Throwable =>
          logger.error("ping/pong exception raised.", t)
      }
    }
  }
}
