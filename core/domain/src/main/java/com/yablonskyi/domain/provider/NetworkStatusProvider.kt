package com.yablonskyi.domain.provider

import com.yablonskyi.model.language.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkStatusProvider {
    val status: Flow<NetworkStatus>
}
