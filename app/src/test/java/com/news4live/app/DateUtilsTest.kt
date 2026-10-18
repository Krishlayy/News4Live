package com.news4live.app

import com.news4live.app.util.DateUtils
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testFormatHindiDateNotNull() {
        val sampleDate = "2026-09-08T11:36:37"
        val formatted = DateUtils.formatHindiDate(sampleDate)

        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun testEmptyDateHandling() {
        val formatted = DateUtils.formatHindiDate("")
        assertTrue(formatted.isEmpty())
    }
}
