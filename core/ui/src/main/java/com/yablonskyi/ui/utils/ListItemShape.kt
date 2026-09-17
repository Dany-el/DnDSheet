package com.yablonskyi.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Rounded outer corners for the first and last items in a vertical list. */
fun listItemShape(
    index: Int,
    itemCount: Int,
    outerCornerSize: Dp = 16.dp,
    innerCornerSize: Dp = 4.dp,
): RoundedCornerShape {
    val topCorners = if (index == 0) outerCornerSize else innerCornerSize
    val bottomCorners = if (index == itemCount - 1) outerCornerSize else innerCornerSize

    return RoundedCornerShape(
        topStart = topCorners,
        topEnd = topCorners,
        bottomStart = bottomCorners,
        bottomEnd = bottomCorners,
    )
}
