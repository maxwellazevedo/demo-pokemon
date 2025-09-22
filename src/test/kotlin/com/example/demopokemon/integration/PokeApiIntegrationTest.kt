package com.example.demopokemon.integration

import com.example.demopokemon.entity.PokemonEntity
import com.example.demopokemon.repository.PokemonRepository
import com.example.demopokemon.service.PokeApiService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class PokeApiIntegrationTest(
    @Autowired private val pokeApiService: PokeApiService,
    @Autowired private val pokemonRepository: PokemonRepository,
    @Autowired private val restTemplate: TestRestTemplate
) {

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `should fetch and save Pokemon from PokeAPI`() {
        // Arrange
        val pokemonName = "pikachu"

        // Act
        val pokemon = pokeApiService.fetchPokemon(pokemonName).block()

        // Assert
        assertNotNull(pokemon)
        assertEquals(pokemonName, pokemon?.name)

        val savedPokemon = pokemonRepository.findByName(pokemonName)
        assertNotNull(savedPokemon)
        assertEquals(pokemonName, savedPokemon?.name)
    }

    @Test
    fun `should fetch Pokemon from database if already saved`() {
        // Arrange
        val pokemonName = "pikachu"
        val savedPokemon = pokemonRepository.save(
            PokemonEntity(
                name = pokemonName,
                abilities = mutableListOf("static", "lightning-rod"),
                moves = mutableListOf("thunder-shock", "quick-attack")
            )
        )

        // Act
        val pokemon = pokeApiService.fetchPokemon(pokemonName).block()

        // Assert
        assertNotNull(pokemon)
        assertEquals(savedPokemon.name, pokemon?.name)
        assertEquals(savedPokemon.abilities, pokemon?.abilities)
        assertEquals(savedPokemon.moves, pokemon?.moves)
    }
}