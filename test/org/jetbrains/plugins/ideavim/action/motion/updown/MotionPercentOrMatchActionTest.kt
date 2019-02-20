package org.jetbrains.plugins.ideavim.action.motion.updown

import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import org.jetbrains.plugins.ideavim.VimTestCase

/**
 * @author Alex Plate
 */
class MotionPercentOrMatchActionTest : VimTestCase() {
    fun `test percent match simple`() {
        typeTextInFile(parseKeys("%"),
                "foo(b<caret>ar)\n")
        assertOffset(3)
    }

    fun `test percent match multi line`() {
        typeTextInFile(parseKeys("%"),
                """foo(bar,
                     |baz,
                     |<caret>quux)
               """.trimMargin())
        assertOffset(3)
    }

    fun `test percent visual mode match multi line end of line`() {
        typeTextInFile(parseKeys("v$%"),
                """<caret>foo(
                  |bar)""".trimMargin())
        assertOffset(8)
    }

    fun `test percent visual mode match from start multi line end of line`() {
        typeTextInFile(parseKeys("v$%"),
                """<caret>(
                  |bar)""".trimMargin())
        assertOffset(5)
    }

    fun `test percent visual mode find brackets on the end of line`() {
        typeTextInFile(parseKeys("v$%"),
                """foo(<caret>bar)""")
        assertOffset(3)
    }

    fun `test percent twice visual mode find brackets on the end of line`() {
        typeTextInFile(parseKeys("v$%%"),
                """foo(<caret>bar)""")
        assertOffset(7)
    }

    fun `test percent match parens in string`() {
        typeTextInFile(parseKeys("%"),
                """foo(bar, "foo(bar", <caret>baz)
               """)
        assertOffset(3)
    }

    fun `test percent match xml comment start`() {
        configureByXmlText("<caret><!-- foo -->")
        typeText(parseKeys("%"))
        myFixture.checkResult("<!-- foo --<caret>>")
    }

    fun `test percent doesnt match partial xml comment`() {
        configureByXmlText("<!<caret>-- ")
        typeText(parseKeys("%"))
        myFixture.checkResult("<!<caret>-- ")
    }

    fun `test percent match xml comment end`() {
        configureByXmlText("<!-- foo --<caret>>")
        typeText(parseKeys("%"))
        myFixture.checkResult("<caret><!-- foo -->")
    }

    fun `test percent match java comment start`() {
        configureByJavaText("/<caret>* foo */")
        typeText(parseKeys("%"))
        myFixture.checkResult("/* foo *<caret>/")
    }

    fun `test percent doesnt match partial java comment`() {
        configureByJavaText("<caret>/* ")
        typeText(parseKeys("%"))
        myFixture.checkResult("<caret>/* ")
    }

    fun `test percent match java comment end`() {
        configureByJavaText("/* foo <caret>*/")
        typeText(parseKeys("%"))
        myFixture.checkResult("<caret>/* foo */")
    }

    fun `test percent match java doc comment start`() {
        configureByJavaText("/*<caret>* foo */")
        typeText(parseKeys("%"))
        myFixture.checkResult("/** foo *<caret>/")
    }

    fun `test percent match java doc comment end`() {
        configureByJavaText("/** foo *<caret>/")
        typeText(parseKeys("%"))
        myFixture.checkResult("<caret>/** foo */")
    }

    fun `test percent doesnt match after comment start`() {
        configureByJavaText("/*<caret> foo */")
        typeText(parseKeys("%"))
        myFixture.checkResult("/*<caret> foo */")
    }

    fun `test percent doesnt match before comment end`() {
        configureByJavaText("/* foo <caret> */")
        typeText(parseKeys("%"))
        myFixture.checkResult("/* foo <caret> */")
    }
}