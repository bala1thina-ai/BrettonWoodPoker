package com.brettonwood.poker.data

data class PlayerWithTotal(
    val playerId: Long,
    val name: String,
    val totalAmount: Double,
    val buyInCount: Int,
    val rebuyCount: Int,
    val rebuyTotal: Double = 0.0,
    val cashoutAmount: Double = 0.0
) {
    val hasCashedOut: Boolean get() = cashoutAmount > 0.0
    val net: Double get() = cashoutAmount - totalAmount
}
