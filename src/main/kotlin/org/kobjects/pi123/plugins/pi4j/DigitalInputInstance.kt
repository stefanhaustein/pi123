package org.kobjects.pi123.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.pi123.pluginapi.FunctionInstance

class DigitalInputInstance(
    val plugin: Pi4jPlugin,
    val configuration: Map<String, Any>,
    val callback: ((Any) -> Unit)?
) : FunctionInstance {

    var pin: PinManager? = null

    override fun attach() {
       pin = plugin.getPin(PinType.DIGITAL_INPUT, configuration)
        println("Attached: $pin")
        if (callback != null) {
            println("Callback added: $callback")
            pin!!.listeners.add(callback)
        }
    }

    override fun apply(params: Map<String, Any>): Any =
        pin?.getState() ?: Unit

    override fun detach() {
        if (callback != null) {
            pin?.listeners?.remove(callback)
        }
    }




}