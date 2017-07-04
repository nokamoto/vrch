package vrch.slackbridge.slack

import play.api.libs.json.{Json, OFormat}

case class SlackChannels(channels: Seq[SlackChannel])

object SlackChannels {
  implicit val format: OFormat[SlackChannels] = Json.format
}
