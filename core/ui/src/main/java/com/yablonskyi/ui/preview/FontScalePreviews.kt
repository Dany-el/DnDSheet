package com.yablonskyi.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "Small (85%)", group = "Font scale", fontScale = 0.85f)
@Preview(name = "Default (100%)", group = "Font scale", fontScale = 1f)
@Preview(name = "Large (130%)", group = "Font scale", fontScale = 1.3f)
@Preview(name = "Extra large (150%)", group = "Font scale", fontScale = 1.5f)
@Preview(name = "Maximum (200%)", group = "Font scale", fontScale = 2f)
annotation class FontScalePreviews
