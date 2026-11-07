package com.dev.lbsuhack.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolClassTest {

    @Test
    fun `normalizes schools by class formats`() {
        assertEquals("10В", parseSchoolClass("10\"В\"(2025)"))
        assertEquals("11М", parseSchoolClass("11 \"М\""))
        assertEquals("10А", parseSchoolClass("10-а"))
        assertEquals("10А", parseSchoolClass("10A"))
        assertEquals("9Б", parseSchoolClass("  9 б  "))
        assertEquals("5В", parseSchoolClass("5 «В» класс"))
    }

    @Test
    fun `maps latin lookalike letters to cyrillic so pupils and teachers share a class`() {
        assertEquals(parseSchoolClass("10 «А»"), parseSchoolClass("10a"))
        assertEquals("11В", parseSchoolClass("11B"))
        assertEquals("9С", parseSchoolClass("9c"))
    }

    @Test
    fun `strict parsing requires a class letter`() {
        assertNull(parseSchoolClass("10 класс"))
        assertNull(parseSchoolClass("7"))
        assertNull(parseSchoolClass("10 Хим"))
    }

    @Test
    fun `pupil class always becomes grade plus letter`() {
        val letterPattern = Regex("^\\d{1,2}[$SCHOOL_CLASS_LETTERS]$")
        listOf("10 Хим", "11 физмат", "10 класс", "7", "9-Ё", "10D", "11 \"М\"", "10A").forEach { raw ->
            val normalized = normalizePupilClass(raw)
            assertTrue("$raw -> $normalized", normalized != null && letterPattern.matches(normalized))
        }
    }

    @Test
    fun `pupil class takes the first letter of the class name word`() {
        assertEquals("10Х", normalizePupilClass("10 Хим"))
        assertEquals("11Ф", normalizePupilClass("11 физмат"))
        assertEquals("11М", normalizePupilClass("11 \"М\""))
    }

    @Test
    fun `pupil class without a usable letter gets the same letter for the same input`() {
        assertEquals(normalizePupilClass("10 класс"), normalizePupilClass(" 10 Класс "))
        assertEquals(normalizePupilClass("7"), normalizePupilClass("7"))
        assertNull(normalizePupilClass("десятый класс"))
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
    }
}
