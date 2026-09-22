package com.yablonskyi.dndsheet

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.yablonskyi.domain.backup.BackupRecoveryManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DnDSheetApplication : Application() {
    @Inject lateinit var backupRecovery: BackupRecoveryManager
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { backupRecovery.recover() }
    }
}
