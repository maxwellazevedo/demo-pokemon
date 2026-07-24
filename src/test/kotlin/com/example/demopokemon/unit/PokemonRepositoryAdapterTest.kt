package com.example.demopokemon.unit

import com.example.demopokemon.adapter.persistence.PokemonJpaEntity
import com.example.demopokemon.adapter.persistence.PokemonJpaRepository
import com.example.demopokemon.adapter.persistence.PokemonRepositoryAdapter
import com.example.demopokemon.domain.model.Pokemon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class PokemonRepositoryAdapterTest {

    private lateinit var jpaRepository: PokemonJpaRepository
    private lateinit var adapter: PokemonRepositoryAdapter

    @BeforeEach
    fun setup() {
        jpaRepository = mock()
        adapter = PokemonRepositoryAdapter(jpaRepository)
    }

    @Test
    fun `findByName retorna dominio quando entidade existe`() {
        val entity = PokemonJpaEntity(id = 1L, name = "pikachu", abilities = listOf("static"), moves = listOf("thunder-shock"))
        whenever(jpaRepository.findByName("pikachu")).thenReturn(entity)

        val result = adapter.findByName("pikachu")

        assertNotNull(result)
        assertEquals("pikachu", result?.name)
        assertEquals(listOf("static"), result?.abilities)
        assertEquals(listOf("thunder-shock"), result?.moves)
    }

    @Test
    fun `findByName retorna null quando entidade nao existe`() {
        whenever(jpaRepository.findByName("missingno")).thenReturn(null)

        val result = adapter.findByName("missingno")

        assertNull(result)
    }

    @Test
    fun `findAll retorna lista de dominio mapeada`() {
        val entities = listOf(
            PokemonJpaEntity(id = 1L, name = "bulbasaur", abilities = listOf("overgrow"), moves = listOf("tackle")),
            PokemonJpaEntity(id = 2L, name = "charmander", abilities = listOf("blaze"), moves = listOf("scratch"))
        )
        whenever(jpaRepository.findAll()).thenReturn(entities)

        val result = adapter.findAll()

        assertEquals(2, result.size)
        assertEquals("bulbasaur", result[0].name)
        assertEquals("charmander", result[1].name)
    }

    @Test
    fun `save persiste e retorna dominio`() {
        val pokemon = Pokemon(name = "squirtle", abilities = listOf("torrent"), moves = listOf("water-gun"))
        val savedEntity = PokemonJpaEntity(id = 3L, name = "squirtle", abilities = listOf("torrent"), moves = listOf("water-gun"))

        // Usa doReturn para evitar NPE com tipos não-nulos do Kotlin em mocks Mockito
        doReturn(savedEntity).whenever(jpaRepository).save(any())

        val result = adapter.save(pokemon)

        assertEquals(3L, result.id)
        assertEquals("squirtle", result.name)
        verify(jpaRepository).save(argThat { name == "squirtle" })
    }
}
