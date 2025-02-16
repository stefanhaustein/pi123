package org.kobjects.tablecraft.model.expression

import kotlinx.datetime.Instant
import org.kobjects.tablecraft.model.RuntimeContext
import org.kobjects.tablecraft.model.Values

class BinaryOperatorExpression(
    val name: String,
    val leftOperand: Expression,
    val rightOperand: Expression
): Expression() {

    override fun eval(context: RuntimeContext): Any {
        val l = leftOperand.eval(context) ?: 0.0
        val r = rightOperand.eval(context) ?: 0.0

        if (name == "&") {
            return "$l$r"
        }

        if ((l is Int || l is Boolean) && (r is Int || r is Boolean)) {
            val li = Values.toInt(l)
            val ri = Values.toInt(r)
            when (name) {
                "." -> return (li shr ri) and 1
                "+" -> return li + ri
                "-" -> return li - ri
                "*" -> return li * ri
                "//" -> return li / ri
                "=" -> return li == ri
                "<>" -> return li != ri
                "<=" -> return li <= ri
                ">=" -> return li >= ri
                "<" -> return li < ri
                ">" -> return li > ri
            }
        }

        val ld = Values.toDouble(l)
        val rd = Values.toDouble(r)

        return when (name) {
            "+" -> ld + rd
            "-" -> ld - rd
            "*" -> ld * rd
            "/" -> ld / rd
            "^" -> Math.pow(ld, rd)
            "=" -> return ld == rd
            "<>" -> return ld != rd
            "<=" -> return ld <= rd
            ">=" -> return ld >= rd
            "<" -> return ld < rd
            ">" -> return ld > rd
            else -> throw UnsupportedOperationException("$name for Double")
        }
    }

    override val children: Collection<Expression>
        get() = listOf(leftOperand, rightOperand)
}