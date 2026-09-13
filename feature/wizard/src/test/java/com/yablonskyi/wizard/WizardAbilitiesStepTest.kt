package com.yablonskyi.wizard

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Config

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "en-w320dp-h900dp")
class WizardAbilitiesStepTest : WizardAbilitiesStepTestCases()
