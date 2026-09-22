package com.yablonskyi.data.provider

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.pm.PackageInfoCompat
import com.yablonskyi.domain.provider.AppVersionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidAppVersionProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AppVersionProvider {
    override val versionName: String
        get() = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }

    override val versionCode: Long
        @RequiresApi(Build.VERSION_CODES.P)
        get() = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (_: Exception) {
            1L
        }
}