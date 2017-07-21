package vrch.slackbridge.firebase

sealed abstract class Platform(val value: Int) extends EnumValue

object Platform {
  case object Android extends Platform(0)
  case object Slack extends Platform(1)

  val values: Set[Platform] = Set(Android, Slack)
}
