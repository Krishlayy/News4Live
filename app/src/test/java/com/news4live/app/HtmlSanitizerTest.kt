package com.news4live.app

import com.news4live.app.domain.model.ParsedHtmlBlock
import com.news4live.app.domain.usecase.HtmlBlockParserUseCase
import com.news4live.app.util.HtmlCompatHelper
import org.junit.Assert.*
import org.junit.Test

class HtmlSanitizerTest {

    @Test
    fun testUnsafeScriptStripping() {
        val maliciousHtml = "<p>सुरक्षित समाचार</p><script>alert('malicious')</script><img src='https://example.com/img.jpg' onerror='alert(1)'>"
        val sanitized = HtmlCompatHelper.sanitizeArticleHtml(maliciousHtml)

        assertFalse(sanitized.contains("<script>"))
        assertFalse(sanitized.contains("onerror"))
        assertTrue(sanitized.contains("सुरक्षित समाचार"))
    }

    @Test
    fun testHtmlTitleEntityDecoding() {
        val encodedTitle = "योगीराज भगवान श्रीकृष्ण &#8211; &amp; &#8216;विशेष रिपोर्ट&#8217;"
        val decoded = HtmlCompatHelper.decodeHtmlTitle(encodedTitle)

        assertEquals("योगीराज भगवान श्रीकृष्ण – & ‘विशेष रिपोर्ट’", decoded)
    }

    @Test
    fun testParseHtmlToBlocks() {
        val sampleHtml = """
            <h2>मुख्य शीर्षक</h2>
            <p>यह पहली समाचार पंक्ति है।</p>
            <blockquote>यह एक महत्वपूर्ण वक्तव्य है</blockquote>
            <ul>
                <li>बिंदु एक</li>
                <li>बिंदु दो</li>
            </ul>
        """.trimIndent()

        val blocks = HtmlBlockParserUseCase.parseHtmlToBlocks(sampleHtml)

        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is ParsedHtmlBlock.Heading)
        assertEquals("मुख्य शीर्षक", (blocks[0] as ParsedHtmlBlock.Heading).text)

        assertTrue(blocks[1] is ParsedHtmlBlock.Paragraph)
        assertEquals("यह पहली समाचार पंक्ति है।", (blocks[1] as ParsedHtmlBlock.Paragraph).text)

        assertTrue(blocks[2] is ParsedHtmlBlock.Blockquote)
        assertEquals("यह एक महत्वपूर्ण वक्तव्य है", (blocks[2] as ParsedHtmlBlock.Blockquote).text)

        assertTrue(blocks[3] is ParsedHtmlBlock.BulletList)
        assertEquals(2, (blocks[3] as ParsedHtmlBlock.BulletList).items.size)
    }
}
