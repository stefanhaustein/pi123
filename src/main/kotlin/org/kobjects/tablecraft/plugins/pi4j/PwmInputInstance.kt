package org.kobjects.tablecraft.plugins.pi4j

import com.pi4j.io.gpio.digital.*
import org.kobjects.tablecraft.pluginapi.OperationHost
import org.kobjects.tablecraft.pluginapi.OperationInstance

class PwmInputInstance(
    val plugin: Pi4jPlugin,
    val host: OperationHost,
) : OperationInstance, Pi4JPort, DigitalStateChangeListener {

    var digitalInput: DigitalInput? = null
    var t0: Long = 0
    var value: Double = 0.0

    override fun attach() {
        plugin.addPort(this)
        attachPort()
    }

    override fun attachPort() {
        val address = (host.configuration["address"] as Number).toInt()
        digitalInput = plugin.pi4J.create(DigitalInputConfig.newBuilder(plugin.pi4J).address(address).build())
        digitalInput?.addListener(this)
    }

    override fun apply(params: Map<String, Any>): Any = value

    override fun onDigitalStateChange(event: DigitalStateChangeEvent<out Digital<*, *, *>>?) {
        when (event!!.state().isHigh()) {
            true -> {
                t0 = System.currentTimeMillis()
            }
            false -> {
                val newValue = (System.currentTimeMillis() - t0) / 1000.0
                if (newValue != value && t0 != 0L) {
                    value = newValue
                    host.notifyValueChanged(value)
                }
            }
        }
    }

    override fun detach() {
        detachPort()
        plugin.removePort(this)
    }

    override fun detachPort() {
        digitalInput?.removeListener(this)
    }



}