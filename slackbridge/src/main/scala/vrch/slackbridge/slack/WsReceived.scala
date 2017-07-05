package vrch.slackbridge.slack

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import play.api.libs.json.{JsValue, Json}
import vrch.Request
import vrch.VrchServiceGrpc.VrchServiceBlockingStub

import scalaj.http.MultiPart

class WsReceived(activeChannel: SlackChannel,
                 context: AtomicReference[String],
                 stub: VrchServiceBlockingStub,
                 slackApi: SlackApi,
                 ack: AtomicLong,
                 connected: RtmConnected) {

  private[this] def message(input: JsValue): Unit = {
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
          data = res.getVoice.voice.toByteArray
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

  val received: PartialFunction[String, Unit] = {
    case str =>
      try {
        val input = Json.parse(str)

        input.\("type").as[String] match {
          case "message" =>
            println(str.take(100))
            message(input)

          case "pong" =>
            println(str.take(100))
            ack.set(input.as[RtmPong].reply_to)

          case "presence_change" =>
            // nop

          case _ =>
            println(str.take(100))
        }
      } catch {
        case t: Throwable =>
          println(t)
      }
  }
}
