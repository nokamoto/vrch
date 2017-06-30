package vrch.chgrpc.docomo

import play.api.libs.json.Json

case class DocomoDialogueResponse(utt: String,
                                  yomi: String,
                                  mode: String,
                                  da: String,
                                  context: String)

object DocomoDialogueResponse {
  implicit val format = Json.format[DocomoDialogueResponse]
}
