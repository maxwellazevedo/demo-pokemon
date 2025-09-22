package com.example.demopokemon.contract

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "PokeAPIProvider", port = "8080")
class PokeApiConsumerTest {

    @Pact(consumer = "PokeApiConsumer")
    fun createPact(builder: PactDslWithProvider) = builder
        .given("Pokemon exists")
        .uponReceiving("A request for a Pokemon")
        .path("/api/v2/pokemon/pikachu")
        .method("GET")
        .willRespondWith()
        .status(200)
        .body("""
               {
                   "name": "pikachu",
                   "abilities": [{"ability": {"name": "static"}}],
                   "moves": [{"move": {"name": "quick-attack"}}]
               }
           """)
        .toPact()

    @Test
    fun `should fetch Pokemon details`() {
        val webClient = WebClient.create("http://localhost:8080")
        val response = webClient.get()
            .uri("/api/v2/pokemon/pikachu")
            .retrieve()
            .bodyToMono(String::class.java)

        StepVerifier.create(response)
            .expectNextMatches { it.contains("pikachu") }
            .verifyComplete()
    }
}