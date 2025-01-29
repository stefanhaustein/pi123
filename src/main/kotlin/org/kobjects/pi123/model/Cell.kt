package org.kobjects.pi123.model

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import org.kobjects.pi123.model.expression.Expression
import org.kobjects.pi123.model.expression.LiteralExpression
import org.kobjects.pi123.model.parser.FormulaParser
import org.kobjects.pi123.model.parser.ParsingContext
import java.util.concurrent.atomic.AtomicReference

class Cell(
    val sheet: Sheet,
    val id: String
) {
    var rawValue: String = ""
    var expression: Expression = LiteralExpression(null)
    var computedValue_: Any? = null

    var tag = 0L
    var formulaTag = 0L

    val depenencies = mutableListOf<Cell>()
    val dependsOn = mutableListOf<Cell>()


    fun setValue(value: String, runtimeContext: RuntimeContext?) {
        rawValue = value
        expression.detachAll()
        expression = if (value.startsWith("=")) {
            try {
                val context = ParsingContext(this)
                val parsed = FormulaParser.parseExpression(value.substring(1), context)
                parsed.attachAll()
                parsed
            } catch (e: Exception) {
                LiteralExpression(e)
            }
        } else {
            when (value.lowercase()) {
                "true" -> LiteralExpression(true)
                "false" -> LiteralExpression(false)
                else -> {
                    try {
                        LiteralExpression(value.toDouble())
                    } catch (e: Exception) {
                        LiteralExpression(value)
                    }
                }
            }
        }
        if (runtimeContext != null) {
            updateAllDependencies(runtimeContext)
            Model.notifyContentUpdated(runtimeContext)
        }
    }

    fun getComputedValue(context: RuntimeContext): Any? {
        if (context.tag > tag) {
            try {
            computedValue_ = expression.eval(context)
            } catch(e: Exception) {
                e.printStackTrace()
                computedValue_ = e
            }
            tag = context.tag
        }
        return computedValue_
    }

    fun updateAllDependencies(context: RuntimeContext) {
        if (context.tag > tag) {
            getComputedValue(context)
            for (dep in depenencies) {
                dep.updateAllDependencies(context)
            }
        }
    }

    fun serializeValue(sb: StringBuilder) {
        val value = computedValue_
        sb.append('"')
        if (value is Unit || value == null) {
            // empty
        } else if (value is Boolean) {
            sb.append("c:${value.toString().uppercase()}")
        } else if (value is Number) {
            sb.append("r:")
            sb.append(value)
        } else if (value is Exception) {
            sb.append("e:")
            sb.append(value)
        } else if (value is Instant) {
            sb.append("r:")
            val localDateTime = value.toLocalDateTime(TimeZone.currentSystemDefault())
           /* sb.append(localDateTime.date.format(LocalDate.Formats.ISO))
            sb.append(' ') */
            sb.append(localDateTime.time.format(TIME_FORMAT_SECONDS))
        } else {
            sb.append("l:")
            sb.append(value.toString().escape())
        }
        sb.append('"')

    }

    fun serialize(sb: StringBuilder, tag: Long, includeComputed: Boolean) {
        val id = id
        if (formulaTag > tag) {
            sb.append("$id.f = ${rawValue.quote()}\n")
        }
        if (includeComputed && this.tag > tag) {
            sb.append("$id.c = ")
            serializeValue(sb)
            sb.append('\n')
        }
    }

    companion object {
        val TIME_FORMAT_MINUTES = LocalTime.Format {
            hour(); char(':'); minute(); // char(':'); second()
        }
        val TIME_FORMAT_SECONDS = LocalTime.Format {
            hour(); char(':'); minute(); char(':'); second()
        }
    }

}