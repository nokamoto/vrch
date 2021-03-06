package vrch.slackbridge.slack.value

import play.api.libs.json.{Json, OFormat}
import vrch.slackbridge.slack.value.RtmConnected.Self

case class RtmConnected(url: String, self: Self)

object RtmConnected {
  case class Self(id: String)

  object Self {
    implicit val format: OFormat[Self] = Json.format
  }

  implicit val format: OFormat[RtmConnected] = Json.format
}
