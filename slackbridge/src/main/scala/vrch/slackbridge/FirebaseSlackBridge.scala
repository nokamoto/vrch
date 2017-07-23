package vrch.slackbridge

import java.util.concurrent.atomic.AtomicReference

import play.api.libs.json.JsValue
import vrch.slackbridge.firebase.Platform.{Android, Slack}
import vrch.slackbridge.firebase.WhoAmI.{Kiritan, Self}
import vrch.slackbridge.firebase.{FirebaseConfig, FirebaseMessage, FirebaseMessageClient, FirebaseStorageClient}
import vrch.{Request, Response}

import scala.concurrent.Await
import scala.concurrent.duration._

class FirebaseSlackBridge(config: SlackBridgeConfig, firebaseConfig: FirebaseConfig) extends SlackBridge(config) {
  private[this] val firebase = new FirebaseMessageClient(config = firebaseConfig, room = "lobby")

  protected[this] override val context = new AtomicReference[String](Await.result(firebase.context(), 10.seconds).context)

  private[this] val storage = new FirebaseStorageClient(firebaseConfig)

  firebase.addListener { message =>
    logger.info(s"receive $message")

    message.who match {
      case Self =>
        message.platform match {
          case Android =>
            val res = slackApi.post[JsValue](
              "/api/chat.postMessage",
              ("channel", listenedChannel.id),
              ("text", message.message),
              ("username", s"${message.displayName}@Android")
            )

            logger.info(s"/api/chat.postMessage - $res")

          case Slack => // nop
        }

      case Kiritan =>
        val bytes: Array[Byte] = storage.download(s"${message.uuid}.wav")

        upload(s"${message.message}.wav", bytes)
    }
  }

  override protected[this] def call(req: Request, res: Response): Unit = {
    val self = FirebaseMessage.self(req.getDialogue.getText.text)
    val kiritan = FirebaseMessage.kiritan(res.getDialogue.getText.text)

    storage.upload(s"${kiritan.uuid}.wav", res.getVoice.voice.toByteArray)

    (self :: kiritan :: Nil).foreach { m =>
      Await.result(firebase.write(m), 10.seconds)
    }
  }
}
