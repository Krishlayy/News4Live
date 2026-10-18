package com.news4live.app.util

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

object HtmlCompatHelper {

    /**
     * Cleans HTML entities and strips HTML tags, returning plain text
     */
    fun cleanHtmlToPlainText(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return try {
            val doc = Jsoup.parse(html)
            doc.text().trim()
        } catch (e: Exception) {
            // Fallback: regex strip
            html.replace(Regex("<[^>]*>"), "")
                .replace("&nbsp;", " ")
                .replace("&#8211;", "–")
                .replace("&#8217;", "’")
                .replace("&#8216;", "‘")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .trim()
        }
    }

    /**
     * Sanitizes HTML to only allow safe elements: p, b, i, em, strong, u, h1..h6, blockquote, img, a, ul, ol, li
     * Completely eliminates scripts, iframes with unsafe sources, and onclick/javascript event handlers.
     */
    fun sanitizeArticleHtml(rawHtml: String?): String {
        if (rawHtml.isNullOrBlank()) return ""
        val safelist = Safelist.relaxed()
            .addTags("figure", "figcaption", "article", "section", "hr")
            .addAttributes("img", "src", "alt", "width", "height", "class", "loading")
            .addAttributes("a", "href", "target", "rel")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https")
        
        return Jsoup.clean(rawHtml, safelist)
    }

    /**
     * Decodes title strings that might have HTML entities
     */
    fun decodeHtmlTitle(title: String?): String {
        if (title.isNullOrBlank()) return ""
        return try {
            Jsoup.parse(title).text()
        } catch (e: Exception) {
            title.replace("&#8211;", "–")
                .replace("&#8217;", "’")
                .replace("&#8216;", "‘")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#038;", "&")
        }
    }
}
