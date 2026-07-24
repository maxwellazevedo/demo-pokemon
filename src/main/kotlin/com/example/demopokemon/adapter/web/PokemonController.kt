package com.example.demopokemon.adapter.web

import com.example.demopokemon.application.PokeApiService
import com.example.demopokemon.domain.model.Pokemon
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

/**
 * Adaptador de entrada HTTP — expõe os casos de uso via REST.
 */
@RestController
@RequestMapping("/pokemon")
class PokemonController(private val pokeApiService: PokeApiService) {

    @GetMapping("/{name}")
    fun getPokemon(@PathVariable name: String): Mono<Map<String, Any>> =
        pokeApiService.fetchPokemon(name)
            .map { pokemon: Pokemon -> mapOf("data" to pokemon as Any) }
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Pokemon not found"))
            )
            .onErrorResume(ResponseStatusException::class.java) { ex ->
                Mono.just(mapOf("error" to (ex.reason ?: "Unknown error") as Any))
            }

    @GetMapping("/all")
    fun getAllPokemon(): Mono<List<Pokemon>> =
        pokeApiService.fetchAllPokemon()
}
