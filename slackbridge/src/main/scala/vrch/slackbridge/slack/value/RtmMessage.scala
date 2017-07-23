package vrch.slackbridge.slack.value

import play.api.libs.json.{Json, OFormat}

case class RtmMessage(user: String, channel: String, text: String, subtype: Option[String])

object RtmMessage {
  implicit val format: OFormat[RtmMessage] = Json.format
}
