package com.example.demopokemon.integration

import com.example.demopokemon.DemoPokemonApplication
import com.example.demopokemon.adapter.messaging.PokeProducer
import com.example.demopokemon.application.PokeApiService
import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

/**
 * Teste de integração do PokeApiService com banco H2.
 * O PokeProducer é mockado para evitar dependência do Kafka neste contexto.
 */
@SpringBootTest(classes = [DemoPokemonApplication::class])
@ActiveProfiles("test")
class PokeApiIntegrationTest @Autowired constructor(
    private val pokeApiService: PokeApiService,
    private val pokemonRepository: PokemonRepositoryPort
) {
    @MockBean
    private lateinit var pokeProducer: PokeProducer

    @BeforeEach
    fun setup() {
        doNothing().whenever(pokeProducer).sendPokemon(any())
    }

    @Test
    fun `fetchPokemon retorna pokemon do cache quando ja salvo`() {
        val saved = pokemonRepository.save(
            Pokemon(
                name = "pikachu",
                abilities = listOf("static", "lightning-rod"),
                moves = listOf("thunder-shock", "quick-attack")
            )
        )

        val result = pokeApiService.fetchPokemon("pikachu").block()

        assertNotNull(result)
        assertEquals(saved.name, result?.name)
        assertEquals(saved.abilities, result?.abilities)
        assertEquals(saved.moves, result?.moves)
    }

    @Test
    fun `fetchAllPokemon retorna todos os pokemon salvos`() {
        pokemonRepository.save(Pokemon(name = "bulbasaur", abilities = listOf("overgrow"), moves = listOf("tackle")))
        pokemonRepository.save(Pokemon(name = "charmander", abilities = listOf("blaze"), moves = listOf("scratch")))

        val result = pokeApiService.fetchAllPokemon().block()

        assertNotNull(result)
        assertTrue(result!!.size >= 2)
        assertTrue(result.any { it.name == "bulbasaur" })
        assertTrue(result.any { it.name == "charmander" })
    }
}
