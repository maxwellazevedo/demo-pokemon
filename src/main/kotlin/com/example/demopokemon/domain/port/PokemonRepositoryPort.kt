package com.example.demopokemon.domain.port

import com.example.demopokemon.domain.model.Pokemon

/**
 * Porta de saída — define o contrato de persistência sem acoplamento à implementação.
 */
interface PokemonRepositoryPort {
    fun findByName(name: String): Pokemon?
    fun findAll(): List<Pokemon>
    fun save(pokemon: Pokemon): Pokemon
}
