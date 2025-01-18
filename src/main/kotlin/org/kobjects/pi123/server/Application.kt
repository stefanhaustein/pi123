package org.kobjects.pi123.server

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kobjects.pi123.model.Model
import org.kobjects.pi123.model.Sheet
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    routing {
        post("/update/{cell}") {
            val cell = call.parameters["cell"]!!
            val text = call.receiveText()
            Model.withLock {
                Model.set(cell, text, it)
                Model.save()
            }
            call.respond(HttpStatusCode.OK, null)
        }
        get("/sheet/{name}") {
            val name = call.parameters["name"]!!
            val tag = call.request.queryParameters["tag"]!!.toLong()

            if (tag >= Model.modificationTag) {
                suspendCoroutine<Unit> { continuation ->
                    Model.withLock {
                        Model.listeners.add {
                            continuation.resume(Unit)
                        }
                    }
                }
            }
            val result = Model.withLock {
                val sheet = Model.sheets[name]!!
                sheet.serialize(tag, true)
            }
            call.respondText("tag = ${Model.modificationTag}\n$result", ContentType.Text.Plain, HttpStatusCode.OK,)
        }

        /* get("/") {
             call.respondText("Hello World!")
         }*/
        // Static plugin. Try to access `/static/index.html`
        staticFiles("/", File("src/main/resources/static"))
        //staticResources("/", "static")
    }
}
