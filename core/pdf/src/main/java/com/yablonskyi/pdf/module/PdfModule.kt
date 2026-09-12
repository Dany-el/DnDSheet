package com.yablonskyi.pdf.module

import com.yablonskyi.domain.CharacterSheetHtmlRenderer
import com.yablonskyi.pdf.html.DefaultCharacterSheetHtmlRenderer
import com.yablonskyi.pdf.html.HtmlTemplateRenderer
import com.yablonskyi.pdf.html.PythonHtmlTemplateRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PdfModule {
    @Binds
    @Singleton
    abstract fun bindsTemplateRenderer(impl: PythonHtmlTemplateRenderer): HtmlTemplateRenderer

    @Binds
    @Singleton
    abstract fun bindsCharacterSheetHtmlRenderer(
        impl: DefaultCharacterSheetHtmlRenderer,
    ): CharacterSheetHtmlRenderer
}
