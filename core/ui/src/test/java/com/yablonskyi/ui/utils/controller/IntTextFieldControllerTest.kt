package com.yablonskyi.ui.utils.controller

import org.junit.Assert.*
import org.junit.Test

class IntTextFieldControllerTest {
    @Test fun givenSignedField_whenTypingIncompleteOrOverflow_thenDoesNotEmitFabricatedValue() {
        val controller = IntTextFieldController(5, allowSigned = true)
        assertNull(controller.onTextChange("-"))
        assertEquals(-12, controller.onTextChange("-12"))
        assertNull(controller.onTextChange("999999999999999"))
        assertEquals("-12", controller.text)
    }
    @Test fun givenUnsignedField_whenTypingNegative_thenRejectsIt() {
        val controller = IntTextFieldController(5)
        assertNull(controller.onTextChange("-1"))
        assertEquals("5", controller.text)
    }
    @Test fun givenFocusedField_whenModelChanges_thenBlurReconcilesText() {
        val controller = IntTextFieldController(5)
        controller.onFocusChange(true, 5)
        controller.onTextChange("8")
        controller.syncIfUnfocused(10)
        controller.onFocusChange(false, 10)
        assertEquals("10", controller.text)
    }
}
