package com.example.demopokemon.unit

import com.example.demopokemon.adapter.messaging.PokeConsumer
import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class PokeConsumerTest {

    private lateinit var pokemonRepository: PokemonRepositoryPort
    private lateinit var consumer: PokeConsumer

    @BeforeEach
    fun setup() {
        pokemonRepository = mock()
        consumer = PokeConsumer(pokemonRepository)
        consumer.clearLastReceived()
    }

    @Test
    fun `getMessage salva pokemon no repositorio`() {
        val pokemon = Pokemon(name = "pikachu", abilities = listOf("static"), moves = listOf("thunder-shock"))
        val record: ConsumerRecord<String, Pokemon> = mock()
        whenever(record.value()).thenReturn(pokemon)
        whenever(pokemonRepository.save(any())).thenReturn(pokemon)

        consumer.getMessage(record)

        verify(pokemonRepository).save(pokemon)
    }

    @Test
    fun `getMessage atualiza lastReceived apos processar mensagem`() {
        val pokemon = Pokemon(name = "bulbasaur", abilities = listOf("overgrow"), moves = listOf("tackle"))
        val record: ConsumerRecord<String, Pokemon> = mock()
        whenever(record.value()).thenReturn(pokemon)
        whenever(pokemonRepository.save(any())).thenReturn(pokemon)

        consumer.getMessage(record)

        val last = consumer.getLastReceived()
        assertNotNull(last)
        assertEquals("bulbasaur", last?.name)
        assertEquals(listOf("overgrow"), last?.abilities)
        assertEquals(listOf("tackle"), last?.moves)
    }

    @Test
    fun `clearLastReceived zera o ultimo payload recebido`() {
        val pokemon = Pokemon(name = "charmander", abilities = listOf("blaze"), moves = listOf("scratch"))
        val record: ConsumerRecord<String, Pokemon> = mock()
        whenever(record.value()).thenReturn(pokemon)
        whenever(pokemonRepository.save(any())).thenReturn(pokemon)
        consumer.getMessage(record)

        consumer.clearLastReceived()

        assertNull(consumer.getLastReceived())
    }

    @Test
    fun `getLastReceived retorna null antes de qualquer mensagem`() {
        assertNull(consumer.getLastReceived())
    }
}
