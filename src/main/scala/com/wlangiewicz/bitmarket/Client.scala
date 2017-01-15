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

class Client(publicApiKey: String, privateApiKey: String)(implicit system: ActorSystem, materializer: ActorMaterializer) extends JsonFormats {
  private val HeaderApiKey = "API-Key"
  private val HeaderSignature = "API-Hash"
  private val BaseUrl = "https://www.bitmarket.pl/api2/"

  private def requestBody(method: String, parameters: Map[String, String] = Map.empty): String = {
    Uri()
      .withQuery(
        Uri.Query.apply(
          Map(
            "method" -> method,
            "tonce" -> (System.currentTimeMillis / 1000).toString
          ) ++ parameters
        )
      )
      .rawQueryString
      .getOrElse("")
  }

  private def httpRequest(body: String) = {
    val signedBody = body.hmac(privateApiKey).sha512.hex

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
    httpRequest(requestBody(method = "info"))
  }

  /**
   * Generates a HttpRequest that can be later performed to get swapList response
   * @see https://github.com/bitmarket-net/api#swaplist---list-swap-contracts
   * @return HttpRequest performing swapList request
   */
  def swapListRequest: HttpRequest = {
    httpRequest(requestBody(method = "swapList", Map("currency" -> "BTC")))
  }

  /**
   * Generates a HttpRequest that can be later performed to create swap contract using swapOpen request
   * @see https://github.com/bitmarket-net/api#swapopen---open-swap-contract
   * @return HttpRequest performing swapOpen request
   */
  def swapOpenRequest(amount: BigDecimal, rate: BigDecimal): HttpRequest = {
    httpRequest(requestBody(method = "swapOpen", Map("currency" -> "BTC", "amount" -> amount.toString, "rate" -> rate.toString)))
  }

  /**
   * Generates a HttpRequest that can be later performed to close swap contract using swapClose request
   * @see https://github.com/bitmarket-net/api#swapclose---close-swap-contract
   * @return HttpRequest performing swapClose request
   */
  def swapCloseRequest(id: Long): HttpRequest = {
    httpRequest(requestBody(method = "swapClose", Map("currency" -> "BTC", "id" -> id.toString)))
  }

  /**
   * Performs a single HttpRequest
   * @param request HttpRequest to perform
   * @return Future[HttpResponse]
   */
  def performRequest(request: HttpRequest): Future[HttpResponse] = {
    Http().singleRequest(request)
  }

  /**
   * Parses HttpResponse into objects
   * @param httpResponse A HttpResponse that contains response from the Bitmarket API
   * @param executionContext Required by underlying API
   * @param reader Required JSON unmarshaller (reader)
   * @return Unmarshaled response as JsValue
   */
  def unmarshalResponse[T](httpResponse: HttpResponse)(implicit executionContext: ExecutionContext, reader: JsonReader[ResponseSuccess[T]]): Future[ResponseSuccess[T]] = {
    Unmarshal(httpResponse).to[String].map(_.parseJson.convertTo[ResponseSuccess[T]])
  }

  /**
   * This is suggested way to get Info object
   * @param executionContext Required by underlying API
   * @return
   */
  def info(implicit executionContext: ExecutionContext): Future[ResponseSuccess[Info]] = {
    performRequest(infoRequest).flatMap(unmarshalResponse[Info])
  }

  /**
   * Lists my open swap contracts
   * @param executionContext Required by underlying API
   * @return
   */
  def swapList(implicit executionContext: ExecutionContext): Future[ResponseSuccess[SwapList]] = {
    performRequest(swapListRequest).flatMap(unmarshalResponse[SwapList])
  }

  /**
   * Opens single swap contract with given parameters, hardcoded BTC as swap currency
   * @param executionContext Required by underlying API
   * @param amount BigDecimal BTC amount
   * @param rate BigDecimal rate in percents per year
   * @return
   */
  def swapOpen(amount: BigDecimal, rate: BigDecimal)(implicit executionContext: ExecutionContext): Future[ResponseSuccess[SwapOpened]] = {
    performRequest(swapOpenRequest(amount, rate)).flatMap(unmarshalResponse[SwapOpened])
  }

  /**
   * Closes single swap contract by Id, hardcoded BTC as swap currency
   * @param executionContext Required by underlying API
   * @param id Long id of the swap contract to close
   * @return SwapClosed response indicating balances on the account
   */
  def swapClose(id: Long)(implicit executionContext: ExecutionContext): Future[ResponseSuccess[SwapClosed]] = {
    performRequest(swapCloseRequest(id)).flatMap(unmarshalResponse[SwapClosed])
  }
}

