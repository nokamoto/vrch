package vrch.slackbridge.slack

import play.api.libs.json._

case class SlackChannel(id: String, name: String)

object SlackChannel {
  implicit val format: OFormat[SlackChannel] = Json.format
}
