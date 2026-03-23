package com.yablonskyi.dndsheet.ui.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GoogleDriveSyncManager(private val context: Context) {

    private val backupFileName = "characters_backup.json"

    /**
     * Builds the Drive service object required to make API calls.
     */
    private fun getDriveService(): Drive {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("User is not signed in to Google")

        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = account.account
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Character Sheet App")
            .build()
    }

    /**
     * Searches the hidden AppData folder for our specific backup file.
     * Returns the File ID if it exists, or null if this is their first sync.
     */
    private fun getExistingBackupFileId(driveService: Drive): String? {
        val result = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$backupFileName'")
            .setFields("files(id, name)")
            .execute()

        return result.files.firstOrNull()?.id
    }

    /**
     * Uploads a JSON string to Google Drive. Overwrites the old file if it exists.
     */
    suspend fun uploadBackup(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            val existingFileId = getExistingBackupFileId(driveService)

            // Convert our string into a format the Google API can upload
            val fileContent = ByteArrayContent.fromString("application/json", jsonString)

            if (existingFileId != null) {
                driveService.files().update(existingFileId, null, fileContent).execute()
            } else {
                val fileMetadata = File().apply {
                    name = backupFileName
                    parents = listOf("appDataFolder")
                }
                driveService.files().create(fileMetadata, fileContent).execute()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Downloads the backup file from Google Drive and returns it as a JSON string.
     */
    suspend fun downloadBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            val existingFileId = getExistingBackupFileId(driveService)
                ?: return@withContext Result.failure(Exception("No backup file found in Drive."))

            val outputStream = ByteArrayOutputStream()
            driveService.files().get(existingFileId).executeMediaAndDownloadTo(outputStream)

            val jsonString = outputStream.toString("UTF-8")
            Result.success(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Deletes the backup file from the hidden AppData folder.
     */
    suspend fun deleteBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()

            val existingFileId = getExistingBackupFileId(driveService)

            if (existingFileId != null) {
                driveService.files().delete(existingFileId).execute()
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Checks if the backup file currently exists in the hidden AppData folder.
     * Returns true if it exists, false if it does not.
     */
    suspend fun doesBackupExist(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()

            val existingFileId = getExistingBackupFileId(driveService)

            Result.success(existingFileId != null)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}