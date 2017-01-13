package com.wlangiewicz.bitmarket

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import collection.immutable.Seq
import com.roundeights.hasher.Implicits._
import spray.json._

class Client(publicApiKey: String, privateApiKey: String)(implicit system: ActorSystem, materializer: ActorMaterializer) {
  val HeaderApiKey = "API-Key"
  val HeaderSignature = "API-Hash"
  val BaseUrl = "https://www.bitmarket.pl/api2/"

  private def requestBody(method: String): String = {
    Uri()
      .withQuery(
        Uri.Query.apply(
          Map(
            "method" -> method,
            "tonce" -> (System.currentTimeMillis / 1000).toString
          )
        )
      )
      .rawQueryString
      .getOrElse("")
  }

  private def httpRequest(signedBody: String, body: String) = {
    HttpRequest(
      uri = BaseUrl, method = HttpMethods.POST,
      headers = Seq(RawHeader(HeaderApiKey, publicApiKey), RawHeader(HeaderSignature, signedBody)),
      entity = HttpEntity(body)
    )
  }

  /**
   * Generates a HttpRequest that can be later performed to get Info response
   * @see https://github.com/bitmarket-net/api#info---account-information
   * @return HttpRequest performing Info request
   */
  def infoRequest: HttpRequest = {
    val body = requestBody(method = "info")
    val signedBody = body.hmac(privateApiKey).sha512.hex

    httpRequest(signedBody, body)
  }

  /**
   * Performs a single HttpRequest
   * @param request
   * @return Future[HttpResponse]
   */
  def performRequest(request: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(request)
  }

  /**
   * Parses HttpResponse into objects
   * @param httpResponse A HttpResponse that contains response from the Bitmarket API
   * @param executionContext Required by underlying API
   * @return Unmarshaled response as JsValue
   */
  def unmarshalResponse(httpResponse: HttpResponse)(implicit executionContext: ExecutionContext): Future[JsValue] = {
    Unmarshal(httpResponse).to[String].map(_.parseJson)
  }
}

