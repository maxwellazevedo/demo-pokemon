package com.example.demopokemon.service

import com.example.demopokemon.dataprovider.kafka.producer.PokeProducer
import com.example.demopokemon.entity.PokemonEntity
import com.example.demopokemon.repository.PokemonRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class PokeApiService(
    @Autowired private val webClientBuilder: WebClient.Builder,
    @Autowired private val pokemonRepository: PokemonRepository,
    @Autowired private val pokemonProducer: PokeProducer

) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val apiUrl = "https://pokeapi.co/api/v2/pokemon/"

    fun fetchPokemon(pokemonName: String): Mono<PokemonEntity> {
        log.info("m=fetchPokemon, stage=init, pokemonName=$pokemonName")

        // Verificar no banco de dados
        val cachedPokemon = pokemonRepository.findByName(pokemonName)
        log.info("m=fetchPokemon, stage=cached, pokemonName=$pokemonName, msg=cachedPokemonFound=${cachedPokemon != null}")

        if (cachedPokemon != null) {
            log.info("m=fetchPokemon, stage=cached, pokemonName=$pokemonName")
            return Mono.just(cachedPokemon)
        }

        // Buscar na PokeAPI
        return webClientBuilder.build()
            .get()
            .uri("$apiUrl$pokemonName")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody ->
                val objectMapper = ObjectMapper()
                val jsonResponse = objectMapper.readTree(responseBody)

                val name = jsonResponse["name"].asText()
                val abilities = jsonResponse["abilities"]
                    .map { it["ability"]["name"].asText() }
                val moves = jsonResponse["moves"]
                    .map { it["move"]["name"].asText() }

                val pokemonEntity = PokemonEntity(name = name, abilities = abilities.toMutableList(), moves = moves.toMutableList())
                // Salvar no banco de dados
                pokemonRepository.save(pokemonEntity)
                pokemonProducer.sendPokemon(pokemonEntity)

                pokemonEntity
            }
            .doOnSuccess {
                log.info("m=fetchPokemon, stage=finish, pokemonName=$pokemonName")
            }
    }
}