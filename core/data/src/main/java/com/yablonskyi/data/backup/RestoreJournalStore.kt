package com.yablonskyi.data.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

@Serializable
internal data class RestoreJournal(
    val version: Int = 1,
    val operationId: String,
    val images: List<RestoreImage>,
    val oldImageNames: List<String>,
    val phase: RestorePhase,
)

@Serializable
internal data class RestoreImage(val assetId: String, val fileName: String, val sizeBytes: Long, val sha256: String)

@Serializable
internal enum class RestorePhase { PREPARED, APPLIED }

/** Private app files only. All operations run under the shared access gate on an IO dispatcher. */
internal class RestoreJournalStore(private val filesDirectory: File) {
    private val journalFile = File(filesDirectory, "restore-journal.json")
    private val pendingFile = File(filesDirectory, "restore-journal.pending")
    private val json = Json { encodeDefaults = true }
    private val managedName = Regex("char_img_[A-Za-z0-9_-]+\\.(jpg|jpeg|png|webp|bin)")

    fun read(): RestoreJournal? {
        // A pending write was never acknowledged and cannot supersede the last committed journal.
        Files.deleteIfExists(pendingFile.toPath())
        if (!journalFile.exists()) return null
        val text = journalFile.inputStream().use { input ->
            if (input.channel.size() > MAX_JOURNAL_BYTES) throw IOException("Restore journal is too large")
            input.readBytes().toString(Charsets.UTF_8)
        }
        return json.decodeFromString<RestoreJournal>(text).also(::validate)
    }

    fun write(journal: RestoreJournal) {
        validate(journal)
        val bytes = json.encodeToString(journal).toByteArray(Charsets.UTF_8)
        if (bytes.size > MAX_JOURNAL_BYTES) throw IOException("Restore journal is too large")
        FileOutputStream(pendingFile).use { output ->
            output.write(bytes)
            output.fd.sync()
        }
        // Unlike AtomicFile.finishWrite, failures must propagate instead of only being logged.
        Files.move(pendingFile.toPath(), journalFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING)
    }

    fun clear() {
        Files.deleteIfExists(pendingFile.toPath())
        Files.deleteIfExists(journalFile.toPath())
    }

    fun newJournal(staged: StagedBackup, oldPaths: List<String>): RestoreJournal {
        val operationId = UUID.randomUUID().toString()
        return RestoreJournal(
            operationId = operationId,
            images = staged.manifest.assets.map {
                RestoreImage(it.id, "char_img_restore_${operationId}_${it.id}.bin", it.sizeBytes, it.sha256)
            },
            oldImageNames = oldPaths.mapNotNull { path ->
                File(path).canonicalFile.takeIf {
                    it.parentFile == filesDirectory.canonicalFile && managedName.matches(it.name)
                }?.name
            }.distinct(),
            phase = RestorePhase.PREPARED,
        )
    }

    fun imagePaths(journal: RestoreJournal): Map<String, String> = journal.images.associate {
        it.assetId to resolve(it.fileName).absolutePath
    }

    fun installImages(journal: RestoreJournal, staged: StagedBackup, checkCanceled: () -> Unit) {
        journal.images.forEach { image ->
            checkCanceled()
            val destination = resolve(image.fileName)
            if (!destination.createNewFile()) throw IOException("Restore image already exists")
            FileOutputStream(destination).use { output ->
                staged.images.getValue(image.assetId).inputStream().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var size = 0L
                    while (true) {
                        checkCanceled()
                        val count = input.read(buffer)
                        if (count < 0) break
                        size += count
                        if (size > image.sizeBytes) throw IOException("Staged image changed")
                        output.write(buffer, 0, count)
                    }
                }
                output.fd.sync()
            }
            verifyImage(image)
        }
    }

    fun verifyImages(journal: RestoreJournal) = journal.images.forEach(::verifyImage)

    fun discardNewImages(journal: RestoreJournal) = journal.images.forEach { delete(it.fileName) }

    fun discardOldImages(journal: RestoreJournal) {
        val retained = journal.images.map { it.fileName }.toSet()
        journal.oldImageNames.filterNot { it in retained }.forEach(::delete)
    }

    private fun verifyImage(image: RestoreImage) {
        val file = resolve(image.fileName)
        if (!file.isFile || file.length() != image.sizeBytes) throw IOException("Restored image missing or incomplete")
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        if (hash != image.sha256) throw IOException("Restored image checksum mismatch")
    }

    private fun delete(name: String) {
        val file = resolve(name)
        if (file.exists() && !file.delete()) throw IOException("Cannot clean up restore image")
    }

    private fun resolve(name: String): File {
        require(managedName.matches(name)) { "Unsafe restore image name" }
        return File(filesDirectory, name).canonicalFile.also {
            require(it.parentFile == filesDirectory.canonicalFile) { "Restore image escapes private files" }
        }
    }

    private fun validate(journal: RestoreJournal) {
        require(journal.version == 1) { "Unsupported restore journal" }
        require(UUID.fromString(journal.operationId).toString() == journal.operationId)
        require(journal.images.map { it.assetId }.distinct().size == journal.images.size)
        journal.images.forEach {
            require(Regex("[A-Za-z0-9_-]{1,100}").matches(it.assetId))
            require(it.fileName == "char_img_restore_${journal.operationId}_${it.assetId}.bin")
            require(it.sizeBytes > 0 && Regex("[0-9a-f]{64}").matches(it.sha256))
            resolve(it.fileName)
        }
        journal.oldImageNames.forEach(::resolve)
    }

    private companion object { const val MAX_JOURNAL_BYTES = 4 * 1024 * 1024 }
}
