package com.example.demopokemon.controller

import com.example.demopokemon.entity.PokemonEntity
import com.example.demopokemon.service.PokeApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RequestMapping("/pokemon")
@RestController
class PokemonController(@Autowired private val pokeApiService: PokeApiService) {

    @GetMapping("/{name}")
    fun getPokemon(@PathVariable name: String): Mono<Map<String, Any>> {
        return pokeApiService.fetchPokemon(name)//TODO: Modifique o método fetchPokemon para verificar o banco antes de consultar a PokeAPI
            .map { pokemon: PokemonEntity -> mapOf("data" to pokemon as Any) }
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Pokemon not found"))
            )
            .onErrorResume(ResponseStatusException::class.java) { ex ->
                Mono.just(mapOf("error" to (ex.reason ?: "Unknown error") as Any))
            }
    }
}