package com.wlangiewicz.bitmarket

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.language.postfixOps
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.duration._
import collection.immutable.Seq
import com.roundeights.hasher.Implicits._
import spray.json._
import scala.concurrent.ExecutionContext.Implicits.global

class Client(publicApiKey: String, privateApiKey: String)(implicit system: ActorSystem, materializer: ActorMaterializer) {
  val HeaderApiKey = "API-Key"
  val HeaderSignature = "API-Hash"

  val BaseUrl = "https://www.bitmarket.pl/api2/"

  val uri = Uri().withQuery(Uri.Query.apply(Map("method" -> "info", "tonce" -> (System.currentTimeMillis / 1000).toString)))

  val body = uri.rawQueryString.getOrElse("")
  println(body)

  val sign = body.hmac(privateApiKey).sha512.hex

  val request = HttpRequest(
    uri = BaseUrl, method = HttpMethods.POST,
    headers = Seq(RawHeader(HeaderApiKey, publicApiKey), RawHeader(HeaderSignature, sign)),
    entity = HttpEntity(body)
  )

  println(s"request: $request")

  val response = Await.result(Http().singleRequest(request), 5 seconds)

  val result = Await.result(Unmarshal(response).to[String].map(_.parseJson), 10 seconds)

  println(result)
}

