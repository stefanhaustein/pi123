package org.kobjects.tablecraft.model.expression

import kotlinx.datetime.*
import org.kobjects.tablecraft.model.RuntimeContext
import org.kobjects.tablecraft.model.Values
import kotlin.time.DurationUnit

abstract class Expression {

    abstract fun eval(context: RuntimeContext): Any

    abstract val children: Collection<Expression>

    fun evalDouble(context: RuntimeContext): Double = Values.toDouble(eval(context))

    fun evalInt(context: RuntimeContext): Int = Values.toInt(eval(context))

    fun evalBoolean(context: RuntimeContext): Boolean = Values.toBoolean(eval(context))

    open fun attach() = Unit

    open fun detach() = Unit

    fun attachAll() {
        for (child in children) {
            child.attachAll()
        }
        try {
            attach()
        } catch(e: Exception ) {
            System.err.println("Error attaching $this")
            e.printStackTrace()
        }
    }


    fun detachAll(){
        for (child in children) {
            child.detachAll()
        }
        try {
            detach()
        } catch(e: Exception ) {
        e.printStackTrace()
    }
    }

    companion object {
        val ZERO_TIME = LocalDateTime(1900, 1, 1, 0, 0)


    }
}