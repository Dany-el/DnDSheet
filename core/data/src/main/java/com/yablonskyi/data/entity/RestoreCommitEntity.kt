package com.yablonskyi.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Device-local recovery metadata, deliberately excluded from portable backups. */
@Entity(tableName = "restore_commit")
data class RestoreCommitEntity(
    @PrimaryKey val id: Int = 1,
    val operationId: String,
)
