package com.yablonskyi.model.character

import kotlinx.serialization.Serializable

@Serializable
data class Money(
    val gold: Int = 0,
    val silver: Int = 0,
    val copper: Int = 0
)
