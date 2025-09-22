package com.example.demopokemon.unitTests

import com.example.demopokemon.dataprovider.kafka.consumer.PokeConsumer
import com.example.demopokemon.entity.PokeResponseEntity
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PokeConsumerTest {

    private lateinit var pokeConsumer: PokeConsumer

    @BeforeEach
    fun setup() {
        pokeConsumer = PokeConsumer()
    }

    /**
     * Tests that the `getMessage` method of `PokeConsumer` is called with the correct parameters.
     *
     * This test uses Mockito to create a spy of the `pokeConsumer` instance and verifies that
     * the `getMessage` method is called with a mocked ConsumerRecord.
     */
    @Test
    fun verifyGetMessageCalled() {
        // Mock do ConsumerRecord
        val consumerRecord: ConsumerRecord<String, PokeResponseEntity> = mock(ConsumerRecord::class.java as Class<ConsumerRecord<String, PokeResponseEntity>>)
        val pokeConsumerSpy = org.mockito.Mockito.spy(pokeConsumer)

        // Chama o método getMessage
        pokeConsumerSpy.getMessage(consumerRecord)

        // Verifica se o método getMessage foi chamado com o mock correto
        verify(pokeConsumerSpy).getMessage(consumerRecord)
    }

    /**
     * Tests that the payload is set correctly when a message is received.
     *
     * This test mocks a ConsumerRecord with a non-null value and verifies that
     * the `getPayload()` method of `pokeConsumer` returns the expected payload
     * after processing the record.
     */
    @Test
    fun setsPayloadWhenMessageReceived() {
        // Mock a ConsumerRecord and configure it to return a valid PokeResponseEntity
        val consumerRecord = mock<ConsumerRecord<String, PokeResponseEntity>>()
        val pokeResponse = PokeResponseEntity("Pikachu", listOf("Static"), listOf("Quick Attack"))
        `when`(consumerRecord.value()).thenReturn(pokeResponse)

        // Process the mock record and verify the payload
        pokeConsumer.getMessage(consumerRecord)
        assertEquals(pokeResponse, pokeConsumer.getPayload())
    }

    /**
     * Tests that the payload is not set when the consumer receives a null message.
     *
     * This test mocks a ConsumerRecord with a null value and verifies that
     * the `getPayload()` method of `pokeConsumer` returns null after processing
     * the record, ensuring that null messages do not update the payload.
     */
    @Test
    fun doesNotSetPayloadWhenNullMessageReceived() {
        val consumerRecord: ConsumerRecord<String, PokeResponseEntity> = mock(ConsumerRecord::class.java as Class<ConsumerRecord<String, PokeResponseEntity>>)
        `when`(consumerRecord.value()).thenReturn(null)

        pokeConsumer.getMessage(consumerRecord)

        assertNull(pokeConsumer.getPayload())
    }


    
    /**
     * Tests that the previous payload is retained when a null message is received.
     *
     * This test first sets an initial payload by passing a non-null ConsumerRecord to the consumer.
     * It then simulates receiving a ConsumerRecord with a null value and verifies that the consumer
     * retains the initial payload instead of overwriting it with null.
     */
    @Test
    fun retainsPreviousPayloadWhenNullMessageReceived() {
        val initialPayload = PokeResponseEntity(name = "Charmander", abilities = listOf("Blaze"), moves = listOf("Flamethrower"))

        // Mock do registro inicial
        val initialRecord: ConsumerRecord<String, PokeResponseEntity> = mock(ConsumerRecord::class.java as Class<ConsumerRecord<String, PokeResponseEntity>>)
        `when`(initialRecord.value()).thenReturn(initialPayload)
        pokeConsumer.getMessage(initialRecord)

        // Mock do registro com valor nulo
        val consumerRecord: ConsumerRecord<String, PokeResponseEntity> = mock(ConsumerRecord::class.java as Class<ConsumerRecord<String, PokeResponseEntity>>)
        `when`(consumerRecord.value()).thenReturn(null)

        pokeConsumer.getMessage(consumerRecord)

        // Verifica se o payload inicial foi mantido
        assertEquals(initialPayload, pokeConsumer.getPayload())
    }
}