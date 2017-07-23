package vrch.slackbridge
import java.util.concurrent.atomic.AtomicReference

import vrch.{Request, Response}

class StandaloneSlackBridge(config: SlackBridgeConfig) extends SlackBridge(config) {
  override protected[this] val context: AtomicReference[String] = new AtomicReference[String]("")

  override protected[this] def call(req: Request, res: Response): Unit = {
    upload(s"${res.getDialogue.getText.text}.wav", res.getVoice.voice.toByteArray)
  }
}