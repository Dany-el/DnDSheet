package com.yablonskyi.ui.settings

import androidx.annotation.StringRes
import com.yablonskyi.ui.R

enum class ListView(@param:StringRes val label: Int) {
    LIST(R.string.list),
    GRID(R.string.grid),
}