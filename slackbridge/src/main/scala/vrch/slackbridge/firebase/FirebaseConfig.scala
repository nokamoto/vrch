package vrch.slackbridge.firebase

import vrch.grpc.ImplicitProperty._

case class FirebaseConfig(json: String, url: String, bucket: String)

object FirebaseConfig {
  lazy val default: FirebaseConfig = {
    FirebaseConfig(
      json = "firebase.adminsdk.json".stringProp,
      url = "firebase.adminsdk.url".stringProp,
      bucket = "firebase.storage.bucket".stringProp
    )
  }
}