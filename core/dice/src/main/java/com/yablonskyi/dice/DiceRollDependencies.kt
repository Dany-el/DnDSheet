package com.yablonskyi.dice

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

fun interface DiceRollClock { fun currentTimeMillis(): Long }

fun interface DiceRollLabelResolver { fun resolve(label: DiceRollLabel?): String }

class SystemDiceRollClock @Inject constructor() : DiceRollClock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}

class AndroidDiceRollLabelResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DiceRollLabelResolver {
    override fun resolve(label: DiceRollLabel?): String = when (label) {
        is DiceRollLabel.TypeStringRes -> {
            val name = context.getString(label.name)
            val displayName = if (label.abbreviateName) name.take(3) else name
            "${displayName.uppercase(Locale.getDefault())}: " +
                context.getString(label.type).uppercase(Locale.getDefault())
        }
        is DiceRollLabel.TypeString -> buildString {
            if (label.name.isNotBlank()) {
                append(label.name.uppercase(Locale.getDefault()))
                append(": ")
            }
            append(context.getString(label.type).uppercase(Locale.getDefault()))
        }
        null -> context.getString(R.string.roll_type_roll).uppercase(Locale.getDefault())
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DiceRollDependenciesModule {
    @Binds @Singleton
    abstract fun bindDiceRollClock(impl: SystemDiceRollClock): DiceRollClock

    @Binds @Singleton
    abstract fun bindDiceRollLabelResolver(impl: AndroidDiceRollLabelResolver): DiceRollLabelResolver
}
