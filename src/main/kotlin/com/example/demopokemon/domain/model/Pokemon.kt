package com.example.demopokemon.domain.model

/**
 * Modelo de domínio puro — sem anotações de framework.
 * Representa um Pokémon com suas habilidades e movimentos.
 */
data class Pokemon(
    val id: Long? = null,
    val name: String,
    val abilities: List<String>,
    val moves: List<String>
)
