package com.news4live.app

import com.news4live.app.data.models.DtoMappers.toDomain
import com.news4live.app.data.models.RenderedTextDto
import com.news4live.app.data.models.WordPressPostDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordPressDtoMappingTest {

    @Test
    fun testPostDtoToDomainMapping() {
        val dto = WordPressPostDto(
            id = 23425,
            date = "2026-09-08T11:36:37",
            slug = "sample-slug",
            link = "https://news4live.com/sample-slug/",
            title = RenderedTextDto(rendered = "आईएएस सलोनी गौतम &#8211; संवाद"),
            content = RenderedTextDto(rendered = "<p>छात्राओं का मार्गदर्शन किया</p>"),
            excerpt = RenderedTextDto(rendered = "<p>संक्षिप्त विवरण</p>"),
            categories = listOf(31L, 37L)
        )

        val catMap = mapOf(31L to "ब्रेकिंग न्यूज़", 37L to "उत्तर प्रदेश")
        val article = dto.toDomain(categoryMap = catMap)

        assertEquals(23425L, article.id)
        assertEquals("आईएएस सलोनी गौतम – संवाद", article.title)
        assertEquals("संक्षिप्त विवरण", article.cleanExcerpt)
        assertTrue(article.isBreaking) // Category 31 is breaking
        assertEquals("ब्रेकिंग न्यूज़", article.categoryName)
        assertEquals("https://news4live.com/sample-slug/", article.canonicalUrl)
    }
}
