package com.yablonskyi.ui.utils

sealed interface FileOperationEffect {
    data class ExportReady(
        val fileName: String,
        val json: String
    ) : FileOperationEffect

    data class ShareReady(
        val fileName: String,
        val json: String
    ) : FileOperationEffect

    data object OpenImportPicker : FileOperationEffect
}
