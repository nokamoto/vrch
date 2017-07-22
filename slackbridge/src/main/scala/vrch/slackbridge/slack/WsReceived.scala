package vrch.slackbridge.slack

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import play.api.libs.json.{JsValue, Json}
import vrch.{Logger, Request}
import vrch.VrchServiceGrpc.VrchServiceBlockingStub
import vrch.slackbridge.firebase.{FirebaseMessage, FirebaseMessageClient, FirebaseStorageClient}

import scala.concurrent.Await
import scala.concurrent.duration._

class WsReceived(activeChannel: SlackChannel,
                 context: AtomicReference[String],
                 stub: VrchServiceBlockingStub,
                 slackApi: SlackApi,
                 ack: AtomicLong,
                 connected: RtmConnected,
                 firebase: FirebaseMessageClient,
                 storage: FirebaseStorageClient) extends Logger {

  private[this] def message(input: JsValue): Unit = synchronized {
    input.as[RtmMessage] match {
      case message if message.channel != activeChannel.id =>
        logger.debug(s"${context.get()}: skip ${message.channel}")

      case message if message.subtype.isDefined =>
        logger.debug(s"${context.get()}: skip ${message.subtype.get}")

      case message if message.user == connected.self.id =>
        logger.debug(s"${context.get()}: skip myself")

      case message =>
        val req = Request().update(_.dialogue.text.text := message.text, _.dialogue.context := context.get())
        logger.info(s"${context.get()}: $req")

        val res = stub.talk(req)
        logger.info(s"${context.get()}: $res")

        val last = context.getAndSet(res.getDialogue.context)
        if (context.get() != last) {
          logger.info(s"context changed: $last > ${context.get()}")
        }

        val self = FirebaseMessage.self(req.getDialogue.getText.text)
        val kiritan = FirebaseMessage.kiritan(res.getDialogue.getText.text)

        storage.upload(s"${kiritan.uuid}.wav", res.getVoice.voice.toByteArray)

        (self :: kiritan :: Nil).foreach { m =>
          Await.result(firebase.write(m), 10.seconds)
        }
    }
  }

  val received: PartialFunction[String, Unit] = {
    case str =>
      try {
        val input = Json.parse(str)

        input.\("type").as[String] match {
          case "message" =>
            logger.debug(str.take(100))
            message(input)

          case "pong" =>
            logger.debug(str.take(100))
            ack.set(input.as[RtmPong].reply_to)

          case "presence_change" | "user_typing" =>
            // nop

          case _ =>
            logger.debug(str.take(100))
        }
      } catch {
        case t: Throwable => logger.error("ws received failed.", t)
      }
  }
}
