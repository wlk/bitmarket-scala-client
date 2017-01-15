package com.wlangiewicz.bitmarket

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {
  // It's annoying but ordering of the vals matters
  implicit val BalanceFormat = jsonFormat3(Balance)

  implicit val BalancesFormat = jsonFormat2(Balances)

  implicit val InfoFormat = jsonFormat1(Info)

  implicit val ResponseErrorFormat = jsonFormat2(ResponseError)

  implicit val LimitsFormat = jsonFormat3(Limits)

  implicit val SwapContractFormat = jsonFormat4(SwapContract)

  implicit def ResponseSuccessFormat[T: JsonFormat] = jsonFormat3(ResponseSuccess.apply[T])
}
