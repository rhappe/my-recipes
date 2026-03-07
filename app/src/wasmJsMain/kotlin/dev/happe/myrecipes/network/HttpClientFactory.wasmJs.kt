package dev.happe.myrecipes.network

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

actual fun platformEngine(): HttpClientEngine = Js.create()
