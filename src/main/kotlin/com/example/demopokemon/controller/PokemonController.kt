package com.example.demopokemon.controller

import com.example.demopokemon.entity.PokeResponseEntity
import com.example.demopokemon.service.PokeApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RequestMapping("/pokemon")
@RestController
class PokemonController(@Autowired private val pokeApiService: PokeApiService) {

    @GetMapping("/{name}")
    fun getPokemon(@PathVariable name: String): Mono<PokeResponseEntity> {
        return pokeApiService.fetchPokemon(name)
    }

//    @GetMapping("/pokemon/{name}")
//    fun getPokemon(@PathVariable name: String): Mono<Any> {
//        return pokeApiService.fetchPokemon(name)
//            .map { pokemon ->
//                mapOf("data" to pokemon) as Any
//            }
//            .onErrorResume(WebClientResponseException.NotFound::class.java) {
//                Mono.fromSupplier { HttpStatus.NOT_FOUND }
//                    .doOnNext { println("Pokemon não encontrado: $name") }
//                    .doOnError { println("Erro ao tratar Pokemon não encontrado: ${it.message}") }
//            }
//            .onErrorResume(WebClientResponseException::class.java) {
//                Mono.fromSupplier { HttpStatus.INTERNAL_SERVER_ERROR }
//                    .doOnNext { println("Falha ao buscar dados da PokeAPI para: $name") }
//                    .doOnError { println("Erro ao tratar falha da PokeAPI: ${it.message}") }
//            }
//            .switchIfEmpty(Mono.just(HttpStatus.OK))
//    }
}