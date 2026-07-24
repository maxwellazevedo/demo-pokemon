package com.example.demopokemon.application

import com.example.demopokemon.adapter.messaging.PokeProducer
import com.example.demopokemon.domain.model.Pokemon
import com.example.demopokemon.domain.port.PokemonRepositoryPort
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Caso de uso principal — orquestra busca, cache e publicação de Pokémon.
 *
 * Correções aplicadas:
 * - Operações JPA executadas em Schedulers.boundedElastic() para não bloquear o event loop reativo.
 * - URL da PokeAPI injetada via @Value (configurável por ambiente).
 * - ObjectMapper injetado pelo Spring (configuração centralizada com Kotlin module).
 */
@Service
class PokeApiService(
    private val webClientBuilder: WebClient.Builder,
    private val pokemonRepository: PokemonRepositoryPort,
    private val pokemonProducer: PokeProducer,
    private val objectMapper: ObjectMapper,
    @Value("\${pokeapi.base-url}") private val pokeApiBaseUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchAllPokemon(): Mono<List<Pokemon>> {
        log.info("m=fetchAllPokemon, stage=init")
        return Mono.fromCallable { pokemonRepository.findAll() }
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSuccess { log.info("m=fetchAllPokemon, stage=finish, total=${it.size}") }
    }

    fun fetchPokemon(pokemonName: String): Mono<Pokemon> {
        log.info("m=fetchPokemon, stage=init, name=$pokemonName")

        // Mono.fromCallable com nullable: usa justOrEmpty para emitir vazio se null
        return Mono.fromCallable { pokemonRepository.findByName(pokemonName) }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { cached ->
                log.info("m=fetchPokemon, stage=cache_hit, name=$pokemonName")
                Mono.justOrEmpty(cached)
            }
            .switchIfEmpty(
                Mono.defer {
                    log.info("m=fetchPokemon, stage=cache_miss, name=$pokemonName")
                    fetchFromApi(pokemonName)
                }
            )
    }

    private fun fetchFromApi(pokemonName: String): Mono<Pokemon> =
        webClientBuilder.build()
            .get()
            .uri("$pokeApiBaseUrl$pokemonName")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { body ->
                val json = objectMapper.readTree(body)
                val name = json["name"].asText()
                val abilities = json["abilities"].map { it["ability"]["name"].asText() }
                val moves = json["moves"].map { it["move"]["name"].asText() }
                Pokemon(name = name, abilities = abilities, moves = moves)
            }
            .doOnSuccess { pokemon ->
                pokemonProducer.sendPokemon(pokemon)
                log.info("m=fetchPokemon, stage=finish, name=$pokemonName")
            }
}
