package dev.happe.myrecipes.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

expect fun platformEngine(): HttpClientEngine

fun buildHttpClient(token: String?): HttpClient = HttpClient(platformEngine()) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
    }
    if (token != null) {
        install(Auth) {
            bearer {
                loadTokens { BearerTokens(token, "") }
            }
        }
    }
}
