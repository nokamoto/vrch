package vrch.slackbridge.firebase

import com.google.firebase.database.{DataSnapshot, ServerValue}
import vrch.slackbridge.firebase.FirebaseMessage._

import scala.collection.JavaConverters._

case class FirebaseMessage(who: WhoAmI,
                           uid: String,
                           displayName: String,
                           message: String,
                           uuid: String,
                           platform: Platform,
                           createdAt: Long) {

  def toMap: java.util.Map[String, Any] = Map[String, Any](
    WHO -> who.value.toString,
    UID -> uid,
    DISPLAY_NAME -> displayName,
    MESSAGE -> message,
    UUID -> uuid,
    PLATFORM -> platform.value.toString,
    CREATED_AT -> ServerValue.TIMESTAMP
  ).asJava
}


object FirebaseMessage {
  private val WHO = "who"
  private val DISPLAY_NAME = "display_name"
  private val UID = "uid"
  private val MESSAGE = "message"
  private val UUID = "uuid"
  private[firebase] val CREATED_AT = "created_at"
  private val PLATFORM = "platform"

  def apply(snapshot: DataSnapshot): FirebaseMessage = {
    FirebaseMessage(
      who = snapshot.asEnum(WHO, WhoAmI.values),
      uid = snapshot.asString(UID),
      displayName = snapshot.asString(DISPLAY_NAME),
      message = snapshot.asString(MESSAGE),
      uuid = snapshot.asString(UUID),
      platform = snapshot.asEnum(PLATFORM, Platform.values),
      createdAt = snapshot.asLong(CREATED_AT)
    )
  }

  def self(message: String): FirebaseMessage = {
    FirebaseMessage(
      who = WhoAmI.Self,
      uid = "",
      displayName = "anonymous@Slack",
      message = message,
      uuid = java.util.UUID.randomUUID().toString,
      platform = Platform.Slack,
      createdAt = 0
    )
  }

  def kiritan(message: String): FirebaseMessage = {
    FirebaseMessage(
      who = WhoAmI.Kiritan,
      uid = "",
      displayName = "",
      message = message,
      uuid = java.util.UUID.randomUUID().toString,
      platform = Platform.Slack,
      createdAt = 0
    )
  }
}
