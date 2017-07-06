package vrch.docomo

import play.api.libs.json._

/**
  * @see [[https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_name=dialogue&p_name=api_reference]]
  */
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
  implicit val format: OFormat[DocomoDialogueRequest] = Json.format
}
