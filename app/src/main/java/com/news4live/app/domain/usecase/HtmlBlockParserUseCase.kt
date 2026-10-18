package com.news4live.app.domain.usecase

import com.news4live.app.domain.model.ParsedHtmlBlock
import com.news4live.app.util.HtmlCompatHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

object HtmlBlockParserUseCase {

    fun parseHtmlToBlocks(rawHtml: String?): List<ParsedHtmlBlock> {
        if (rawHtml.isNullOrBlank()) return emptyList()

        val sanitized = HtmlCompatHelper.sanitizeArticleHtml(rawHtml)
        val doc = Jsoup.parseBodyFragment(sanitized)
        val body = doc.body()
        val blocks = mutableListOf<ParsedHtmlBlock>()

        for (child in body.children()) {
            parseElement(child, blocks)
        }

        // If no structured blocks were parsed, fallback to paragraphs
        if (blocks.isEmpty()) {
            val text = body.text().trim()
            if (text.isNotBlank()) {
                blocks.add(ParsedHtmlBlock.Paragraph(text))
            }
        }

        return blocks
    }

    private fun parseElement(element: Element, blocks: MutableList<ParsedHtmlBlock>) {
        val tagName = element.tagName().lowercase()

        when (tagName) {
            "p" -> {
                // Check if p tag contains only an image
                val img = element.selectFirst("img")
                val text = element.text().trim()
                val youtubeId = com.news4live.app.util.YouTubeUtils.extractVideoId(text)

                if (img != null && text.isBlank()) {
                    val src = img.attr("src")
                    val alt = img.attr("alt")
                    if (src.isNotBlank()) {
                        blocks.add(ParsedHtmlBlock.Image(url = src, alt = alt))
                    }
                } else if (youtubeId != null && (text.startsWith("http://") || text.startsWith("https://"))) {
                    // Raw YouTube link in a paragraph
                    blocks.add(ParsedHtmlBlock.VideoEmbed(videoUrl = text, youtubeVideoId = youtubeId))
                } else {
                    if (text.isNotBlank()) {
                        val isItalic = element.selectFirst("em, i") != null
                        val isBold = element.selectFirst("strong, b") != null
                        blocks.add(ParsedHtmlBlock.Paragraph(text = text, isItalic = isItalic, isBold = isBold))
                    }
                }
            }

            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                val level = tagName.substring(1).toIntOrNull() ?: 2
                val text = element.text().trim()
                if (text.isNotBlank()) {
                    blocks.add(ParsedHtmlBlock.Heading(text = text, level = level))
                }
            }

            "figure" -> {
                val img = element.selectFirst("img")
                val caption = element.selectFirst("figcaption")?.text()?.trim()
                val iframe = element.selectFirst("iframe")
                if (img != null) {
                    val src = img.attr("src")
                    val alt = img.attr("alt")
                    if (src.isNotBlank()) {
                        blocks.add(ParsedHtmlBlock.Image(url = src, caption = caption, alt = alt))
                    }
                } else if (iframe != null) {
                    val src = iframe.attr("src")
                    val youtubeId = com.news4live.app.util.YouTubeUtils.extractVideoId(src)
                    blocks.add(ParsedHtmlBlock.VideoEmbed(videoUrl = src, youtubeVideoId = youtubeId))
                }
            }

            "img" -> {
                val src = element.attr("src")
                val alt = element.attr("alt")
                if (src.isNotBlank()) {
                    blocks.add(ParsedHtmlBlock.Image(url = src, alt = alt))
                }
            }

            "blockquote" -> {
                val text = element.text().trim()
                if (text.isNotBlank()) {
                    blocks.add(ParsedHtmlBlock.Blockquote(text = text))
                }
            }

            "ul", "ol" -> {
                val items = element.select("li").map { it.text().trim() }.filter { it.isNotBlank() }
                if (items.isNotEmpty()) {
                    blocks.add(ParsedHtmlBlock.BulletList(items = items))
                }
            }

            "iframe" -> {
                val src = element.attr("src")
                val youtubeId = com.news4live.app.util.YouTubeUtils.extractVideoId(src)
                blocks.add(ParsedHtmlBlock.VideoEmbed(videoUrl = src, youtubeVideoId = youtubeId))
            }

            "div", "section", "article" -> {
                // Recursively parse children of generic containers
                for (nested in element.children()) {
                    parseElement(nested, blocks)
                }
            }

            else -> {
                val text = element.text().trim()
                if (text.isNotBlank()) {
                    blocks.add(ParsedHtmlBlock.Paragraph(text = text))
                }
            }
        }
    }
}
