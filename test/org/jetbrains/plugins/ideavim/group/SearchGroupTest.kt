package org.jetbrains.plugins.ideavim.group

import com.intellij.openapi.util.Ref
import com.intellij.testFramework.UsefulTestCase
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.helper.RunnableHelper
import com.maddyhome.idea.vim.helper.StringHelper.parseKeys
import com.maddyhome.idea.vim.option.Options
import com.maddyhome.idea.vim.option.ToggleOption
import org.jetbrains.plugins.ideavim.VimTestCase
import java.util.*

/**
 * @author Alex Plate
 */
class SearchGroupTest : VimTestCase() {
    fun `test one letter`() {
        val pos = search("w",
                """<caret>one
                  |two
               """.trimMargin())
        assertEquals(5, pos)
    }

    fun `test end of line`() {
        val pos = search("$",
                """<caret>I found it in a legendary land
                  |all rocks and lavender and tufted grass,
               """.trimMargin())
        assertEquals(30, pos)
    }

    // VIM-146
    fun `test end of line with highlighting`() {
        setHighlightSearch()
        val pos = search("$",
                """<caret>I found it in a legendary land
                  |all rocks and lavender and tufted grass,
               """.trimMargin())
        assertEquals(30, pos)
    }

    fun `test "and" without branches`() {
        val pos = search("\\&",
                """<caret>I found it in a legendary land
                  |all rocks and lavender and tufted grass,
               """.trimMargin())
        assertEquals(1, pos)
    }

    // VIM-226
    fun `test "and" without branches with highlighting`() {
        setHighlightSearch()
        val pos = search("\\&",
                """<caret>I found it in a legendary land
                  |all rocks and lavender and tufted grass,
               """.trimMargin())
        assertEquals(1, pos)
    }

    // VIM-528
    fun `test not found`() {
        val pos = search("(found)",
                """<caret>I found it in a legendary land
                  |all rocks and lavender and tufted grass,
               """.trimMargin())
        assertEquals(-1, pos)
    }

    // VIM-528
    fun `test grouping`() {
        val pos = search("\\(found\\)",
                """<caret>I found it in a legendary land
                  |all rocks and lavender and tufted grass,
               """.trimMargin())
        assertEquals(2, pos)
    }

    // VIM-855
    fun `test character class regression`() {
        val pos = search("[^c]b",
                "<caret>bb\n")
        assertEquals(0, pos)
    }

    // VIM-855
    fun `test character class regression case insensitive`() {
        val pos = search("\\c[ABC]b",
                "<caret>dd\n")
        assertEquals(-1, pos)
    }

    // VIM-856
    fun `test negative lookbehind regression`() {
        val pos = search("a\\@<!b",
                "<caret>ab\n")
        assertEquals(-1, pos)
    }

    fun `test smart case search case insensitive`() {
        setIgnoreCaseAndSmartCase()
        val pos = search("tostring",
                "obj.toString();\n")
        assertEquals(4, pos)
    }

    fun `test smart case search case sensitive`() {
        setIgnoreCaseAndSmartCase()
        val pos = search("toString",
                """obj.tostring();
                 |obj.toString();""".trimMargin())
        assertEquals(20, pos)
    }

    fun `test search motion`() {
        typeTextInFile(parseKeys("/", "two", "<Enter>"),
                "<caret>one two\n")
        assertOffset(4)
    }

    // |/pattern/e|
    fun `test search e motion offset`() {
        typeTextInFile(parseKeys("/", "two/e", "<Enter>"),
                "<caret>one two three")
        assertOffset(6)
    }

    // |/pattern/e|
    fun `test search e-1 motion offset`() {
        typeTextInFile(parseKeys("/", "two/e-1", "<Enter>"),
                "<caret>one two three")
        assertOffset(5)
    }

    // |/pattern/e|
    fun `test search e+2 motion offset`() {
        typeTextInFile(parseKeys("/", "two/e+2", "<Enter>"),
                "<caret>one two three")
        assertOffset(8)
    }

    // |/pattern/s|
    fun `test search s motion offset`() {
        typeTextInFile(parseKeys("/", "two/s", "<Enter>"),
                "<caret>one two three")
        assertOffset(4)
    }

    // |/pattern/s|
    fun `test search s-2 motion offset`() {
        typeTextInFile(parseKeys("/", "two/s-2", "<Enter>"),
                "<caret>one two three")
        assertOffset(2)
    }

    // |/pattern/s|
    fun `test search s+1 motion offset`() {
        typeTextInFile(parseKeys("/", "two/s+1", "<Enter>"),
                "<caret>one two three")
        assertOffset(5)
    }

    // |/pattern/b|
    fun `test search b motion offset`() {
        typeTextInFile(parseKeys("/", "two/b", "<Enter>"),
                "<caret>one two three")
        assertOffset(4)
    }

    // |/pattern/b|
    fun `test search b-2 motion offset`() {
        typeTextInFile(parseKeys("/", "two/b-2", "<Enter>"),
                "<caret>one two three")
        assertOffset(2)
    }

    // |/pattern/b|
    fun `test search b+1 motion offset`() {
        typeTextInFile(parseKeys("/", "two/b+1", "<Enter>"),
                "<caret>one two three")
        assertOffset(5)
    }

    fun `test search above line motion offset`() {
        typeTextInFile(parseKeys("/", "rocks/-1", "<Enter>"),
                """I found it in a legendary land
                 |<caret>all rocks and lavender and tufted grass,
                 |where it was settled on some sodden sand
                 |hard by the torrent of a mountain pass.""".trimMargin())
        assertOffset(0)
    }

    fun `test search below line motion offset`() {
        typeTextInFile(parseKeys("/", "rocks/+2", "<Enter>"),
                """I found it in a legendary land
                 |<caret>all rocks and lavender and tufted grass,
                 |where it was settled on some sodden sand
                 |hard by the torrent of a mountain pass.""".trimMargin())
        assertOffset(113)
    }

    // |i_CTRL-K|
    fun `test search digraph`() {
        typeTextInFile(parseKeys("/", "<C-K>O:", "<Enter>"),
                "<caret>Hallo Österreich!\n")
        assertOffset(6)
    }

    private fun setIgnoreCaseAndSmartCase() {
        val options = Options.getInstance()
        options.resetAllOptions()
        val ignoreCaseOption = options.getOption("ignorecase")
        val smartCaseOption = options.getOption("smartcase")
        UsefulTestCase.assertInstanceOf(ignoreCaseOption, ToggleOption::class.java)
        UsefulTestCase.assertInstanceOf(smartCaseOption, ToggleOption::class.java)
        (ignoreCaseOption as ToggleOption).set()
        (smartCaseOption as ToggleOption).set()
    }

    private fun setHighlightSearch() {
        val options = Options.getInstance()
        options.resetAllOptions()
        val option = options.getOption("hlsearch") as ToggleOption
        UsefulTestCase.assertInstanceOf(option, ToggleOption::class.java)
        option.set()
    }

    private fun search(pattern: String, input: String): Int {
        myFixture.configureByText("a.java", input)
        val editor = myFixture.editor
        val project = myFixture.project
        val searchGroup = VimPlugin.getSearch()
        val ref = Ref.create(-1)
        RunnableHelper.runReadCommand(project, {
            val n = searchGroup.search(editor, pattern, 1, EnumSet.of(CommandFlags.FLAG_SEARCH_FWD), false)
            ref.set(n)
        }, null, null)
        return ref.get()
    }
}