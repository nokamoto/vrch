package vrch.chgrpc.docomo

import play.api.libs.json.{Format, Json}

case class DocomoDialogueRequest(utt: String,
                                 context: Option[String] = None,
                                 nickname: Option[String] = None,
                                 nickname_y: Option[String] = None,
                                 sex: Option[String] = None,
                                 bloodtype: Option[String] = None,
                                 birthdateY: Option[String] = None,
                                 birthdateM: Option[String] = None,
                                 birthdateD: Option[String] = None,
                                 age: Option[String] = None,
                                 constellations: Option[String] = None,
                                 place: Option[String] = None)

object DocomoDialogueRequest {
  lazy val format: Format[DocomoDialogueRequest] = Json.format[DocomoDialogueRequest]
}
