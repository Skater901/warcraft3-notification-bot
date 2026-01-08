package au.com.skater901.wc3.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.ClientRequestFilter
import java.net.URI

private val mapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()

fun createClient(wireMockInfo: WireMockRuntimeInfo): Client = ClientBuilder.newClient()
    .register(JacksonJsonProvider(mapper))
    .register(ClientRequestFilter { request ->
        request.uri = URI.create(wireMockInfo.httpBaseUrl + request.uri.toString())
    })