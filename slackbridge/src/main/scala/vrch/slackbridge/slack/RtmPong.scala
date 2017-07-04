package vrch.slackbridge.slack

import play.api.libs.json.{Json, OFormat}

case class RtmPong(reply_to: Long)

object RtmPong {
  implicit val format: OFormat[RtmPong] = Json.format
}
