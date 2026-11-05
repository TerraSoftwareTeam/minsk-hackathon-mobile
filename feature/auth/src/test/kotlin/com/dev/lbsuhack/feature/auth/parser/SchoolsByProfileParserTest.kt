package com.dev.lbsuhack.feature.auth.data.parser

import com.dev.lbsuhack.core.domain.model.Pupil
import com.dev.lbsuhack.core.domain.model.Teacher
import com.dev.lbsuhack.core.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchoolsByProfileParserTest {

    private val parser = SchoolsByProfileParser()

    @Test
    fun parse_extracts_pupil_info_from_exact_html_structure() {
        val html = """
            <div class="grid_pst_c" style="margin-bottom: 20px;">
                <div class="title_box" style="margin-bottom: 10px;">
                    <h1>
                        Смирнов Владислав, <a href="/class/76677">11 &quot;М&quot;</a> класс, ID 3575208
                    </h1>
                </div>
            </div>
        """.trimIndent()

        val user = parser.parse(html, role = UserRole.PUPIL)

        assertNotNull(user)
        assertTrue(user is Pupil)
        val pupil = user as Pupil
        assertEquals("Смирнов Владислав", pupil.name)
        assertEquals("11 \"М\"", pupil.pupilClass)
        assertEquals("11 \"М\"", pupil.`class`)
        assertEquals("3575208", pupil.id)
        assertEquals(UserRole.PUPIL, pupil.role)
    }

    @Test
    fun parse_extracts_teacher_without_class_from_profile() {
        val html = """
            <div class="title_box">
                <h1>Божедомова Ирина Валерьевна, Учитель математики, ID 1234567</h1>
            </div>
        """.trimIndent()

        val user = parser.parse(html, role = UserRole.TEACHER)

        assertNotNull(user)
        assertTrue(user is Teacher)
        val teacher = user as Teacher
        assertEquals("Божедомова Ирина Валерьевна", teacher.name)
        assertEquals("1234567", teacher.id)
        assertEquals(UserRole.TEACHER, teacher.role)
    }

    @Test
    fun parse_extracts_teacher_when_role_is_teacher_even_from_pupil_page() {
        val html = """
            <div class="title_box">
                <h1>Смирнов Владислав, <a href="/class/76677">11 &quot;М&quot;</a> класс, ID 3575208</h1>
            </div>
        """.trimIndent()

        val user = parser.parse(html, role = UserRole.TEACHER)

        assertNotNull(user)
        assertTrue(user is Teacher)
        val teacher = user as Teacher
        assertEquals("Смирнов Владислав", teacher.name)
        assertEquals("3575208", teacher.id)
        assertEquals(UserRole.TEACHER, teacher.role)
    }

    @Test
    fun parse_returns_null_when_h1_missing() {
        val html = "<div><span>Some content without h1</span></div>"
        val user = parser.parse(html, role = UserRole.PUPIL)
        assertNull(user)
    }
}
