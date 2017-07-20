package vrch.slackbridge.firebase

import com.google.firebase.database.DataSnapshot

case class FirebaseRoom(context: String)

object FirebaseRoom {
  private[this] val CONTEXT = "context"

  def apply(snapshot: DataSnapshot): FirebaseRoom = {
    FirebaseRoom(snapshot.asString(CONTEXT))
  }
}
