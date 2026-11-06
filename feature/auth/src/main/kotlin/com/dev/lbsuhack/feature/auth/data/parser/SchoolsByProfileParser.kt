package com.dev.lbsuhack.feature.auth.data.parser

import com.dev.lbsuhack.core.domain.model.Pupil
import com.dev.lbsuhack.core.domain.model.Teacher
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole

class SchoolsByProfileParser {

    private val h1Regex = Regex(
        pattern = """<h1[^>]*>(.*?)</h1>""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val schoolRegex = Regex(
        pattern = """<title[^>]*>(.*?)</title>""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val schoolHeaderRegex = Regex(
        pattern = """<span\b[^>]*class=["'][^"']*\bname\b[^"']*["'][^>]*>\s*<a[^>]*>(.*?)</a>\s*</span>""",
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val htmlTagRegex = Regex("""<[^>]+>""")
    private val whitespaceRegex = Regex("""\s+""")
    private val idInTextRegex = Regex("""ID\s*(\d+)""", RegexOption.IGNORE_CASE)
    private val classInTextRegex = Regex(
        pattern = """,\s*(.*?)\s*класс(?:\s*,|\s*$)""",
        options = setOf(RegexOption.IGNORE_CASE),
    )
    private val idFromUrlRegex = Regex("""/(?:pupil|teacher|user)/(\d+)""")

    fun parse(html: String, role: UserRole = UserRole.PUPIL, pageUrl: String? = null): User? {
        return when (role) {
            UserRole.PUPIL -> parsePupil(html, pageUrl)
            UserRole.TEACHER -> parseTeacher(html, pageUrl)
            UserRole.ORGANIZATOR -> null
        }
    }

    fun parsePupil(html: String, pageUrl: String? = null): Pupil? {
        val unescaped = extractCleanH1(html) ?: return null

        val rawName = extractName(unescaped)
        val id = extractId(unescaped, pageUrl)
        val className = extractClass(unescaped)
        val schoolName = extractSchoolName(html)

        if (rawName.isNotEmpty() && !id.isNullOrEmpty() && !className.isNullOrEmpty()) {
            val parts = rawName.split(whitespaceRegex).filter { it.isNotBlank() }
            val surname = parts.firstOrNull()

            return Pupil(
                name = parts.getOrNull(1) ?: rawName,
                surname = surname,
                schoolsById = id,
                schoolName = schoolName,
                pupilClass = className,
                id = id,
            )
        }
        return null
    }

    fun parseTeacher(html: String, pageUrl: String? = null): Teacher? {
        val unescaped = extractCleanH1(html) ?: return null

        val rawName = extractName(unescaped)
        val id = extractId(unescaped, pageUrl)
        val schoolName = extractSchoolName(html)

        if (rawName.isNotEmpty() && !id.isNullOrEmpty()) {
            val parts = rawName.split(whitespaceRegex).filter { it.isNotBlank() }
            val surname = parts.firstOrNull()

            return Teacher(
                id = id,
                name = parts.getOrNull(1) ?: rawName,
                surname = surname,
                schoolsById = id,
                schoolName = schoolName,
            )
        }
        return null
    }

    private fun extractName(unescaped: String): String {
        return unescaped.substringBefore(",").trim()
    }

    private fun extractId(unescaped: String, pageUrl: String?): String? {
        return idInTextRegex.find(unescaped)?.groupValues?.get(1)?.trim()
            ?: pageUrl?.let { idFromUrlRegex.find(it)?.groupValues?.get(1)?.trim() }
    }

    private fun extractClass(unescaped: String): String? {
        return classInTextRegex.find(unescaped)?.groupValues?.get(1)?.trim()
    }

    private fun extractSchoolName(html: String): String? {
        val headerMatch = schoolHeaderRegex.find(html)
        if (headerMatch != null) {
            val textOnly = htmlTagRegex.replace(headerMatch.groupValues[1], " ")
            val normalized = whitespaceRegex.replace(textOnly, " ").trim()
            val clean = unescapeHtml(normalized)
            if (clean.isNotBlank()) return clean
        }

        val titleMatch = schoolRegex.find(html) ?: return null
        val rawTitle = titleMatch.groupValues[1]
        val textOnly = htmlTagRegex.replace(rawTitle, " ")
        val normalized = whitespaceRegex.replace(textOnly, " ").trim()
        val unescaped = unescapeHtml(normalized)
        if (unescaped.isBlank()) return null

        if (unescaped.contains("—")) {
            val after = unescaped.substringAfterLast("—").trim()
            if (after.isNotBlank()) return after
        }
        if (unescaped.contains(" - ")) {
            val after = unescaped.substringAfterLast(" - ").trim()
            if (after.isNotBlank()) return after
        }

        val lowerTitle = unescaped.lowercase()
        val keywordIndices = listOf("гуо", "гудо", "гимнази", "средняя школа", "школа", "лицей", "колледж", "ясли")
            .map { lowerTitle.indexOf(it) }
            .filter { it >= 0 }

        if (keywordIndices.isNotEmpty()) {
            val minIndex = keywordIndices.minOrNull() ?: 0
            val schoolCandidate = unescaped.substring(minIndex).trim()
            if (schoolCandidate.isNotBlank()) {
                return schoolCandidate
            }
        }

        val sentences = unescaped.split(".").map { it.trim() }.filter { it.isNotBlank() }
        if (sentences.size > 2) {
            return sentences.last()
        }

        return null
    }

    private fun extractCleanH1(html: String): String? {
        val h1Match = h1Regex.find(html) ?: return null
        val rawH1 = h1Match.groupValues[1]
        val textOnly = htmlTagRegex.replace(rawH1, " ")
        val normalized = whitespaceRegex.replace(textOnly, " ").trim()
        return unescapeHtml(normalized)
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace(Regex("&#(x[0-9a-fA-F]+|[0-9]+);")) { match ->
                val encoded = match.groupValues[1]
                val codePoint = if (encoded.startsWith("x")) encoded.drop(1).toIntOrNull(16) else encoded.toIntOrNull()
                if (codePoint != null && Character.isValidCodePoint(codePoint)) String(Character.toChars(codePoint)) else match.value
            }
            .replace("&quot;", "\"")
            .replace("&#34;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace('\u00A0', ' ')
            .replace(whitespaceRegex, " ")
            .trim()
    }
}
