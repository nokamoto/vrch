package vrch.slackbridge.firebase

sealed abstract class WhoAmI(val value: Int) extends EnumValue

object WhoAmI {
  case object Self extends WhoAmI(0)
  case object Kiritan extends WhoAmI(1)

  val values: Set[WhoAmI] = Set(Self, Kiritan)
}
