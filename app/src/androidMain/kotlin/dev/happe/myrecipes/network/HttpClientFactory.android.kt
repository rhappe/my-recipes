package dev.happe.myrecipes.network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual fun platformEngine(): HttpClientEngine = OkHttp.create()
