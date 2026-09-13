package com.yablonskyi.character.platform.print

import android.content.Context
import android.widget.Toast
import com.yablonskyi.ui.R
import kotlinx.coroutines.CancellationException

internal suspend fun handleCharacterPrintEffect(
    effect: CharacterPrintEffect,
    context: Context,
    claim: (Long) -> Boolean,
    print: suspend (String, String) -> Result<Unit>,
    complete: (Long, Result<Unit>) -> Unit,
    cancel: (Long) -> Unit,
) {
    when (effect) {
        is CharacterPrintEffect.LaunchPrint -> if (claim(effect.requestId)) {
            try { complete(effect.requestId, print(effect.html, effect.jobName)) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { complete(effect.requestId, Result.failure(error)) }
            finally { cancel(effect.requestId) }
        }
        is CharacterPrintEffect.Failed -> Toast.makeText(context, when (effect.error) {
            CharacterPrintError.LOAD_FAILED -> R.string.print_load_failed
            CharacterPrintError.RENDER_FAILED -> R.string.print_render_failed
            CharacterPrintError.PRINT_LAUNCH_FAILED -> R.string.print_launch_failed
        }, Toast.LENGTH_LONG).show()
        CharacterPrintEffect.PrintRequestAccepted -> Toast.makeText(context, R.string.print_request_accepted, Toast.LENGTH_SHORT).show()
    }
}
