package vrch.slackbridge

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import play.api.libs.json.{JsValue, Json}
import vrch.Logger
import vrch.slackbridge.slack.value.{RtmConnected, RtmMessage, RtmPong, SlackChannel}

class WsReceived(listenedChannel: SlackChannel,
                 context: AtomicReference[String],
                 ack: AtomicLong,
                 connected: RtmConnected,
                 onMessage: RtmMessage => Unit) extends Logger {

  private[this] def message(input: JsValue): Unit = synchronized {
    input.as[RtmMessage] match {
      case message if message.channel != listenedChannel.id =>
        logger.debug(s"${context.get()}: skip ${message.channel}")

      case message if message.subtype.isDefined =>
        logger.debug(s"${context.get()}: skip ${message.subtype.get}")

      case message if message.user == connected.self.id =>
        logger.debug(s"${context.get()}: skip myself")

      case message =>
        onMessage(message)
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
