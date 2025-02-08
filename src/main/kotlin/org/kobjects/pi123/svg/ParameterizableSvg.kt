package org.kobjects.pi123.svg

import org.kobjects.pi123.pluginapi.ParameterKind
import org.kobjects.pi123.pluginapi.ParameterSpec
import org.kobjects.pi123.pluginapi.Type
import org.kobjects.pi123.svg.parser.ExpressionParser
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class ParameterizableSvg(
    val document: Document
) {
    val parameters: List<ParameterSpec> = buildList {
        val childNodes = document.documentElement.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child.nodeType == Node.ELEMENT_NODE && child.localName.equals("def") && child.namespaceURI == PARAM_NAMESPACE) {
                val name = child.attributes.getNamedItem("name")!!.nodeValue
                val typeName = child.attributes.getNamedItem("type")!!.nodeValue
                val type = when(typeName) {
                    "String" -> Type.TEXT
                    "Number" -> Type.NUMBER
                    "Boolean" -> Type.BOOLEAN
                    else -> throw IllegalArgumentException("Unrecognized type: $typeName")
                }
                val spec = ParameterSpec(
                    name,
                    ParameterKind.RUNTIME,
                    type)
                add(spec)
            }
        }
    }

    val parameterTypes: Map<String, Type> = parameters.map { Pair(it.name, it.type) }.toMap()


    fun parameterized(params: Map<String, String>): Document {
        val convertedParameters = mutableMapOf<String, Any>()
        for ((key, value) in params) {
            val type = parameterTypes[key]
            when (type) {
                null -> System.err.println("Unexpected Parameter $key")
                Type.TEXT -> convertedParameters[key] = value
                Type.NUMBER -> convertedParameters[key] = value.toDouble()
                Type.BOOLEAN -> convertedParameters[key] = value.toBoolean()
                else -> System.err.println("Parameter $key: Int type not supported for SVG Parameterization")
            }
        }
        return if (params.isEmpty()) document else parameterizedCopy(convertedParameters)
    }

    fun parameterizedCopy(params: Map<String, Any>): Document {
        val copiedDocument: Document = createDocumentBuilder().newDocument()
        val copiedRoot = copiedDocument.importNode(document.documentElement, true)
        parameterize(copiedRoot as Element, params)
        copiedDocument.appendChild(copiedRoot)
        return copiedDocument
    }


    companion object {
        val PARAM_NAMESPACE = "http://kobjects.org/svg/param"

        fun createDocumentBuilder(): DocumentBuilder {
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true
            return factory.newDocumentBuilder()
        }

        fun load(file: File): ParameterizableSvg {
            val builder = createDocumentBuilder()
            val doc: Document = builder.parse(file)
            return ParameterizableSvg(doc)
        }

        fun parameterize(element: Element, params: Map<String, Any>) {
            val attrs = element.attributes
            val resolved = mutableMapOf<String, String>()
            for (i in 0 until attrs.length) {
                val attr = attrs.item(i)
                if (attr.namespaceURI == PARAM_NAMESPACE) {
                    try {
                        val value = ExpressionParser.evaluateExpression(attr.textContent, params)
                        resolved[attr.localName] = value.toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            for ((key, value) in resolved) {
                element.setAttribute(key, value)
            }

            val children = element.childNodes
            for (i in 0 until children.length) {
                val child = children.item(i)
                if (child is Element) {
                    parameterize(child, params)
                }
            }
        }
    }

}