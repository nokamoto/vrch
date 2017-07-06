package vrch.mockdocomo

import java.util.concurrent.atomic.AtomicReference

import org.http4s._
import org.http4s.dsl._
import play.api.libs.json.Json
import vrch.docomo.DocomoDialogueResponse

import scala.collection.mutable

object DocomoService {
  def service(responses: Seq[DocomoDialogueResponse]): (HttpService, AtomicReference[List[Request]]) = {
    val q = mutable.Queue(responses: _*)
    val expected = new AtomicReference(List.empty[Request])

    val svc = HttpService {
      case req @ POST -> Root / "dialogue" / "v1" / "dialogue" =>
        expected.set((req :: expected.get().reverse).reverse)
        Ok(Json.toJson(q.dequeue()).toString()).putHeaders(Header("Content-Type", "application/json"))
    }

    (svc, expected)
  }
}
