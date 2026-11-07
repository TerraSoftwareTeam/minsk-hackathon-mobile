package com.dev.lbsuhack.feature.auth

internal const val SCHOOL_CLASS_LETTERS = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ"

internal fun parseSchoolClass(raw: String): String? =
    CLASS_WITH_LETTER.findAll(raw)
        .filter { it.groupValues[1].isGradeNumber() }
        .firstNotNullOfOrNull { match ->
            match.groupValues[2].toClassLetter()?.let { letter -> match.groupValues[1] + letter }
        }

internal fun normalizePupilClass(raw: String): String? {
    parseSchoolClass(raw)?.let { return it }
    val grade = CLASS_NUMBER.findAll(raw).firstOrNull { it.value.isGradeNumber() } ?: return null
    val letter = WORD.findAll(raw, grade.range.last + 1)
        .map { it.value }
        .filterNot { it.lowercase() in IGNORED_WORDS }
        .firstOrNull()
        ?.take(1)
        ?.toClassLetter()
        ?: stableLetterFor(raw)
    return grade.value + letter
}

private fun String.toClassLetter(): String? {
    val upper = uppercase()
    val cyrillic = LATIN_TO_CYRILLIC[upper] ?: upper
    return cyrillic.takeIf { it.length == 1 && it in SCHOOL_CLASS_LETTERS }
}

private fun stableLetterFor(raw: String): String {
    val key = raw.trim().lowercase()
    val index = Math.floorMod(key.hashCode(), SCHOOL_CLASS_LETTERS.length)
    return SCHOOL_CLASS_LETTERS[index].toString()
}

private fun String.isGradeNumber(): Boolean = (toIntOrNull() ?: return false) in 1..12

private val LATIN_TO_CYRILLIC = mapOf(
    "A" to "А",
    "B" to "В",
    "C" to "С",
    "E" to "Е",
    "H" to "Н",
    "K" to "К",
    "M" to "М",
    "O" to "О",
    "P" to "Р",
    "T" to "Т",
    "X" to "Х",
)

private val IGNORED_WORDS = setOf("класс", "класса", "кл", "class", "год", "г")

private val CLASS_WITH_LETTER =
    Regex("""(?<!\d)(\d{1,2})(?!\d)[\s\-–—"«»'`]*([A-Za-zА-Яа-яЁё])(?![A-Za-zА-Яа-яЁё])""")

private val CLASS_NUMBER = Regex("""(?<!\d)\d{1,2}(?!\d)""")

private val WORD = Regex("""[A-Za-zА-Яа-яЁё]+""")
