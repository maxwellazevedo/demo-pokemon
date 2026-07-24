package com.example.demopokemon.integration

import com.example.demopokemon.DemoPokemonApplication
import com.example.demopokemon.adapter.messaging.PokeConsumer
import com.example.demopokemon.adapter.messaging.PokeProducer
import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

/**
 * Teste de integração Kafka end-to-end com Testcontainers.
 * Valida o ciclo completo: PokeProducer → tópico Kafka → PokeConsumer → repositório.
 *
 * Aguarda a atribuição de partições ao consumer antes de enviar mensagens,
 * evitando perda de mensagens por auto.offset.reset=latest.
 */
@SpringBootTest(classes = [DemoPokemonApplication::class])
@Testcontainers
class KafkaIntegrationTest {

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.2.1"))

        @JvmStatic
        @DynamicPropertySource
        fun overrideKafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("topic") { "simple.topic.kafka.test" }
            // earliest garante que mensagens enviadas antes do rebalance sejam lidas
            registry.add("spring.kafka.consumer.auto-offset-reset") { "earliest" }
            // Recria o schema em cada execução — evita conflito de tipo TEXT→jsonb no banco local
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @Autowired
    private lateinit var producer: PokeProducer

    @Autowired
    private lateinit var consumer: PokeConsumer

    @Autowired
    private lateinit var pokemonRepository: PokemonRepositoryPort

    @Autowired
    private lateinit var endpointRegistry: KafkaListenerEndpointRegistry

    @BeforeEach
    fun setup() {
        consumer.clearLastReceived()
        // Aguarda todos os containers de listener terem suas partições atribuídas
        // antes de cada teste, eliminando a race condition com auto.offset.reset
        endpointRegistry.listenerContainers.forEach { container: MessageListenerContainer ->
            ContainerTestUtils.waitForAssignment(container, 1)
        }
    }

    @Test
    fun `produtor envia e consumidor recebe pokemon corretamente`() {
        val pokemon = Pokemon(
            name = "pikachu",
            abilities = listOf("static", "lightning-rod"),
            moves = listOf("thunder-shock", "quick-attack")
        )

        producer.sendPokemon(pokemon)

        await.atMost(20, TimeUnit.SECONDS).until {
            consumer.getLastReceived()?.name == "pikachu"
        }

        val received = consumer.getLastReceived()
        assertNotNull(received)
        assertEquals("pikachu", received?.name)
        assertEquals(listOf("static", "lightning-rod"), received?.abilities)
        assertEquals(listOf("thunder-shock", "quick-attack"), received?.moves)
    }

    @Test
    fun `consumidor persiste pokemon no repositorio apos receber mensagem`() {
        val pokemon = Pokemon(
            name = "eevee",
            abilities = listOf("run-away", "adaptability"),
            moves = listOf("tackle", "sand-attack")
        )

        producer.sendPokemon(pokemon)

        await.atMost(20, TimeUnit.SECONDS).until {
            consumer.getLastReceived()?.name == "eevee"
        }

        val saved = pokemonRepository.findByName("eevee")
        assertNotNull(saved)
        assertEquals("eevee", saved?.name)
    }
}
