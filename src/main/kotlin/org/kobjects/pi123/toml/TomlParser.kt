package org.kobjects.pi123.toml

import io.netty.handler.codec.http.HttpResponseStatus.parseLine

object TomlParser {

    fun parse(input: String): Map<String, Map<String, Any>> {
        val result = mutableMapOf<String, Map<String, Any>>()
        var currentSectionMap = mutableMapOf<String, Any>()
        var currentSectionName = ""

        for (line in input.lineSequence().map { it.trim() }) {
            if (line.startsWith("[")) {
                require(line.endsWith("]"))
                if (currentSectionMap.isNotEmpty()) {
                    result[currentSectionName] = currentSectionMap.toMap()
                }
                currentSectionName = line.substring(1, line.length - 1)
                currentSectionMap = mutableMapOf<String, Any>()
            } else {
                val cut = line.indexOf("=")
                if (cut != -1) {
                    val key = line.substring(0, cut).trim()
                    val raw = line.substring(cut + 1).trim()
                    val value = if (raw.startsWith("\"")) {
                        require(raw.endsWith("\""))
                        raw.substring(1, raw.length - 1)
                    } else {
                        raw.toDouble()
                    }
                    currentSectionMap[key] = value
                } else if (line.isNotEmpty()) {
                    throw IllegalArgumentException("Unexpected line '$line'")
                }
            }
        }
        if (currentSectionMap.isNotEmpty()) {
            result[currentSectionName] = currentSectionMap.toMap()
        }
        return result.toMap()
    }

}