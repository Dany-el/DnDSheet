package com.yablonskyi.data.language

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.yablonskyi.domain.provider.NetworkStatusProvider
import com.yablonskyi.model.language.NetworkStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNetworkStatusProvider @Inject constructor(
    @ApplicationContext context: Context,
) : NetworkStatusProvider {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    override val status: Flow<NetworkStatus> = callbackFlow {
        fun currentStatus(): NetworkStatus {
            val network = connectivityManager.activeNetwork ?: return NetworkStatus.OFFLINE
            val capabilities = connectivityManager.getNetworkCapabilities(network)
                ?: return NetworkStatus.OFFLINE
            return if (
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ) {
                NetworkStatus.ONLINE
            } else {
                NetworkStatus.OFFLINE
            }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentStatus())
            }

            override fun onLost(network: Network) {
                trySend(currentStatus())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(currentStatus())
            }
        }

        trySend(currentStatus())
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
