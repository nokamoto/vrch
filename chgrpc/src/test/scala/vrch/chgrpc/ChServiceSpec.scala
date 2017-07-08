package vrch.chgrpc

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import io.grpc.netty.NettyChannelBuilder
import io.grpc.{ManagedChannel, ServerServiceDefinition}
import org.http4s.{Method, Request}
import org.http4s.server.blaze.BlazeBuilder
import org.scalatest.FlatSpec
import play.api.libs.json.{JsSuccess, Json}
import vrch.chgrpc.ChServiceSpec.{Props, withServer}
import vrch.docomo.{DocomoDialogueRequest, DocomoDialogueResponse}
import vrch.grpc.{MixinExecutionContext, ServerConfig, ServerMain}
import vrch.mockdocomo.DocomoService
import vrch.util.AvailablePort
import vrch.{ChServiceGrpc, Dialogue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ChServiceSpec extends FlatSpec {
  it should "talk to dialogue API" in {
    val context = UUID.randomUUID().toString

    val res1 = DocomoDialogueResponse(utt = "今日も一日がんばるぞい", yomi = "きょうもいちにちがんばるぞい", context = context)

    val res2 = DocomoDialogueResponse(utt = "また明日", yomi = "またあした", context = context)

    val props = Props(apiKey = "test-api-key", responses = res1 :: res2 :: Nil)

    withServer(props) { (channel, actual) =>
      val stub = ChServiceGrpc.blockingStub(channel)

      val req1 = Dialogue().update(_.text.text := "おはようございます")
      assert(stub.talk(req1) === Dialogue().update(_.text.text := res1.utt, _.context := context))

      val req2 = Dialogue().update(_.text.text := "さようなら", _.context := context)
      assert(stub.talk(req2) === Dialogue().update(_.text.text := res2.utt, _.context := context))

      actual.get() match {
        case xs @ actual1 :: actual2 :: Nil =>
          xs.foreach { case (x, _) =>
            assert(x.method === Method.POST)

            assert(x.uri.path.contentEquals("/dialogue/v1/dialogue"))
            assert(x.uri.query.contains(("APIKEY", Some(props.apiKey))))

            assert(x.contentType.exists(_.mediaType.mainType === "application"))
            assert(x.contentType.exists(_.mediaType.subType === "json"))
          }

          def req(actual: (Request, String)): DocomoDialogueRequest = {
            try {
              Json.parse(actual._2).validate[DocomoDialogueRequest].get
            } catch {
              case e: Throwable => fail(s"unexpected json request: $actual", e)
            }
          }

          assert(req(actual1) === DocomoDialogueRequest(utt = req1.getText.text))
          assert(req(actual2) === DocomoDialogueRequest(utt = req2.getText.text, context = Some(context)))

        case xs => fail(s"unexpected requests: $xs")
      }
    }
  }
}

object ChServiceSpec extends AvailablePort {
  case class Props(apiKey: String, responses: Seq[DocomoDialogueResponse])

  def withServer(props: Props)(f: (ManagedChannel, AtomicReference[List[(Request, String)]]) => Unit): Unit = {
    val p1 = availablePort()
    val p2 = availablePort()

    val (docomoService, expected) = DocomoService.service(props.responses)

    val grpc = new ServerMain with MixinChService with MixinExecutionContext {
      override protected[this] def services: Seq[(ExecutionContext) => ServerServiceDefinition] = {
        Seq(ChServiceGrpc.bindService(chService, _))
      }

      override def chConfig: ChConfig = ChConfig(apiKey = props.apiKey, url = s"http://localhost:$p1")

      override def serverConfig: ServerConfig = ServerConfig(port = p2, concurrency = 1, shutdownTimeout = 10.seconds)
    }

    val channel = NettyChannelBuilder.forAddress("localhost", p2).usePlaintext(true).build()

    val docomo = BlazeBuilder.bindHttp(p1, "localhost").mountService(docomoService, "/").run

    try {
      grpc.start()
      f(channel, expected)
    } finally {
      channel.shutdown()
      docomo.shutdown
      grpc.shutdown()
    }
  }
}
