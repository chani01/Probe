package com.chani.probe.internal

internal object JsonFormatter {

    fun format(jsonString: String): String? {
        return try {
            val json = jsonString.trim()

            if (json.isEmpty()) return null

            val result = StringBuilder()
            var indentLevel = 0
            var inString = false
            var escapeNext = false

            for (i in json.indices) {
                val char = json[i]

                when {
                    escapeNext -> {
                        result.append(char)
                        escapeNext = false
                    }
                    char == '\\' -> {
                        result.append(char)
                        escapeNext = true
                    }
                    char == '"' -> {
                        result.append(char)
                        inString = !inString
                    }
                    !inString -> {
                        when (char) {
                            '{', '[' -> {
                                result.append(char)
                                result.append('\n')
                                indentLevel++
                                result.append("  ".repeat(indentLevel))
                            }
                            '}', ']' -> {
                                result.append('\n')
                                indentLevel--
                                result.append("  ".repeat(indentLevel))
                                result.append(char)
                            }
                            ',' -> {
                                result.append(char)
                                result.append('\n')
                                result.append("  ".repeat(indentLevel))
                            }
                            ':' -> {
                                result.append(char)
                                result.append(' ')
                            }
                            ' ', '\n', '\r', '\t' -> {
                                // Skip whitespace
                            }
                            else -> result.append(char)
                        }
                    }
                    else -> result.append(char)
                }
            }

            result.toString()
        } catch (e: Exception) {
            null
        }
    }
}