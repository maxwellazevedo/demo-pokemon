package com.example.demopokemon.adapter.web

import com.example.demopokemon.application.PokeApiService
import com.example.demopokemon.domain.exception.PokemonNotFoundException
import com.example.demopokemon.domain.model.Pokemon
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Adaptador de entrada HTTP — expõe os casos de uso via REST.
 */
@RestController
@RequestMapping("/pokemon")
class PokemonController(private val pokeApiService: PokeApiService) {

    @GetMapping("/{name}")
    fun getPokemon(@PathVariable name: String): Mono<ResponseEntity<Map<String, Any>>> =
        pokeApiService.fetchPokemon(name)
            .map { pokemon -> ResponseEntity.ok(mapOf("data" to pokemon as Any)) }
            .onErrorResume(PokemonNotFoundException::class.java) { ex ->
                val body = mapOf("error" to "Pokemon '${ex.pokemonName}' not found" as Any)
                Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body))
            }

    @GetMapping("/all")
    fun getAllPokemon(): Mono<List<Pokemon>> =
        pokeApiService.fetchAllPokemon()
}
