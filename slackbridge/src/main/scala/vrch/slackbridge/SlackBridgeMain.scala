package vrch.slackbridge

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import com.github.andyglow.websocket.WebsocketClient
import io.grpc.netty.NettyChannelBuilder
import play.api.libs.json._
import vrch.VrchServiceGrpc
import vrch.grpc.GcpApiKeyInterceptor
import vrch.grpc.ImplicitProperty._
import vrch.slackbridge.slack._

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

    val channels = slackApi.post[SlackChannels](
      "/api/channels.list",
      ("exclude_archived", "true"),
      ("exclude_members", "true")
    )

    val activeChannel = channels.channels.find(_.name == slackChannel).
      getOrElse(throw new RuntimeException(s"#$slackChannel not found."))

    val id = new AtomicLong(0)
    val ack = new AtomicLong(0)

    def newClient(): WebsocketClient[String] = {
      val connected = slackApi.post[RtmConnected]("/api/rtm.connect")
      val received = new WsReceived(
        activeChannel = activeChannel, context = context, stub = stub, ack = ack, connected = connected, slackApi = slackApi
      )

      WebsocketClient[String](connected.url)(received.received)
    }

    var client = newClient()
    var websocket = client.open()

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
