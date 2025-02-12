package org.kobjects.tablecraft.pluginapi

import org.kobjects.tablecraft.model.quote

data class OperationSpec(
    val kind: OperationKind,
    val returnType: Type,
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val tag: Long = 0,
    val createFn: (host: OperationHost) -> OperationInstance,
) {
    fun toJson(): String {
        val filteredParams = parameters.filter { kind != OperationKind.PORT_CONSTRUCTOR || it.kind != ParameterKind.RUNTIME }
        val convertedParams = filteredParams.joinToString { it.toJson() }
        return """{"name":${name.quote()},"kind":"$kind","returnType":"$returnType","description":${description.quote()},"params":[$convertedParams]}"""
    }

    companion object {
        fun createTombstone(name: String, tag: Long) = OperationSpec(
            OperationKind.TOMBSTONE, Type.NUMBER, name, "Deleted Operation '$name'.", emptyList(), tag
        ) {
            throw UnsupportedOperationException("Tombstone for '$name' can't be instantiated.")
        }
    }
}

