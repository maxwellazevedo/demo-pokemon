package com.example.demopokemon.service

import com.example.demopokemon.dataprovider.kafka.producer.PokeProducer
import com.example.demopokemon.entity.PokeResponseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Service
class PokeApiService(
    @Autowired private val webClientBuilder: WebClient.Builder,
    @Autowired private val pokemonProducer: PokeProducer
    ) {

    private val log = LoggerFactory.getLogger(javaClass)
    val apiUrl = "https://pokeapi.co/api/v2/pokemon/"

    fun fetchPokemon(pokemonName: String): Mono<PokeResponseEntity> {
        log.info("m=fetchPokemon, stage=init i=get_pokemon")

        return webClientBuilder.build()
            .get()
            .uri(apiUrl + pokemonName)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody ->
                val objectMapper = ObjectMapper()
                val jsonResponse = objectMapper.readTree(responseBody)

                val name = jsonResponse["name"].asText()

                val abilities = jsonResponse["abilities"]
                    .map { it["ability"]["name"].asText() }
                    .toList()

                val moves = jsonResponse["moves"]
                    .map { it["move"]["name"].asText() }
                    .toList()

                val pokeResponse = PokeResponseEntity(name, abilities, moves)

                // Enviar o evento para o Kafka
                pokemonProducer.sendPokemon(pokeResponse)

                pokeResponse
            }
            .doOnSuccess {
                log.info("m=fetchPokemon, stage=finish i=get_pokemon, eventResponse=$it")
            }
    }
}