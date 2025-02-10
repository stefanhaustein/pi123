package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.Pi4J
import org.kobjects.tablecraft.pluginapi.*

class Pi4jPlugin : Plugin {
    var pi4J = Pi4J.newAutoContext()

    val ports = mutableListOf<Pi4JPort>()

    fun addPort(port: Pi4JPort) {
        ports.add(port)
    }

    fun removePort(remove: Pi4JPort) {
        ports.remove(remove)
        for (port in ports) {
            port.detachPort()
        }
        pi4J.shutdown()
        pi4J = Pi4J.newAutoContext()
    }

    override val operationSpecs = listOf(
        OperationSpec(
            OperationKind.PORT_CONSTRUCTOR,
            Type.BOOLEAN,
            "din",
            "Configures the given pin address for digital input and reports a high value as TRUE and a low value as FALSE.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) {  DigitalInputInstance(this, it) },
        OperationSpec(
            OperationKind.PORT_CONSTRUCTOR,
            Type.NUMBER,
            "pwmin",
            "Configures the given pin address for input and reports the pulse width in seconds.",
            listOf(ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT))
        ) { PwmInputInstance(this, it) },

        OperationSpec(
            OperationKind.PORT_CONSTRUCTOR,
            Type.BOOLEAN,
            "dout",
            "Configures the given pin address for digital output and sets it to 'high' for a TRUE value and to 'low' for a FALSE or 0 value.",
            listOf(
                ParameterSpec("address", ParameterKind.CONFIGURATION, Type.INT),
                ParameterSpec("value", ParameterKind.RUNTIME, Type.INT),
                )
        ) { DigitalOutputInstance(this, it.configuration) },
    )

}