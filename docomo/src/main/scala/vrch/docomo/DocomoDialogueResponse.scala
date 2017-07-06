package vrch.docomo

import play.api.libs.json._

/**
  * @see [[https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_name=dialogue&p_name=api_reference]]
  */
case class DocomoDialogueResponse(utt: String,
                                  yomi: String = "",
                                  mode: String = "",
                                  da: String = "",
                                  context: String)

object DocomoDialogueResponse {
  implicit val format: OFormat[DocomoDialogueResponse] = Json.format
}
