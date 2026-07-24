package com.example.demopokemon.unit

import com.example.demopokemon.adapter.persistence.PokemonJpaEntity
import com.example.demopokemon.domain.model.Pokemon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PokemonJpaEntityTest {

    @Test
    fun `toDomain mapeia corretamente para modelo de dominio`() {
        val entity = PokemonJpaEntity(
            id = 1L,
            name = "pikachu",
            abilities = listOf("static", "lightning-rod"),
            moves = listOf("thunder-shock", "quick-attack")
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("pikachu", domain.name)
        assertEquals(listOf("static", "lightning-rod"), domain.abilities)
        assertEquals(listOf("thunder-shock", "quick-attack"), domain.moves)
    }

    @Test
    fun `fromDomain mapeia corretamente de modelo de dominio`() {
        val pokemon = Pokemon(
            id = 2L,
            name = "charmander",
            abilities = listOf("blaze"),
            moves = listOf("scratch", "ember")
        )

        val entity = PokemonJpaEntity.fromDomain(pokemon)

        assertEquals(2L, entity.id)
        assertEquals("charmander", entity.name)
        assertEquals(listOf("blaze"), entity.abilities)
        assertEquals(listOf("scratch", "ember"), entity.moves)
    }

    @Test
    fun `fromDomain preserva id nulo para novos registros`() {
        val pokemon = Pokemon(name = "squirtle", abilities = listOf("torrent"), moves = listOf("water-gun"))

        val entity = PokemonJpaEntity.fromDomain(pokemon)

        assertNull(entity.id)
    }
}
