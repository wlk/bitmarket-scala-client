package com.wlangiewicz

package object bitmarket {
  sealed trait ApiObject

  // API Responses
  case class ResponseSuccess[T](success: Boolean, limit: Limits, data: T)

  case class ResponseError(error: Int, errorMsg: String)

  // TOP LEVEL API OBJECTS
  case class Info(balances: Balances) extends ApiObject

  // LOWER LEVEL API OBJECTS
  case class Limits(used: Int, allowed: Int, expires: Int)

  case class Balance(PLN: BigDecimal, BTC: BigDecimal, LTC: BigDecimal)

  case class Balances(available: Balance, blocked: Balance)
}
