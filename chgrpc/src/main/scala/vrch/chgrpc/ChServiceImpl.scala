package vrch.chgrpc

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsResultException, JsValue, Json}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import vrch.ChServiceGrpc.ChService
import vrch.Dialogue
import vrch.chgrpc.docomo.{DocomoDialogueRequest, DocomoDialogueResponse}
import vrch.grpc.UseExecutionContext

import scala.concurrent.{ExecutionContext, Future}

trait ChServiceImpl extends ChService with UseChConfig with UseExecutionContext {
  private[this] implicit val system = ActorSystem("ch")
  private[this] implicit val materializer = ActorMaterializer()

  private[this] lazy val apiKey = chConfig.apiKey

  private[this] lazy val url = s"https://api.apigw.smt.docomo.ne.jp/dialogue/v1/dialogue?APIKEY=$apiKey"

  private[this] val client = StandaloneAhcWSClient()

  private[this] val headers = Seq("Content-Type" -> "application/json")

  override def talk(request: Dialogue): Future[Dialogue] = {
    val body = Json.toJson(
      DocomoDialogueRequest(utt = request.getText.text, context = Some(request.context).find(_ != ""))
    )(DocomoDialogueRequest.format)

    println(body)

    client.url(url).addHttpHeaders(headers: _*).post(body).map { res =>
      println(s"$res - ${res.body.take(100)}")
      res.body[JsValue].validate[DocomoDialogueResponse].fold(
        invalid => throw JsResultException(invalid),
        r => Dialogue().update(_.text.text := r.utt, _.context := r.context)
      )
    }
  }
}

trait UseChService {
  def chService: ChService
}

trait MixinChService extends UseChService with UseChConfig with UseExecutionContext { self =>
  override def chService: ChService = new ChServiceImpl {
    override def chConfig: ChConfig = self.chConfig

    override implicit val context: ExecutionContext = self.context
  }
}
