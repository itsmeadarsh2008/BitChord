package com.music.bitchord.data.lyrics

/**
 * A minimal XML DOM for the machine-generated lyric documents, replacing
 * `javax.xml` so TTML parsing compiles in `commonMain` with no new
 * dependency.
 *
 * Deliberately small on purpose: these documents are regular
 * `<p>`/`<span>` trees written by Apple Music's exporter, and the parse
 * only ever walks elements, attributes and text. What a full parser would
 * do that this one won't:
 * - resolve anything external (`<!DOCTYPE>`, external entities, XIncludes
 *   are skipped, never fetched — the XXE hardening the JVM parse needed
 *   is inherent here);
 * - validate, keep processing instructions, or preserve comments.
 *
 * Qualified names stay intact (`ttm:agent`, `xml:id`), matching the
 * namespace-unaware DOM the Android parse used, so [TtmlLyrics] ports
 * verbatim. Whitespace text nodes are kept — the word merger reads word
 * boundaries off them.
 */
internal class XmlNode(
    /** Qualified tag name as written (`p`, `span`, `ttm:agent`), or `#text`. */
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
    val children: List<XmlNode> = emptyList(),
    private val text: String? = null,
) {
    val isText: Boolean get() = text != null
    val isElement: Boolean get() = text == null

    fun getAttribute(name: String): String = attributes[name].orEmpty()

    /** This node plus every descendant with [tag], in document order. */
    fun getElementsByTagName(tag: String): List<XmlNode> {
        val found = ArrayList<XmlNode>()
        if (!isText && name == tag) found += this
        for (child in children) found += child.getElementsByTagName(tag)
        return found
    }

    /** Concatenated text of this node and every descendant. */
    val textContent: String
        get() = buildString {
            if (text != null) append(text)
            for (child in children) append(child.textContent)
        }
}

/** Parses [input] or throws on malformed XML; callers turn that into null. */
internal fun parseXml(input: String): XmlNode {
    val parser = Parser(input)
    parser.skipPrologue()
    val root = parser.parseElement()
        ?: throw IllegalArgumentException("No root element")
    return root
}

private class Parser(private val input: String) {
    private var pos = 0

    fun skipPrologue() {
        while (true) {
            skipWhitespace()
            if (peek(2) == "<?") skipUntil("?>")
            else if (peek(9) == "<!DOCTYPE") skipDoctype()
            else if (peek(4) == "<!--") skipUntil("-->")
            else return
        }
    }

    fun parseElement(): XmlNode? {
        skipWhitespace()
        if (pos >= input.length || input[pos] != '<') return null
        if (peek(2) == "</") return null
        pos++ // <
        val name = readName()
        if (name.isEmpty()) throw IllegalArgumentException("Empty tag at $pos")
        val attributes = LinkedHashMap<String, String>()
        while (true) {
            skipWhitespace()
            if (pos >= input.length) throw IllegalArgumentException("Unclosed <$name>")
            when {
                peek(2) == "/>" -> {
                    pos += 2
                    return XmlNode(name, attributes)
                }
                input[pos] == '>' -> {
                    pos++
                    break
                }
                else -> {
                    val attr = readName()
                    if (attr.isEmpty()) throw IllegalArgumentException("Bad attribute in <$name>")
                    skipWhitespace()
                    var value = ""
                    if (pos < input.length && input[pos] == '=') {
                        pos++
                        skipWhitespace()
                        value = readQuoted()
                    }
                    attributes[attr] = EnhancedLrc.decodeEntities(value)
                }
            }
        }
        val children = ArrayList<XmlNode>()
        val text = StringBuilder()
        fun flushText() {
            if (text.isNotEmpty()) {
                children += XmlNode("#text", text = text.toString())
                text.clear()
            }
        }
        while (true) {
            if (pos >= input.length) throw IllegalArgumentException("Unclosed <$name>")
            when {
                peek(2) == "</" -> {
                    pos += 2
                    val close = readName()
                    if (close != name) throw IllegalArgumentException("Mismatched </$close> for <$name>")
                    skipWhitespace()
                    if (pos >= input.length || input[pos] != '>') {
                        throw IllegalArgumentException("Bad </$close>")
                    }
                    pos++
                    flushText()
                    return XmlNode(name, attributes, children)
                }
                peek(9) == "<![CDATA[" -> {
                    pos += 9
                    val end = input.indexOf("]]>", pos)
                    if (end < 0) throw IllegalArgumentException("Unclosed CDATA")
                    text.append(input.substring(pos, end))
                    pos = end + 3
                }
                peek(4) == "<!--" -> skipUntil("-->")
                input[pos] == '<' -> {
                    flushText()
                    children += parseElement()
                        ?: throw IllegalArgumentException("Bad child of <$name>")
                }
                else -> {
                    val next = input.indexOf('<', pos)
                    val end = if (next < 0) input.length else next
                    text.append(EnhancedLrc.decodeEntities(input.substring(pos, end)))
                    pos = end
                }
            }
        }
    }

    private fun skipDoctype() {
        // `<!DOCTYPE ...>` can nest `[...]` internal subsets; walk depth.
        var depth = 0
        while (pos < input.length) {
            when {
                input[pos] == '[' -> { depth++; pos++ }
                input[pos] == ']' -> { depth--; pos++ }
                input[pos] == '>' && depth <= 0 -> { pos++; return }
                input[pos] == '"' || input[pos] == '\'' -> readQuoted()
                else -> pos++
            }
        }
    }

    private fun skipUntil(end: String) {
        val i = input.indexOf(end, pos)
        pos = if (i < 0) input.length else i + end.length
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) pos++
    }

    private fun peek(n: Int): String =
        input.substring(pos, minOf(pos + n, input.length))

    private fun readName(): String {
        val start = pos
        while (pos < input.length && !input[pos].isWhitespace() &&
            input[pos] != '>' && input[pos] != '/' && input[pos] != '=' &&
            input[pos] != '?' && input[pos] != '!'
        ) pos++
        return input.substring(start, pos)
    }

    private fun readQuoted(): String {
        if (pos >= input.length) return ""
        val quote = input[pos]
        if (quote != '"' && quote != '\'') return ""
        pos++
        val start = pos
        while (pos < input.length && input[pos] != quote) pos++
        val value = input.substring(start, pos)
        if (pos < input.length) pos++
        return value
    }
}
