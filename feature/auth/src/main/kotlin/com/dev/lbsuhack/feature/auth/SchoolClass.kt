package com.dev.lbsuhack.feature.auth

/**
 * Приводит произвольную запись класса к формату «номер + буква»:
 * `11 "М"` -> `11М`, `10"В"(2025)` -> `10В`, `10-а` -> `10А`, `10 класс` -> `10`.
 * Возвращает null, если номер класса найти не удалось.
 */
internal fun parseSchoolClass(raw: String): String? {
    CLASS_WITH_LETTER.findAll(raw)
        .firstOrNull { it.groupValues[1].isGradeNumber() }
        ?.let { return it.groupValues[1] + it.groupValues[2].uppercase() }

    return CLASS_NUMBER.findAll(raw)
        .firstOrNull { it.value.isGradeNumber() }
        ?.value
}

private fun String.isGradeNumber(): Boolean = (toIntOrNull() ?: return false) in 1..12

private val CLASS_WITH_LETTER =
    Regex("""(?<!\d)(\d{1,2})(?!\d)[\s\-–—"«»'`]*([A-Za-zА-Яа-яЁё])(?![A-Za-zА-Яа-яЁё])""")

private val CLASS_NUMBER = Regex("""(?<!\d)\d{1,2}(?!\d)""")
