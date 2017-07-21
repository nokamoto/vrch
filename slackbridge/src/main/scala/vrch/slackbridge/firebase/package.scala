package vrch.slackbridge

import com.google.firebase.database.DataSnapshot

package object firebase {
  implicit class DataSnapshotTo(s: DataSnapshot) {
    def asString(key: String): String = s.child(key).getValue().asInstanceOf[String]

    def asLong(key: String): Long = s.child(key).getValue().asInstanceOf[Long]

    def asEnum[A <: EnumValue](key: String, values: Set[A]): A = {
      values.find(_.value == asString(key).toInt).getOrElse(throw new NoSuchElementException)
    }
  }
}
