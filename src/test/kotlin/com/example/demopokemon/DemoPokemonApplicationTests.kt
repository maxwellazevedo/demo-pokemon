package com.example.demopokemon

import com.example.demopokemon.adapter.messaging.PokeConsumer
import com.example.demopokemon.adapter.messaging.PokeProducer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class DemoPokemonApplicationTests {

    // Evita que o contexto tente conectar ao Kafka real durante o boot
    @MockBean
    private lateinit var pokeProducer: PokeProducer

    @MockBean
    private lateinit var pokeConsumer: PokeConsumer

    @Test
    fun `contexto da aplicacao sobe corretamente`() {
        // Verifica que todos os beans são criados sem erros
    }
}
