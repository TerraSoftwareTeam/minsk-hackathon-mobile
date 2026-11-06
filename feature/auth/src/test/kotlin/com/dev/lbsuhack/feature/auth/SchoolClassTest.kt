package com.dev.lbsuhack.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SchoolClassTest {

    @Test
    fun `normalizes schools by class formats`() {
        assertEquals("10В", parseSchoolClass("10\"В\"(2025)"))
        assertEquals("11М", parseSchoolClass("11 \"М\""))
        assertEquals("10А", parseSchoolClass("10-а"))
        assertEquals("10A", parseSchoolClass("10A"))
        assertEquals("9Б", parseSchoolClass("  9 б  "))
        assertEquals("5В", parseSchoolClass("5 «В» класс"))
    }

    @Test
    fun `keeps number when there is no letter`() {
        assertEquals("10", parseSchoolClass("10 класс"))
        assertEquals("7", parseSchoolClass("7"))
    }

    @Test
    fun `returns null when there is no grade number`() {
        assertNull(parseSchoolClass("десятый класс"))
        assertNull(parseSchoolClass(""))
        assertNull(parseSchoolClass("(2025)"))
    }

    @Test
    fun everyPickerLetterSurvivesParsing() {
        "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ".forEach { letter ->
            assertEquals("11$letter", parseSchoolClass("11$letter"))
        }
        assertEquals("7", parseSchoolClass("7"))
    }
}
