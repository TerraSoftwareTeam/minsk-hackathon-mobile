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
        }
    }

    fun parsePupil(html: String, pageUrl: String? = null): Pupil? {
        val unescaped = extractCleanH1(html) ?: return null

        val name = extractName(unescaped)
        val id = extractId(unescaped, pageUrl)
        val className = extractClass(unescaped)

        if (name.isNotEmpty() && !id.isNullOrEmpty() && !className.isNullOrEmpty()) {
            return Pupil(
                name = name,
                pupilClass = className,
                id = id,
            )
        }
        return null
    }

    fun parseTeacher(html: String, pageUrl: String? = null): Teacher? {
        val unescaped = extractCleanH1(html) ?: return null

        val name = extractName(unescaped)
        val id = extractId(unescaped, pageUrl)

        if (name.isNotEmpty() && !id.isNullOrEmpty()) {
            return Teacher(
                id = id,
                name = name,
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

    private fun extractCleanH1(html: String): String? {
        val h1Match = h1Regex.find(html) ?: return null
        val rawH1 = h1Match.groupValues[1]
        val textOnly = htmlTagRegex.replace(rawH1, " ")
        val normalized = whitespaceRegex.replace(textOnly, " ").trim()
        return unescapeHtml(normalized)
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&quot;", "\"")
            .replace("&#34;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }
}
