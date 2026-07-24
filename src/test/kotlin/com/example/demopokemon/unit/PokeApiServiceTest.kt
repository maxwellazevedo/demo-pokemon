package com.example.demopokemon.unit

import com.example.demopokemon.adapter.messaging.PokeProducer
import com.example.demopokemon.application.PokeApiService
import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

class PokeApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var pokemonRepository: PokemonRepositoryPort
    private lateinit var pokemonProducer: PokeProducer
    private lateinit var service: PokeApiService

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        pokemonRepository = mock()
        pokemonProducer = mock()

        service = PokeApiService(
            webClientBuilder = WebClient.builder(),
            pokemonRepository = pokemonRepository,
            pokemonProducer = pokemonProducer,
            objectMapper = ObjectMapper(),
            pokeApiBaseUrl = mockWebServer.url("/").toString()
        )
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchPokemon retorna do cache quando pokemon ja existe no banco`() {
        val cached = Pokemon(id = 1L, name = "pikachu", abilities = listOf("static"), moves = listOf("thunder-shock"))
        whenever(pokemonRepository.findByName("pikachu")).thenReturn(cached)

        StepVerifier.create(service.fetchPokemon("pikachu"))
            .expectNextMatches { it.name == "pikachu" && it.abilities == listOf("static") }
            .verifyComplete()

        verify(pokemonRepository).findByName("pikachu")
        verify(pokemonProducer, never()).sendPokemon(any())
    }

    @Test
    fun `fetchPokemon chama PokeAPI quando nao ha cache e publica no Kafka`() {
        whenever(pokemonRepository.findByName("bulbasaur")).thenReturn(null)

        val apiResponse = """
            {
                "name": "bulbasaur",
                "abilities": [{"ability": {"name": "overgrow"}}, {"ability": {"name": "chlorophyll"}}],
                "moves": [{"move": {"name": "tackle"}}, {"move": {"name": "growl"}}]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(apiResponse).setResponseCode(200).addHeader("Content-Type", "application/json"))

        StepVerifier.create(service.fetchPokemon("bulbasaur"))
            .expectNextMatches { pokemon ->
                pokemon.name == "bulbasaur" &&
                    pokemon.abilities == listOf("overgrow", "chlorophyll") &&
                    pokemon.moves == listOf("tackle", "growl")
            }
            .verifyComplete()

        verify(pokemonProducer).sendPokemon(argThat { name == "bulbasaur" })
    }

    @Test
    fun `fetchAllPokemon retorna lista do repositorio`() {
        val pokemons = listOf(
            Pokemon(name = "pikachu", abilities = listOf("static"), moves = listOf("thunder-shock")),
            Pokemon(name = "eevee", abilities = listOf("run-away"), moves = listOf("tackle"))
        )
        whenever(pokemonRepository.findAll()).thenReturn(pokemons)

        StepVerifier.create(service.fetchAllPokemon())
            .expectNextMatches { it.size == 2 && it[0].name == "pikachu" }
            .verifyComplete()
    }

    @Test
    fun `fetchAllPokemon retorna lista vazia quando banco esta vazio`() {
        whenever(pokemonRepository.findAll()).thenReturn(emptyList())

        StepVerifier.create(service.fetchAllPokemon())
            .expectNextMatches { it.isEmpty() }
            .verifyComplete()
    }
}
