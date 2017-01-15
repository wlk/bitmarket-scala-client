package com.wlangiewicz.bitmarket

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  // It's annoying but ordering of the vals matters
  implicit val BalanceFormat: RootJsonFormat[Balance] = jsonFormat3(Balance)

  implicit val BalancesFormat: RootJsonFormat[Balances] = jsonFormat2(Balances)

  implicit val InfoFormat: RootJsonFormat[Info] = jsonFormat1(Info)

  implicit val ResponseErrorFormat: RootJsonFormat[ResponseError] = jsonFormat2(ResponseError)

  implicit val LimitsFormat: RootJsonFormat[Limits] = jsonFormat3(Limits)

  implicit val SwapContractFormat: RootJsonFormat[SwapContract] = jsonFormat4(SwapContract)

  implicit val SwapOpenedFormat: RootJsonFormat[SwapOpened] = jsonFormat2(SwapOpened)

  implicit val SwapClosedFormat: RootJsonFormat[SwapClosed] = jsonFormat1(SwapClosed)

  implicit val SwapOrderReader = new JsonFormat[SwapOrder] {
    override def read(json: JsValue): SwapOrder = json match {
      case JsArray(values) => SwapOrder(values.head.convertTo[BigDecimal], values(1).convertTo[BigDecimal])
      case other           => throw new RuntimeException("failed to deserialize SwapOrder " + other)
    }

    override def write(obj: SwapOrder): JsValue = {
      JsArray(JsNumber(obj.amount), JsNumber(obj.percent))
    }
  }

  implicit val SwapsResponseFormat: RootJsonFormat[SwapsResponse] = jsonFormat3(SwapsResponse)

  implicit def ResponseSuccessFormat[T: JsonFormat]: RootJsonFormat[ResponseSuccess[T]] = jsonFormat3(ResponseSuccess.apply[T])
}
