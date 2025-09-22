import com.example.demopokemon.DemoPokemonApplication
import com.example.demopokemon.entity.PokemonEntity
import com.example.demopokemon.repository.PokemonRepository
import com.example.demopokemon.service.PokeApiService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(
    classes = [DemoPokemonApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)class PokeApiIntegrationTest(
    @Autowired private val pokeApiService: PokeApiService,
    @Autowired private val pokemonRepository: PokemonRepository,
    @Autowired private val restTemplate: TestRestTemplate
) {

    @LocalServerPort
    private var port: Int = 0

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        // Se tiver um repository para pokemon_abilities, limpe ele primeiro
        //pokemonRepository.deleteAll()
    }

    @Test
    fun `should fetch and save Pokemon from PokeAPI`() {
        val pokemonName = "pikachu"
        val pokemon = pokeApiService.fetchPokemon(pokemonName).block()

        Assertions.assertNotNull(pokemon)
        Assertions.assertEquals(pokemonName, pokemon?.name)

        val savedPokemon = pokemonRepository.findByName(pokemonName)
        Assertions.assertNotNull(savedPokemon)
        Assertions.assertEquals(pokemonName, savedPokemon?.name)
        // Opcional: validar se abilities e moves são JSON válidos
        assertDoesNotThrow { objectMapper.readTree(savedPokemon?.abilities ?: "") }
        assertDoesNotThrow { objectMapper.readTree(savedPokemon?.moves ?: "") }
    }

    @Test
    fun `should fetch Pokemon from database if already saved`() {
        val pokemonName = "pikachu"
        val abilitiesList = listOf("static", "lightning-rod")
        val movesList = listOf("thunder-shock", "quick-attack")
        val abilitiesJson = objectMapper.writeValueAsString(abilitiesList)
        val movesJson = objectMapper.writeValueAsString(movesList)

        val savedPokemon = pokemonRepository.save(
            PokemonEntity(
                name = pokemonName,
                abilities = abilitiesJson,
                moves = movesJson
            )
        )

        val pokemon = pokeApiService.fetchPokemon(pokemonName).block()

        Assertions.assertNotNull(pokemon)
        Assertions.assertEquals(savedPokemon.name, pokemon?.name)
        Assertions.assertEquals(savedPokemon.abilities, pokemon?.abilities)
        Assertions.assertEquals(savedPokemon.moves, pokemon?.moves)
    }
}
