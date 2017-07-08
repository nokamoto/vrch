package vrch.chgrpc

import java.net.URL

import play.api.libs.json._
import vrch.ChServiceGrpc.ChService
import vrch.Dialogue
import vrch.docomo.{DocomoDialogueRequest, DocomoDialogueResponse}

import scala.concurrent.Future
import scalaj.http.Http

trait ChServiceImpl extends ChService with UseChConfig {
  private[this] lazy val apiKey = chConfig.apiKey

  private[this] lazy val url = new URL(new URL(chConfig.url), "/dialogue/v1/dialogue").toString

  private[this] val headers = Seq("Content-Type" -> "application/json")

  override def talk(request: Dialogue): Future[Dialogue] = {
    val body = Json.toJson(
      DocomoDialogueRequest(utt = request.getText.text, context = Some(request.context).find(_ != ""))
    )

    println(body)

    val req = Http(url).param("APIKEY", apiKey).headers(headers).postData(body.toString())
    val res = req.asString

    println(s"${req.toString().take(100)} - ${res.toString.take(100)}")

    Json.parse(res.body).validate[DocomoDialogueResponse].fold(
      invalid => Future.failed(JsResultException(invalid)),
      r => Future.successful(Dialogue().update(_.text.text := r.utt, _.context := r.context))
    )
  }
}

trait UseChService {
  def chService: ChService
}

trait MixinChService extends UseChService with UseChConfig { self =>
  override def chService: ChService = new ChServiceImpl {
    override def chConfig: ChConfig = self.chConfig
  }
}
