package com.yablonskyi.dndsheet.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.google.gson.Gson
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.ui.utils.GoogleDriveSyncManager
import com.yablonskyi.dndsheet.ui.utils.encodeImageToBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class DriveBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val charRepository: CharacterRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val sheets = charRepository.getAllCharacterSheets()

            val sheetsForExport = sheets.map { sheet ->
                val base64String = encodeImageToBase64(sheet.character.imagePath)
                sheet.copy(
                    character = sheet.character.copy(imagePath = base64String)
                )
            }
            val jsonString = Gson().toJson(sheetsForExport)

            val syncManager = GoogleDriveSyncManager(appContext)
            val result = syncManager.uploadBackup(jsonString)

            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

fun scheduleDailyBackup(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val backupRequest = PeriodicWorkRequestBuilder<DriveBackupWorker>(12, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "HalfDayDriveBackup",
        ExistingPeriodicWorkPolicy.KEEP,
        backupRequest
    )
}