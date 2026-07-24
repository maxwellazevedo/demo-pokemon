package com.example.demopokemon.contract

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

/**
 * Teste de contrato Pact V4 — define o contrato que o consumidor espera da PokeAPI.
 * Usa a API PactBuilder (V4) exigida pelo Pact 4.5+.
 */
@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "PokeAPIProvider")
class PokeApiConsumerTest {

    @Pact(consumer = "PokeApiConsumer")
    fun createPact(builder: PactBuilder): V4Pact = builder
        .usingLegacyDsl()
        .given("Pokemon pikachu exists")
        .uponReceiving("A request for pikachu")
        .path("/api/v2/pokemon/pikachu")
        .method("GET")
        .willRespondWith()
        .status(200)
        .body(
            """
            {
                "name": "pikachu",
                "abilities": [{"ability": {"name": "static"}}, {"ability": {"name": "lightning-rod"}}],
                "moves": [{"move": {"name": "thunder-shock"}}, {"move": {"name": "quick-attack"}}]
            }
            """.trimIndent()
        )
        .toPact(V4Pact::class.java)

    @Test
    @PactTestFor(pactMethod = "createPact")
    fun `deve buscar detalhes do pikachu conforme contrato`(mockServer: MockServer) {
        val webClient = WebClient.create(mockServer.getUrl())

        val response = webClient.get()
            .uri("/api/v2/pokemon/pikachu")
            .retrieve()
            .bodyToMono(String::class.java)

        StepVerifier.create(response)
            .expectNextMatches { body ->
                body.contains("pikachu") && body.contains("static") && body.contains("thunder-shock")
            }
            .verifyComplete()
    }
}
