package vrch.slackbridge.slack

import play.api.libs.json.{Json, OFormat}

case class RtmMessage(user: String, channel: String, text: String)

object RtmMessage {
  implicit val format: OFormat[RtmMessage] = Json.format
}
