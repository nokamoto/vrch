package vrch.slackbridge.slack

import java.net.URL

import play.api.libs.json._
import vrch.Logger

import scalaj.http.{Http, HttpResponse, MultiPart}

case class SlackApi(url: String, token: String) extends Logger {
  private[this] def as[A](path: String, res: HttpResponse[String])(implicit reads: Reads[A]): A = {
    logger.debug(s"$path - $res")

    reads.reads(Json.parse(res.body)) match {
      case JsSuccess(value, _) => value
      case JsError(errors) => throw JsResultException(errors)
    }
  }

  private[this] def full(path: String): String = new URL(new URL(url), path).toString

  def post[A](path: String, params: (String, String)*)(implicit reads: Reads[A]): A = {
    val res = Http(full(path)).postForm(("token", token) :: params.toList).asString
    as[A](path, res)
  }

  def postFile[A](path: String, multipart: MultiPart, params: (String, String)*)(implicit reads: Reads[A]): A = {
    val res = Http(full(path)).params(("token", token) :: params.toList).postMulti(multipart).asString
    as[A](path, res)
  }
}
