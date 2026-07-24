package com.example.demopokemon.adapter.messaging

import com.fasterxml.jackson.annotation.JsonProperty
import com.example.demopokemon.domain.model.Pokemon

/**
 * DTO de resposta usado na comunicação via Kafka.
 * Renomeado de PokeResponseEntity para refletir corretamente seu papel (não é entidade JPA).
 */
data class PokemonResponseDTO(
    @JsonProperty("name") val name: String,
    @JsonProperty("abilities") val abilities: List<String>,
    @JsonProperty("moves") val moves: List<String>
) {
    companion object {
        fun fromDomain(pokemon: Pokemon) = PokemonResponseDTO(
            name = pokemon.name,
            abilities = pokemon.abilities,
            moves = pokemon.moves
        )
    }
}
