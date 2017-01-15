package com.wlangiewicz

package object bitmarket {
  // API Responses
  case class ResponseSuccess[T](success: Boolean, limit: Limits, data: T)

  case class ResponseError(error: Int, errorMsg: String)

  // TOP LEVEL API OBJECTS
  case class Info(balances: Balances)

  type SwapList = Vector[SwapContract]

  case class SwapOpened(id: Long, balances: Balances)

  case class SwapClosed(balances: Balances)

  case class SwapsResponse(demand: BigDecimal, cutoff: BigDecimal, swaps: Vector[SwapOrder])

  // LOWER LEVEL API OBJECTS
  case class Limits(used: Int, allowed: Int, expires: Int)

  case class Balance(PLN: BigDecimal, BTC: BigDecimal, LTC: BigDecimal)

  case class Balances(available: Balance, blocked: Balance)

  case class SwapContract(id: Long, amount: BigDecimal, rate: BigDecimal, earnings: BigDecimal)

  case class SwapOrder(percent: BigDecimal, amount: BigDecimal)
}
