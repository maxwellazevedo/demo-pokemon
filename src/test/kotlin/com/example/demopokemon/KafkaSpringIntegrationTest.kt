import com.example.demopokemon.DemoPokemonApplication
import com.example.demopokemon.dataprovider.kafka.consumer.PokeConsumer
import com.example.demopokemon.dataprovider.kafka.producer.PokeProducer
import com.example.demopokemon.entity.PokeResponseEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.awaitility.kotlin.await
import java.util.concurrent.TimeUnit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [DemoPokemonApplication::class])
@Testcontainers
class KafkaSpringIntegrationTest {

    companion object {
        @Container
        val kafka = KafkaContainer()

        @JvmStatic
        @DynamicPropertySource /*pra injetar o bootstrapServers e o topic no Spring*/
        fun overrideKafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("topic") { "simple.topic.kafka.test" }
        }
    }

    @Autowired
    private lateinit var producer: PokeProducer

    @Autowired
    private lateinit var consumer: PokeConsumer

    @Test
    fun `deve enviar e receber mensagem com beans reais do projeto`() {
        val expected = PokeResponseEntity(
            name = "pikachu",
            abilities = listOf("overgrow", "chlorophyll"),
            moves = listOf("tackle", "growl")
        )

        producer.sendPokemon(expected)

        // Esperar o consumidor processar a mensagem
        await.atMost(5, TimeUnit.SECONDS).until {
            consumer.getPayload() != null
        }

        val received = consumer.getPayload()
        assertEquals(expected.name, received?.name)
    }
}
